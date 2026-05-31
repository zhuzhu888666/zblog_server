package cc.ztzhome.zblog.config;

import cc.ztzhome.zblog.utils.JwtUtil;
import cc.ztzhome.zblog.utils.RedisUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String TOKEN_PREFIX = "token:user:";

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);
                if (jwtUtil.validateToken(token)) {
                    Long userId = jwtUtil.getUserId(token);
                    String role = jwtUtil.getRole(token);

                    // 检查 token 在 Redis 中是否存在（被踢下线则不存在）
                    // Redis 不可用时降级为仅 JWT 校验，避免阻塞所有请求
                    try {
                        String redisKey = TOKEN_PREFIX + userId;
                        String storedToken = (String) redisUtil.get(redisKey);
                        if (storedToken == null || !storedToken.equals(token)) {
                            filterChain.doFilter(request, response);
                            return;
                        }
                    } catch (Exception e) {
                        log.warn("Redis 不可用，降级为仅 JWT 校验: {}", e.getMessage());
                    }

                    List<SimpleGrantedAuthority> authorities =
                            List.of(new SimpleGrantedAuthority("ROLE_" + role));

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userId, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    request.setAttribute("userId", userId);
                }
            }
        } catch (Exception e) {
            log.warn("JWT认证过滤失败: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}

package cc.ztzhome.zblog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 启用 CORS，会自动获取注入的 CorsConfigurationSource / CorsFilter
                .cors(cors -> {})
                // 禁用 CSRF（API 后端 + JWT 场景不需要）
                .csrf(csrf -> csrf.disable())
                // 无状态会话（不生成 JSESSIONID）
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 权限配置
                .authorizeHttpRequests(auth -> auth
                        // 公开接口放行
                        .requestMatchers("/public/**").permitAll()
                        // 其余所有接口需要认证
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
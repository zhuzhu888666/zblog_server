package cc.ztzhome.zblog.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    //private static final SecretKey SECRET_KEY = Jwts.SIG.HS256.key().build(); // 通过 HS256 算法生成强随机密钥

    @Value("${jwt.secret}")
    private String secret;

    // 普通登录状态的 token 有效期（毫秒）
    @Value("${jwt.expiration}")
    private Long expiration=7*24*60*60* 1000L;

    @Value("${jwt.remember-me-expiration}")
    private Long rememberMeExpiration=1000L*60*60*24*30; // 长时间 token 有效期（毫秒）

    // 获取签名密钥（JJWT 0.12.x 要求密钥长度至少为 32 字节）
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 JWT Token（不再存入 email）
     * @param id 用户ID
     * @param role 用户角色
     * @param isRememberMe 是否记住我（决定有效期）
     */
    public String generateToken(Long id, String role, boolean isRememberMe) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", id);
        claims.put("role", role);
        // 注意：不再存入 email

        long currentTimeMillis = System.currentTimeMillis();
        Date issuedAt = new Date(currentTimeMillis);
        long ttl = isRememberMe ? rememberMeExpiration : expiration;
        Date expirationDate = new Date(currentTimeMillis + ttl);

        // subject 改用用户 ID 的字符串形式
        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(id))
                .issuedAt(issuedAt)
                .expiration(expirationDate)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    // 兼容旧调用（不推荐，建议直接使用新方法）
    @Deprecated
    public String generateToken(Long id, String email, String role, boolean isRememberMe) {
        return generateToken(id, role, isRememberMe);
    }

    // 普通登录（非记住我）
    public String generateToken(Long id, String role) {
        return generateToken(id, role, false);
    }

    // 兼容旧调用
    @Deprecated
    public String generateToken(Long id, String email, String role) {
        return generateToken(id, role, false);
    }

    /**
     * 从 Token 中解析所有 Claims
     */
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("JWT Token已过期", e);
        } catch (JwtException e) {
            throw new RuntimeException("JWT Token无效", e);
        }
    }

    // 获取用户ID
    public Long getUserId(String token) {
        Claims claims = parseClaims(token);
        Object idObj = claims.get("id");
        if (idObj instanceof Integer) {
            return ((Integer) idObj).longValue();
        }
        return (Long) idObj;
    }

    // 获取角色
    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    /**
     * 验证 Token 是否有效（仅校验签名和过期时间，不再比对 email）
     */
    public boolean validateToken(String token) {
        try {
            // 能正常解析且未过期即为有效
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    // 判断 Token 是否过期
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = parseClaims(token).getExpiration();
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * 刷新 Token（保持相同的有效期策略）
     * @param oldToken 旧 Token
     * @param isRememberMe 是否记住我（决定新 Token 的有效期）
     */
    public String refreshToken(String oldToken, boolean isRememberMe) {
        if (isTokenExpired(oldToken)) {
            throw new RuntimeException("Token已过期，无法刷新");
        }
        Long userId = getUserId(oldToken);
        String role = getRole(oldToken);
        return generateToken(userId, role, isRememberMe);
    }

    // 获取 Token 剩余有效时间（毫秒）
    public long getRemainingTime(String token) {
        Date expiration = parseClaims(token).getExpiration();
        long remaining = expiration.getTime() - System.currentTimeMillis();
        return Math.max(remaining, 0);
    }
}
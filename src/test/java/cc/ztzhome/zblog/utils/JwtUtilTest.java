package cc.ztzhome.zblog.utils;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() throws Exception {
        jwtUtil = new JwtUtil();
        setField(jwtUtil, "secret", "this-is-a-test-secret-key-that-is-long-enough-for-hs256");
        setField(jwtUtil, "expiration", 7 * 24 * 60 * 60 * 1000L); // 7 days
        setField(jwtUtil, "rememberMeExpiration", 30L * 24 * 60 * 60 * 1000); // 30 days
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    @DisplayName("生成普通token - 验证能正常生成并解析出id和role")
    void testGenerateToken() {
        String token = jwtUtil.generateToken(1L, "user");
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(1L, jwtUtil.getUserId(token));
        assertEquals("user", jwtUtil.getRole(token));
    }

    @Test
    @DisplayName("生成记住我token - 不应报错")
    void testGenerateTokenRememberMe() {
        String token = jwtUtil.generateToken(1L, "admin", true);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("验证有效token返回true")
    void testValidateTokenValid() {
        String token = jwtUtil.generateToken(1L, "user");
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    @DisplayName("验证被篡改的token返回false")
    void testValidateTokenTampered() {
        String token = jwtUtil.generateToken(1L, "user");
        String tampered = token.substring(0, token.length() - 4) + "XXXX";
        assertFalse(jwtUtil.validateToken(tampered));
    }

    @Test
    @DisplayName("验证空字符串返回false")
    void testValidateTokenEmpty() {
        assertFalse(jwtUtil.validateToken(""));
    }

    @Test
    @DisplayName("验证null返回false")
    void testValidateTokenNull() {
        assertFalse(jwtUtil.validateToken(null));
    }

    @Test
    @DisplayName("isTokenExpired - 未过期token返回false")
    void testIsTokenExpiredFalse() {
        String token = jwtUtil.generateToken(1L, "user");
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    @DisplayName("isTokenExpired - 已过期token返回true")
    void testIsTokenExpiredTrue() throws Exception {
        JwtUtil shortLivedJwt = new JwtUtil();
        setField(shortLivedJwt, "secret", "this-is-a-test-secret-key-that-is-long-enough-for-hs256");
        setField(shortLivedJwt, "expiration", 1L);
        setField(shortLivedJwt, "rememberMeExpiration", 1L);

        String token = shortLivedJwt.generateToken(1L, "user");
        Thread.sleep(10);

        // 同一个密钥，同一个 JwtUtil 实例判断是否过期
        assertTrue(shortLivedJwt.isTokenExpired(token));
    }

    @Test
    @DisplayName("获取用户ID - 大数值Long")
    void testGetUserIdLargeNumber() {
        String token = jwtUtil.generateToken(123456789012345L, "user");
        assertEquals(123456789012345L, jwtUtil.getUserId(token));
    }

    @Test
    @DisplayName("获取用户ID - 小数值(可能被JJWT存为Integer)")
    void testGetUserIdSmallNumber() {
        String token = jwtUtil.generateToken(42L, "user");
        assertEquals(42L, jwtUtil.getUserId(token));
    }

    @Test
    @DisplayName("获取角色")
    void testGetRole() {
        String token = jwtUtil.generateToken(1L, "admin");
        assertEquals("admin", jwtUtil.getRole(token));
    }

    @Test
    @DisplayName("获取角色 - user角色")
    void testGetRoleUser() {
        String token = jwtUtil.generateToken(1L, "user");
        assertEquals("user", jwtUtil.getRole(token));
    }

    @Test
    @DisplayName("刷新有效token - 新token解析结果与旧token一致")
    void testRefreshTokenValid() {
        String oldToken = jwtUtil.generateToken(1L, "user");
        String newToken = jwtUtil.refreshToken(oldToken, false);
        assertNotNull(newToken);
        assertEquals(1L, jwtUtil.getUserId(newToken));
        assertEquals("user", jwtUtil.getRole(newToken));
    }

    @Test
    @DisplayName("刷新已过期token应抛出RuntimeException")
    void testRefreshExpiredToken() throws Exception {
        JwtUtil shortLivedJwt = new JwtUtil();
        setField(shortLivedJwt, "secret", "this-is-a-test-secret-key-that-is-long-enough-for-hs256");
        setField(shortLivedJwt, "expiration", 1L);
        setField(shortLivedJwt, "rememberMeExpiration", 1L);

        String expiredToken = shortLivedJwt.generateToken(1L, "user");
        Thread.sleep(10);

        assertThrows(RuntimeException.class, () ->
                shortLivedJwt.refreshToken(expiredToken, false));
    }

    @Test
    @DisplayName("获取剩余有效时间 - 未过期token应大于0")
    void testGetRemainingTimePositive() {
        String token = jwtUtil.generateToken(1L, "user");
        long remaining = jwtUtil.getRemainingTime(token);
        assertTrue(remaining > 0);
        assertTrue(remaining <= 7 * 24 * 60 * 60 * 1000L);
    }

    @Test
    @DisplayName("获取剩余有效时间 - 已过期token应返回0")
    void testGetRemainingTimeExpired() throws Exception {
        JwtUtil shortLivedJwt = new JwtUtil();
        setField(shortLivedJwt, "secret", "this-is-a-test-secret-key-that-is-long-enough-for-hs256");
        setField(shortLivedJwt, "expiration", 1L);
        setField(shortLivedJwt, "rememberMeExpiration", 1L);

        String token = shortLivedJwt.generateToken(1L, "user");
        Thread.sleep(10);

        assertEquals(0, shortLivedJwt.getRemainingTime(token));
    }

    @Test
    @DisplayName("getUserId对过期token应抛出ExpiredJwtException")
    void testGetUserIdExpiredToken() throws Exception {
        JwtUtil shortLivedJwt = new JwtUtil();
        setField(shortLivedJwt, "secret", "this-is-a-test-secret-key-that-is-long-enough-for-hs256");
        setField(shortLivedJwt, "expiration", 1L);
        setField(shortLivedJwt, "rememberMeExpiration", 1L);

        String token = shortLivedJwt.generateToken(1L, "user");
        Thread.sleep(10);

        assertThrows(ExpiredJwtException.class, () ->
                shortLivedJwt.getUserId(token));
    }

    @Test
    @DisplayName("validateToken对过期token应返回false")
    void testValidateTokenExpired() throws Exception {
        JwtUtil shortLivedJwt = new JwtUtil();
        setField(shortLivedJwt, "secret", "this-is-a-test-secret-key-that-is-long-enough-for-hs256");
        setField(shortLivedJwt, "expiration", 1L);
        setField(shortLivedJwt, "rememberMeExpiration", 1L);

        String token = shortLivedJwt.generateToken(1L, "user");
        Thread.sleep(10);

        assertFalse(shortLivedJwt.validateToken(token));
    }
}

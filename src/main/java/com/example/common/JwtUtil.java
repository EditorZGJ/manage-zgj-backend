package com.example.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {

    // 密钥（实际项目应该放在配置文件里）
    private static final String SECRET = "my-secret-key-which-is-at-least-256-bits-long-for-hs256";
    // 过期时间：24 小时
    private static final long EXPIRATION = 24 * 60 * 60 * 1000;

    private static SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    // 生成 token
    public static String generateToken(Long userId, String username) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getKey())
                .compact();
    }

    // 解析 token，获取 Claims
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 从 token 获取用户 ID
    public static Long getUserId(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }

    // 从 token 获取用户名
    public static String getUsername(String token) {
        return parseToken(token).get("username", String.class);
    }

    // 校验 token 是否过期
    public static boolean isExpired(String token) {
        return parseToken(token).getExpiration().before(new Date());
    }
}
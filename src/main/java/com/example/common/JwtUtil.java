package com.example.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private static JwtUtil instance;

    // token 黑名单（退出后加入，过期自动清除）
    private static final Set<String> blacklist = ConcurrentHashMap.newKeySet();

    @PostConstruct
    public void init() {
        instance = this;
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String username) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }

    public String getUsername(String token) {
        return parseToken(token).get("username", String.class);
    }

    // ====== 黑名单 ======

    /** 将 token 加入黑名单 */
    public static void invalidate(String token) {
        blacklist.add(token);
    }

    /** 检查 token 是否已被拉黑 */
    public static boolean isInvalid(String token) {
        return blacklist.contains(token);
    }

    // ====== 静态方法，方便在拦截器等非 Bean 中调用 ======
    public static String generate(Long userId, String username) {
        return instance.generateToken(userId, username);
    }

    public static Claims parse(String token) {
        return instance.parseToken(token);
    }
}
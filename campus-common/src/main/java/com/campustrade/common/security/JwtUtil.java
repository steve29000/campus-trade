package com.campustrade.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * 无状态 JWT 工具：由 campus-user 在登录时签发，由 campus-gateway 在网关处校验。
 *
 * <p>token 的 subject 为用户 id，附带 username 声明。校验失败（签名错误、过期、格式非法）
 * 会抛出 {@link JwtException}，调用方据此拒绝请求。</p>
 */
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMillis;

    public JwtUtil(String secret, long expirationMillis) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /**
     * 解析并校验 token，返回 claims。token 非法或过期时抛出 {@link JwtException}。
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 校验 token 是否有效（签名正确且未过期）。
     */
    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public Long getUserId(String token) {
        return Long.valueOf(parse(token).getSubject());
    }
}

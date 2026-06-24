package com.campustrade.user.service;

import com.campustrade.common.security.JwtUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

/**
 * JWT 登出处理：把已登出的 token 写入 Redis 黑名单，TTL 设为 token 的剩余有效期，
 * 到期后自动清理。网关在鉴权时会检查该黑名单。
 */
@Service
public class TokenService {

    public static final String BLOCKLIST_PREFIX = JwtUtil.BLOCKLIST_PREFIX;

    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;

    public TokenService(StringRedisTemplate redisTemplate, JwtUtil jwtUtil) {
        this.redisTemplate = redisTemplate;
        this.jwtUtil = jwtUtil;
    }

    public void revoke(String token) {
        if (token == null || !jwtUtil.isValid(token)) {
            return;
        }
        Date expiration = jwtUtil.getExpiration(token);
        long ttlMillis = expiration.getTime() - System.currentTimeMillis();
        if (ttlMillis > 0) {
            redisTemplate.opsForValue().set(BLOCKLIST_PREFIX + token, "1", Duration.ofMillis(ttlMillis));
        }
    }
}

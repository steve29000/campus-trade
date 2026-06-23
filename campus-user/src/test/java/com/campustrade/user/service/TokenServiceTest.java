package com.campustrade.user.service;

import com.campustrade.common.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TokenServiceTest {

    private static final String SECRET = "campus-trade-jwt-secret-key-for-token-service-tests-0123456789";

    private final JwtUtil jwtUtil = new JwtUtil(SECRET, 60_000L);
    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    private final ValueOperations<String, String> valueOps = mock(ValueOperations.class);
    private final TokenService tokenService = new TokenService(redisTemplate, jwtUtil);

    @Test
    void revokeAddsValidTokenToBlocklistWithTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        String token = jwtUtil.generateToken(1L, "alice");

        tokenService.revoke(token);

        verify(valueOps).set(eq(TokenService.BLOCKLIST_PREFIX + token), eq("1"), any(Duration.class));
    }

    @Test
    void revokeIgnoresInvalidToken() {
        tokenService.revoke("not-a-valid-token");

        verifyNoInteractions(valueOps);
    }

    @Test
    void revokeIgnoresExpiredToken() {
        String expired = new JwtUtil(SECRET, -1_000L).generateToken(1L, "bob");

        tokenService.revoke(expired);

        verify(redisTemplate, never()).opsForValue();
    }
}

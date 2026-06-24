package com.campustrade.common.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String SECRET = "campus-trade-jwt-secret-key-for-unit-test-0123456789";

    private final JwtUtil jwtUtil = new JwtUtil(SECRET, 60_000L);

    @Test
    void generatedTokenRoundTripsUserIdAndUsername() {
        String token = jwtUtil.generateToken(42L, "alice");

        assertThat(jwtUtil.isValid(token)).isTrue();
        assertThat(jwtUtil.getUserId(token)).isEqualTo(42L);
        assertThat(jwtUtil.parse(token).get("username", String.class)).isEqualTo("alice");
    }

    @Test
    void invalidTokenIsRejected() {
        assertThat(jwtUtil.isValid("not-a-real-token")).isFalse();
    }

    @Test
    void tokenSignedWithDifferentSecretIsRejected() {
        String token = new JwtUtil("another-secret-key-that-is-also-long-enough-1234567890", 60_000L)
                .generateToken(1L, "bob");

        assertThat(jwtUtil.isValid(token)).isFalse();
    }

    @Test
    void expiredTokenIsRejected() {
        String token = new JwtUtil(SECRET, -1_000L).generateToken(1L, "carol");

        assertThat(jwtUtil.isValid(token)).isFalse();
    }
}

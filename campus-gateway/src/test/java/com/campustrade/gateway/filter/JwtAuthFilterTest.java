package com.campustrade.gateway.filter;

import com.campustrade.common.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthFilterTest {

    private static final String SECRET = "campus-trade-jwt-secret-key-for-gateway-tests-0123456789";

    private final JwtUtil jwtUtil = new JwtUtil(SECRET, 60_000L);
    @SuppressWarnings("unchecked")
    private final ReactiveStringRedisTemplate redisTemplate = mock(ReactiveStringRedisTemplate.class);
    private final JwtAuthFilter filter = new JwtAuthFilter(jwtUtil, new ObjectMapper(), redisTemplate);

    private void notRevoked() {
        when(redisTemplate.hasKey(anyString())).thenReturn(Mono.just(false));
    }

    @Test
    void whitelistedLoginPathPassesThroughWithoutToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/user/login"));
        AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();

        filter.filter(exchange, capturingChain(forwarded)).block();

        assertThat(forwarded.get()).isNotNull();
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void similarLoginPrefixRequiresToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/user/login-extra"));
        AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();

        filter.filter(exchange, capturingChain(forwarded)).block();

        assertThat(forwarded.get()).isNull();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void missingTokenIsRejectedWithUnauthorized() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/product/1"));
        AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();

        filter.filter(exchange, capturingChain(forwarded)).block();

        assertThat(forwarded.get()).isNull();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void invalidTokenIsRejectedWithUnauthorized() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/order/1").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-token"));
        AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();

        filter.filter(exchange, capturingChain(forwarded)).block();

        assertThat(forwarded.get()).isNull();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void validTokenPassesAndForwardsUserIdHeader() {
        notRevoked();
        String token = jwtUtil.generateToken(7L, "alice");
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/product/1").header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
        AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();

        filter.filter(exchange, capturingChain(forwarded)).block();

        assertThat(exchange.getResponse().getStatusCode()).isNull();
        assertThat(forwarded.get()).isNotNull();
        assertThat(forwarded.get().getRequest().getHeaders().getFirst("X-User-Id")).isEqualTo("7");
    }

    @Test
    void validTokenReplacesForgedUserIdHeader() {
        notRevoked();
        String token = jwtUtil.generateToken(7L, "alice");
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/product/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .header("X-User-Id", "999"));
        AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();

        filter.filter(exchange, capturingChain(forwarded)).block();

        assertThat(forwarded.get()).isNotNull();
        assertThat(forwarded.get().getRequest().getHeaders().get("X-User-Id")).containsExactly("7");
    }

    @Test
    void revokedTokenIsRejectedWithUnauthorized() {
        when(redisTemplate.hasKey(anyString())).thenReturn(Mono.just(true));
        String token = jwtUtil.generateToken(7L, "alice");
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/product/1").header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
        AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();

        filter.filter(exchange, capturingChain(forwarded)).block();

        assertThat(forwarded.get()).isNull();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private GatewayFilterChain capturingChain(AtomicReference<ServerWebExchange> holder) {
        return exchange -> {
            holder.set(exchange);
            return Mono.empty();
        };
    }
}

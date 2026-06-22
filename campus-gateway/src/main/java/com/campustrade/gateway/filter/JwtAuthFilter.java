package com.campustrade.gateway.filter;

import com.campustrade.common.response.ApiResponse;
import com.campustrade.common.response.ResultCode;
import com.campustrade.common.security.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 网关 JWT 鉴权过滤器。登录和注册路径放行，其余请求必须携带有效的
 * {@code Authorization: Bearer <token>}，否则直接返回 401。
 *
 * <p>校验通过后把用户 id 放进 {@code X-User-Id} 请求头传递给下游服务。</p>
 */
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private static final List<String> WHITELIST = List.of("/user/login", "/user/register");

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public JwtAuthFilter(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (isWhitelisted(path)) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return unauthorized(exchange, "missing or malformed Authorization header");
        }

        String token = authorization.substring("Bearer ".length());
        if (!jwtUtil.isValid(token)) {
            return unauthorized(exchange, "invalid or expired token");
        }

        Long userId = jwtUtil.getUserId(token);
        ServerWebExchange mutated = exchange.mutate()
                .request(builder -> builder.header("X-User-Id", String.valueOf(userId)))
                .build();
        return chain.filter(mutated);
    }

    private boolean isWhitelisted(String path) {
        return WHITELIST.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ApiResponse<Void> body = ApiResponse.fail(ResultCode.UNAUTHORIZED, message);
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException exception) {
            bytes = ("{\"code\":401,\"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        }
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // 在路由转发之前执行鉴权。
        return -1;
    }
}

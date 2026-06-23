package com.campustrade.product.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.campustrade.common.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

/**
 * Sentinel 限流被触发时的统一响应：返回 HTTP 429 + 统一 {@link ApiResponse} 结构，
 * 而不是 Sentinel 默认的纯文本「Blocked by Sentinel」。
 */
@Component
public class SentinelBlockHandler implements BlockExceptionHandler {

    private final ObjectMapper objectMapper;

    public SentinelBlockHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, String resourceName,
                       BlockException e) throws Exception {
        response.setStatus(429);
        response.setContentType("application/json;charset=UTF-8");
        ApiResponse<Void> body = ApiResponse.fail(429, "请求过于频繁，请稍后再试");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}

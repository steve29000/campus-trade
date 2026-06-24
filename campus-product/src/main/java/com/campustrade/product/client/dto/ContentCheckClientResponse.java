package com.campustrade.product.client.dto;

/**
 * 调用 campus-ai 内容检查接口的最小响应 DTO。{@code passed} 为 false 表示命中违规，{@code reason} 给出原因。
 */
public record ContentCheckClientResponse(
        Boolean passed,
        String reason
) {
}

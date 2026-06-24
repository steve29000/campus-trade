package com.campustrade.product.client.dto;

/**
 * 调用 campus-ai 内容检查接口的最小请求 DTO，由商品服务自行维护，避免直接依赖 AI 模块的 DTO。
 */
public record ContentCheckClientRequest(
        String content
) {
}

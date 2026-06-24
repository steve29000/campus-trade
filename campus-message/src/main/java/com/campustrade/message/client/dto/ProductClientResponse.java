package com.campustrade.message.client.dto;

/**
 * 调用 campus-product 的最小响应 DTO，留言服务只关心商品是否存在以及标题。
 */
public record ProductClientResponse(
        Long id,
        String title
) {
}

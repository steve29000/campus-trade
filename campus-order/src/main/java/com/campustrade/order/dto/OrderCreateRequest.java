package com.campustrade.order.dto;

public record OrderCreateRequest(
        Long buyerId,
        Long sellerId,
        Long productId
) {
}

package com.campustrade.order.dto;

import com.campustrade.order.enums.OrderStatus;

import java.math.BigDecimal;

public record OrderResponse(
        Long id,
        Long buyerId,
        Long sellerId,
        Long productId,
        String productTitle,
        BigDecimal price,
        OrderStatus status
) {
}

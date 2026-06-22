package com.campustrade.order.dto;

import java.math.BigDecimal;

public record OrderCreateRequest(
        Long buyerId,
        Long sellerId,
        Long productId,
        String productTitle,
        BigDecimal price
) {
}

package com.campustrade.order.client.dto;

import com.campustrade.order.client.enums.ProductClientStatus;

import java.math.BigDecimal;

public record ProductClientResponse(
        Long id,
        Long sellerId,
        String title,
        String description,
        String category,
        BigDecimal price,
        ProductClientStatus status
) {
}

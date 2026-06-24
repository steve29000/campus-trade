package com.campustrade.product.dto;

import com.campustrade.product.enums.ProductStatus;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        Long sellerId,
        String title,
        String description,
        String category,
        BigDecimal price,
        ProductStatus status
) {
}

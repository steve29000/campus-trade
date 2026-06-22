package com.campustrade.product.dto;

import java.math.BigDecimal;

public record ProductCreateRequest(
        Long sellerId,
        String title,
        String description,
        String category,
        BigDecimal price
) {
}

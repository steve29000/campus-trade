package com.campustrade.ai.dto;

import java.math.BigDecimal;

public record PriceSuggestResponse(
        BigDecimal suggestedPrice,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String reason
) {
}

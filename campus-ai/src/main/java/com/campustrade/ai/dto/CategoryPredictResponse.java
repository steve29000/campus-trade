package com.campustrade.ai.dto;

public record CategoryPredictResponse(
        String category,
        Double confidence
) {
}

package com.campustrade.ai.dto;

public record ContentCheckResponse(
        Boolean passed,
        String reason
) {
}

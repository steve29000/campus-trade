package com.campustrade.ai.dto;

public record PriceSuggestRequest(
        String category,
        String title,
        String description
) {
}

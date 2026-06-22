package com.campustrade.message.dto;

public record MessageCreateRequest(
        Long productId,
        Long senderId,
        String content
) {
}

package com.campustrade.message.dto;

import com.campustrade.message.enums.MessageStatus;

public record MessageResponse(
        Long id,
        Long productId,
        Long senderId,
        String content,
        MessageStatus status
) {
}

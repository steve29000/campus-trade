package com.campustrade.order.client.dto;

public record UserProfileClientResponse(
        Long id,
        String username,
        String nickname
) {
}

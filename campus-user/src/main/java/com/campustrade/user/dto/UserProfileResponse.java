package com.campustrade.user.dto;

public record UserProfileResponse(
        Long id,
        String username,
        String nickname
) {
}

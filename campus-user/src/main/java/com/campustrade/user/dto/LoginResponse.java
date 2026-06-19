package com.campustrade.user.dto;

public record LoginResponse(
        String token,
        UserProfileResponse user
) {
}

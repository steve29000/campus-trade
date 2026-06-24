package com.campustrade.user.dto;

public record UserLoginRequest(
        String username,
        String password
) {

    @Override
    public String toString() {
        return "UserLoginRequest[username=" + username + ", password=***]";
    }
}

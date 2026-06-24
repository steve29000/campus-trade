package com.campustrade.user.dto;

public record UserRegisterRequest(
        String username,
        String password,
        String nickname
) {

    @Override
    public String toString() {
        return "UserRegisterRequest[username=" + username + ", password=***, nickname=" + nickname + "]";
    }
}

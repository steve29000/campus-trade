package com.campustrade.user.service;

import com.campustrade.common.response.ApiResponse;
import com.campustrade.common.response.ResultCode;
import com.campustrade.user.dto.LoginResponse;
import com.campustrade.user.dto.UserLoginRequest;
import com.campustrade.user.dto.UserProfileResponse;
import com.campustrade.user.dto.UserRegisterRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserServiceTest {

    private final UserService userService = new UserService();

    @Test
    void registerCreatesUserProfile() {
        ApiResponse<UserProfileResponse> response = userService.register(
                new UserRegisterRequest("alice", "secret", "Alice")
        );

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.message()).isEqualTo(ResultCode.SUCCESS.getMessage());
        assertThat(response.data().id()).isPositive();
        assertThat(response.data())
                .extracting(UserProfileResponse::username, UserProfileResponse::nickname)
                .containsExactly("alice", "Alice");
    }

    @Test
    void registerRejectsDuplicateUsername() {
        userService.register(new UserRegisterRequest("alice", "secret", "Alice"));

        ApiResponse<UserProfileResponse> response = userService.register(
                new UserRegisterRequest("alice", "other-secret", "Other Alice")
        );

        assertThat(response.code()).isEqualTo(ResultCode.CONFLICT.getCode());
        assertThat(response.message()).isEqualTo("username already exists");
        assertThat(response.data()).isNull();
    }

    @Test
    void loginReturnsMockTokenAndProfileForMatchingPassword() {
        userService.register(new UserRegisterRequest("alice", "secret", "Alice"));

        ApiResponse<LoginResponse> response = userService.login(new UserLoginRequest("alice", "secret"));

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data().token()).isNotBlank();
        assertThat(response.data().user())
                .extracting(UserProfileResponse::username, UserProfileResponse::nickname)
                .containsExactly("alice", "Alice");
    }

    @Test
    void loginRejectsWrongPassword() {
        userService.register(new UserRegisterRequest("alice", "secret", "Alice"));

        ApiResponse<LoginResponse> response = userService.login(new UserLoginRequest("alice", "wrong"));

        assertThat(response.code()).isEqualTo(ResultCode.UNAUTHORIZED.getCode());
        assertThat(response.message()).isEqualTo("username or password is incorrect");
        assertThat(response.data()).isNull();
    }

    @Test
    void findProfileReturnsRegisteredUser() {
        ApiResponse<UserProfileResponse> registerResponse = userService.register(
                new UserRegisterRequest("alice", "secret", "Alice")
        );

        ApiResponse<UserProfileResponse> response = userService.findProfile(registerResponse.data().id());

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data().id()).isEqualTo(registerResponse.data().id());
        assertThat(response.data())
                .extracting(UserProfileResponse::username, UserProfileResponse::nickname)
                .containsExactly("alice", "Alice");
    }

    @Test
    void requestToStringRedactsPasswords() {
        String registerText = new UserRegisterRequest("alice", "secret", "Alice").toString();
        String loginText = new UserLoginRequest("alice", "secret").toString();

        assertThat(registerText).contains("password=***").doesNotContain("secret");
        assertThat(loginText).contains("password=***").doesNotContain("secret");
    }
}

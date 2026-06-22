package com.campustrade.user.controller;

import com.campustrade.common.response.ApiResponse;
import com.campustrade.user.dto.LoginResponse;
import com.campustrade.user.dto.UserLoginRequest;
import com.campustrade.user.dto.UserProfileResponse;
import com.campustrade.user.dto.UserRegisterRequest;
import com.campustrade.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerWebTest {

    private final CapturingUserService userService = new CapturingUserService();
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new UserController(userService))
            .build();

    @Test
    void registerRouteBindsJsonRequestAndReturnsUserProfile() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "alice",
                                  "password": "secret",
                                  "nickname": "Alice"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("alice"))
                .andExpect(jsonPath("$.data.nickname").value("Alice"));

        assertThat(userService.registerRequest.username()).isEqualTo("alice");
        assertThat(userService.registerRequest.password()).isEqualTo("secret");
    }

    @Test
    void loginRouteBindsJsonRequestAndReturnsToken() throws Exception {
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "alice",
                                  "password": "secret"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("mock-token-user-1"));

        assertThat(userService.loginRequest.username()).isEqualTo("alice");
        assertThat(userService.loginRequest.password()).isEqualTo("secret");
    }

    @Test
    void findProfileRouteBindsPathVariable() throws Exception {
        mockMvc.perform(get("/user/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(7))
                .andExpect(jsonPath("$.data.username").value("alice"));

        assertThat(userService.id).isEqualTo(7L);
    }

    private static class CapturingUserService extends UserService {

        private UserRegisterRequest registerRequest;
        private UserLoginRequest loginRequest;
        private Long id;

        @Override
        public ApiResponse<UserProfileResponse> register(UserRegisterRequest request) {
            this.registerRequest = request;
            return ApiResponse.success(new UserProfileResponse(1L, request.username(), request.nickname()));
        }

        @Override
        public ApiResponse<LoginResponse> login(UserLoginRequest request) {
            this.loginRequest = request;
            return ApiResponse.success(new LoginResponse(
                    "mock-token-user-1",
                    new UserProfileResponse(1L, request.username(), "Alice")
            ));
        }

        @Override
        public ApiResponse<UserProfileResponse> findProfile(Long id) {
            this.id = id;
            return ApiResponse.success(new UserProfileResponse(id, "alice", "Alice"));
        }
    }
}

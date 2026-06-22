package com.campustrade.user.controller;

import com.campustrade.common.response.ApiResponse;
import com.campustrade.user.dto.LoginResponse;
import com.campustrade.user.dto.UserLoginRequest;
import com.campustrade.user.dto.UserProfileResponse;
import com.campustrade.user.dto.UserRegisterRequest;
import com.campustrade.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ApiResponse<UserProfileResponse> register(@RequestBody UserRegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody UserLoginRequest request) {
        return userService.login(request);
    }

    @GetMapping("/{id}")
    public ApiResponse<UserProfileResponse> findProfile(@PathVariable("id") Long id) {
        return userService.findProfile(id);
    }
}

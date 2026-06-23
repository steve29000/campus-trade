package com.campustrade.user.controller;

import com.campustrade.common.response.ApiResponse;
import com.campustrade.user.service.TokenService;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登出接口。登出需要携带有效 token（网关已校验），把该 token 加入 Redis 黑名单使其失效。
 */
@RestController
@RequestMapping("/user")
public class AuthController {

    private final TokenService tokenService;

    public AuthController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            tokenService.revoke(authorization.substring("Bearer ".length()));
        }
        return ApiResponse.success();
    }
}

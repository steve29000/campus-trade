package com.campustrade.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campustrade.common.response.ApiResponse;
import com.campustrade.common.response.ResultCode;
import com.campustrade.common.security.JwtUtil;
import com.campustrade.user.dto.LoginResponse;
import com.campustrade.user.dto.UserLoginRequest;
import com.campustrade.user.dto.UserProfileResponse;
import com.campustrade.user.dto.UserRegisterRequest;
import com.campustrade.user.entity.UserEntity;
import com.campustrade.user.mapper.UserMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public ApiResponse<UserProfileResponse> register(UserRegisterRequest request) {
        if (request == null || isBlank(request.username()) || isBlank(request.password())) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "username and password are required");
        }

        String username = request.username().trim();
        if (findByUsername(username) != null) {
            return ApiResponse.fail(ResultCode.CONFLICT, "username already exists");
        }

        String nickname = isBlank(request.nickname()) ? username : request.nickname().trim();
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setNickname(nickname);

        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException exception) {
            // 兜底并发下的唯一约束冲突，等价于用户名已存在。
            return ApiResponse.fail(ResultCode.CONFLICT, "username already exists");
        }

        return ApiResponse.success(toProfile(user));
    }

    public ApiResponse<LoginResponse> login(UserLoginRequest request) {
        if (request == null || isBlank(request.username()) || isBlank(request.password())) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "username and password are required");
        }

        UserEntity user = findByUsername(request.username().trim());
        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            return ApiResponse.fail(ResultCode.UNAUTHORIZED, "username or password is incorrect");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return ApiResponse.success(new LoginResponse(token, toProfile(user)));
    }

    public ApiResponse<UserProfileResponse> findProfile(Long id) {
        if (id == null) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "user id is required");
        }

        UserEntity user = userMapper.selectById(id);
        if (user == null) {
            return ApiResponse.fail(ResultCode.NOT_FOUND, "user not found");
        }

        return ApiResponse.success(toProfile(user));
    }

    private UserEntity findByUsername(String username) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUsername, username)
        );
    }

    private UserProfileResponse toProfile(UserEntity user) {
        return new UserProfileResponse(user.getId(), user.getUsername(), user.getNickname());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

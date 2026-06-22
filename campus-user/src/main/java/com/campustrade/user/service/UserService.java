package com.campustrade.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campustrade.common.response.ApiResponse;
import com.campustrade.common.response.ResultCode;
import com.campustrade.user.dto.LoginResponse;
import com.campustrade.user.dto.UserLoginRequest;
import com.campustrade.user.dto.UserProfileResponse;
import com.campustrade.user.dto.UserRegisterRequest;
import com.campustrade.user.entity.UserEntity;
import com.campustrade.user.mapper.UserMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
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
        // Mock-only: replace plain text storage with password hashing before adding authentication.
        user.setPassword(request.password());
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
        if (user == null || !user.getPassword().equals(request.password())) {
            return ApiResponse.fail(ResultCode.UNAUTHORIZED, "username or password is incorrect");
        }

        String mockToken = "mock-token-user-" + user.getId();
        return ApiResponse.success(new LoginResponse(mockToken, toProfile(user)));
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

package com.campustrade.user.service;

import com.campustrade.common.response.ApiResponse;
import com.campustrade.common.response.ResultCode;
import com.campustrade.user.dto.LoginResponse;
import com.campustrade.user.dto.UserLoginRequest;
import com.campustrade.user.dto.UserProfileResponse;
import com.campustrade.user.dto.UserRegisterRequest;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {

    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Map<Long, StoredUser> usersById = new ConcurrentHashMap<>();
    private final Map<String, Long> userIdsByUsername = new ConcurrentHashMap<>();

    public ApiResponse<UserProfileResponse> register(UserRegisterRequest request) {
        if (request == null || isBlank(request.username()) || isBlank(request.password())) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "username and password are required");
        }

        String username = request.username().trim();
        if (userIdsByUsername.containsKey(username)) {
            return ApiResponse.fail(ResultCode.CONFLICT, "username already exists");
        }

        Long id = idGenerator.getAndIncrement();
        String nickname = isBlank(request.nickname()) ? username : request.nickname().trim();
        // Mock-only: replace plain text storage with password hashing before adding persistence.
        StoredUser user = new StoredUser(id, username, request.password(), nickname);

        Long existingUserId = userIdsByUsername.putIfAbsent(username, id);
        if (existingUserId != null) {
            return ApiResponse.fail(ResultCode.CONFLICT, "username already exists");
        }

        usersById.put(id, user);

        return ApiResponse.success(toProfile(user));
    }

    public ApiResponse<LoginResponse> login(UserLoginRequest request) {
        if (request == null || isBlank(request.username()) || isBlank(request.password())) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "username and password are required");
        }

        Long userId = userIdsByUsername.get(request.username().trim());
        if (userId == null) {
            return ApiResponse.fail(ResultCode.UNAUTHORIZED, "username or password is incorrect");
        }

        StoredUser user = usersById.get(userId);
        if (user == null || !user.password().equals(request.password())) {
            return ApiResponse.fail(ResultCode.UNAUTHORIZED, "username or password is incorrect");
        }

        String mockToken = "mock-token-user-" + user.id();
        return ApiResponse.success(new LoginResponse(mockToken, toProfile(user)));
    }

    public ApiResponse<UserProfileResponse> findProfile(Long id) {
        if (id == null) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "user id is required");
        }

        StoredUser user = usersById.get(id);
        if (user == null) {
            return ApiResponse.fail(ResultCode.NOT_FOUND, "user not found");
        }

        return ApiResponse.success(toProfile(user));
    }

    private UserProfileResponse toProfile(StoredUser user) {
        return new UserProfileResponse(user.id(), user.username(), user.nickname());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private record StoredUser(
            Long id,
            String username,
            String password,
            String nickname
    ) {
    }
}

package com.campustrade.message.client;

import com.campustrade.common.response.ApiResponse;
import com.campustrade.message.client.dto.UserProfileClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "campus-user")
public interface UserClient {

    @GetMapping("/user/{id}")
    ApiResponse<UserProfileClientResponse> findProfile(@PathVariable("id") Long id);
}

package com.campustrade.message.client;

import com.campustrade.common.response.ApiResponse;
import com.campustrade.message.client.dto.ProductClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "campus-product")
public interface ProductClient {

    @GetMapping("/product/{id}")
    ApiResponse<ProductClientResponse> findById(@PathVariable("id") Long id);
}

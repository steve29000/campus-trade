package com.campustrade.order.client;

import com.campustrade.common.response.ApiResponse;
import com.campustrade.order.client.dto.ProductClientResponse;
import com.campustrade.order.client.dto.ProductStatusUpdateClientRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "campus-product")
public interface ProductClient {

    @GetMapping("/product/{id}")
    ApiResponse<ProductClientResponse> findById(@PathVariable("id") Long id);

    @PutMapping("/product/{id}/status")
    ApiResponse<ProductClientResponse> updateStatus(
            @PathVariable("id") Long id,
            @RequestBody ProductStatusUpdateClientRequest request
    );
}

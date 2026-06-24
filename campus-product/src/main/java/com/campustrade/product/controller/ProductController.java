package com.campustrade.product.controller;

import com.campustrade.common.response.ApiResponse;
import com.campustrade.product.dto.ProductCreateRequest;
import com.campustrade.product.dto.ProductResponse;
import com.campustrade.product.dto.ProductStatusUpdateRequest;
import com.campustrade.product.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ApiResponse<ProductResponse> publish(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody ProductCreateRequest request
    ) {
        return productService.publish(request, userId);
    }

    @GetMapping
    public ApiResponse<List<ProductResponse>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) String status
    ) {
        return productService.list(keyword, category, status);
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> findById(@PathVariable("id") Long id) {
        return productService.findById(id);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<ProductResponse> updateStatus(
            @PathVariable("id") Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody ProductStatusUpdateRequest request
    ) {
        return productService.updateStatus(id, request, userId);
    }
}

package com.campustrade.order.controller;

import com.campustrade.common.response.ApiResponse;
import com.campustrade.order.dto.OrderCreateRequest;
import com.campustrade.order.dto.OrderResponse;
import com.campustrade.order.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ApiResponse<OrderResponse> create(
            @RequestBody OrderCreateRequest request,
            @RequestHeader("X-User-Id") Long userId
    ) {
        return orderService.create(request, userId);
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> findById(
            @PathVariable("id") Long id,
            @RequestHeader("X-User-Id") Long userId
    ) {
        return orderService.findById(id, userId);
    }

    @GetMapping("/buyer/{buyerId}")
    public ApiResponse<List<OrderResponse>> listByBuyerId(
            @PathVariable("buyerId") Long buyerId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        return orderService.listByBuyerId(buyerId, userId);
    }

    @GetMapping("/seller/{sellerId}")
    public ApiResponse<List<OrderResponse>> listBySellerId(
            @PathVariable("sellerId") Long sellerId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        return orderService.listBySellerId(sellerId, userId);
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<OrderResponse> cancel(
            @PathVariable("id") Long id,
            @RequestHeader("X-User-Id") Long userId
    ) {
        return orderService.cancel(id, userId);
    }

    @PutMapping("/{id}/complete")
    public ApiResponse<OrderResponse> complete(
            @PathVariable("id") Long id,
            @RequestHeader("X-User-Id") Long userId
    ) {
        return orderService.complete(id, userId);
    }
}

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
    public ApiResponse<OrderResponse> create(@RequestBody OrderCreateRequest request) {
        return orderService.create(request);
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> findById(@PathVariable("id") Long id) {
        return orderService.findById(id);
    }

    @GetMapping("/buyer/{buyerId}")
    public ApiResponse<List<OrderResponse>> listByBuyerId(@PathVariable("buyerId") Long buyerId) {
        return orderService.listByBuyerId(buyerId);
    }

    @GetMapping("/seller/{sellerId}")
    public ApiResponse<List<OrderResponse>> listBySellerId(@PathVariable("sellerId") Long sellerId) {
        return orderService.listBySellerId(sellerId);
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<OrderResponse> cancel(@PathVariable("id") Long id) {
        return orderService.cancel(id);
    }

    @PutMapping("/{id}/complete")
    public ApiResponse<OrderResponse> complete(@PathVariable("id") Long id) {
        return orderService.complete(id);
    }
}

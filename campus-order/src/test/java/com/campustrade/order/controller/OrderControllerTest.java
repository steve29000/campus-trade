package com.campustrade.order.controller;

import com.campustrade.common.response.ApiResponse;
import com.campustrade.common.response.ResultCode;
import com.campustrade.order.dto.OrderCreateRequest;
import com.campustrade.order.dto.OrderResponse;
import com.campustrade.order.enums.OrderStatus;
import com.campustrade.order.service.OrderService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderControllerTest {

    private final CapturingOrderService orderService = new CapturingOrderService();
    private final OrderController orderController = new OrderController(orderService);

    @Test
    void createDelegatesToOrderService() {
        OrderCreateRequest request = new OrderCreateRequest(2L, 1L, 10L);

        ApiResponse<OrderResponse> response = orderController.create(request);

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data()).isEqualTo(orderService.order);
        assertThat(orderService.createRequest).isEqualTo(request);
    }

    @Test
    void findByIdDelegatesToOrderService() {
        ApiResponse<OrderResponse> response = orderController.findById(7L);

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data()).isEqualTo(orderService.order);
        assertThat(orderService.id).isEqualTo(7L);
    }

    @Test
    void listByBuyerIdDelegatesToOrderService() {
        ApiResponse<List<OrderResponse>> response = orderController.listByBuyerId(2L);

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data()).containsExactly(orderService.order);
        assertThat(orderService.buyerId).isEqualTo(2L);
    }

    @Test
    void listBySellerIdDelegatesToOrderService() {
        ApiResponse<List<OrderResponse>> response = orderController.listBySellerId(1L);

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data()).containsExactly(orderService.order);
        assertThat(orderService.sellerId).isEqualTo(1L);
    }

    @Test
    void cancelDelegatesToOrderService() {
        ApiResponse<OrderResponse> response = orderController.cancel(7L);

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data()).isEqualTo(orderService.order);
        assertThat(orderService.id).isEqualTo(7L);
    }

    @Test
    void completeDelegatesToOrderService() {
        ApiResponse<OrderResponse> response = orderController.complete(7L);

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data()).isEqualTo(orderService.order);
        assertThat(orderService.id).isEqualTo(7L);
    }

    private static class CapturingOrderService extends OrderService {

        private final OrderResponse order = new OrderResponse(
                7L,
                2L,
                1L,
                10L,
                "iPad Air",
                java.math.BigDecimal.valueOf(2800),
                OrderStatus.CREATED
        );

        private OrderCreateRequest createRequest;
        private Long id;
        private Long buyerId;
        private Long sellerId;

        private CapturingOrderService() {
            super(null, null);
        }

        @Override
        public ApiResponse<OrderResponse> create(OrderCreateRequest request) {
            this.createRequest = request;
            return ApiResponse.success(order);
        }

        @Override
        public ApiResponse<OrderResponse> findById(Long id) {
            this.id = id;
            return ApiResponse.success(order);
        }

        @Override
        public ApiResponse<List<OrderResponse>> listByBuyerId(Long buyerId) {
            this.buyerId = buyerId;
            return ApiResponse.success(List.of(order));
        }

        @Override
        public ApiResponse<List<OrderResponse>> listBySellerId(Long sellerId) {
            this.sellerId = sellerId;
            return ApiResponse.success(List.of(order));
        }

        @Override
        public ApiResponse<OrderResponse> cancel(Long id) {
            this.id = id;
            return ApiResponse.success(order);
        }

        @Override
        public ApiResponse<OrderResponse> complete(Long id) {
            this.id = id;
            return ApiResponse.success(order);
        }
    }
}

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

        ApiResponse<OrderResponse> response = orderController.create(request, 2L);

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data()).isEqualTo(orderService.order);
        assertThat(orderService.createRequest).isEqualTo(request);
        assertThat(orderService.authenticatedUserId).isEqualTo(2L);
    }

    @Test
    void findByIdDelegatesToOrderService() {
        ApiResponse<OrderResponse> response = orderController.findById(7L, 2L);

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data()).isEqualTo(orderService.order);
        assertThat(orderService.id).isEqualTo(7L);
        assertThat(orderService.authenticatedUserId).isEqualTo(2L);
    }

    @Test
    void listByBuyerIdDelegatesToOrderService() {
        ApiResponse<List<OrderResponse>> response = orderController.listByBuyerId(2L, 2L);

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data()).containsExactly(orderService.order);
        assertThat(orderService.buyerId).isEqualTo(2L);
        assertThat(orderService.authenticatedUserId).isEqualTo(2L);
    }

    @Test
    void listBySellerIdDelegatesToOrderService() {
        ApiResponse<List<OrderResponse>> response = orderController.listBySellerId(1L, 1L);

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data()).containsExactly(orderService.order);
        assertThat(orderService.sellerId).isEqualTo(1L);
        assertThat(orderService.authenticatedUserId).isEqualTo(1L);
    }

    @Test
    void cancelDelegatesToOrderService() {
        ApiResponse<OrderResponse> response = orderController.cancel(7L, 2L);

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data()).isEqualTo(orderService.order);
        assertThat(orderService.id).isEqualTo(7L);
        assertThat(orderService.authenticatedUserId).isEqualTo(2L);
    }

    @Test
    void completeDelegatesToOrderService() {
        ApiResponse<OrderResponse> response = orderController.complete(7L, 2L);

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data()).isEqualTo(orderService.order);
        assertThat(orderService.id).isEqualTo(7L);
        assertThat(orderService.authenticatedUserId).isEqualTo(2L);
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
        private Long authenticatedUserId;

        private CapturingOrderService() {
            super(null, null, null);
        }

        @Override
        public ApiResponse<OrderResponse> create(OrderCreateRequest request, Long authenticatedUserId) {
            this.createRequest = request;
            this.authenticatedUserId = authenticatedUserId;
            return ApiResponse.success(order);
        }

        @Override
        public ApiResponse<OrderResponse> findById(Long id, Long authenticatedUserId) {
            this.id = id;
            this.authenticatedUserId = authenticatedUserId;
            return ApiResponse.success(order);
        }

        @Override
        public ApiResponse<List<OrderResponse>> listByBuyerId(Long buyerId, Long authenticatedUserId) {
            this.buyerId = buyerId;
            this.authenticatedUserId = authenticatedUserId;
            return ApiResponse.success(List.of(order));
        }

        @Override
        public ApiResponse<List<OrderResponse>> listBySellerId(Long sellerId, Long authenticatedUserId) {
            this.sellerId = sellerId;
            this.authenticatedUserId = authenticatedUserId;
            return ApiResponse.success(List.of(order));
        }

        @Override
        public ApiResponse<OrderResponse> cancel(Long id, Long authenticatedUserId) {
            this.id = id;
            this.authenticatedUserId = authenticatedUserId;
            return ApiResponse.success(order);
        }

        @Override
        public ApiResponse<OrderResponse> complete(Long id, Long authenticatedUserId) {
            this.id = id;
            this.authenticatedUserId = authenticatedUserId;
            return ApiResponse.success(order);
        }
    }
}

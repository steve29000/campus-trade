package com.campustrade.order.controller;

import com.campustrade.common.response.ApiResponse;
import com.campustrade.order.dto.OrderCreateRequest;
import com.campustrade.order.dto.OrderResponse;
import com.campustrade.order.enums.OrderStatus;
import com.campustrade.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerWebTest {

    private final CapturingOrderService orderService = new CapturingOrderService();
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new OrderController(orderService))
            .build();

    @Test
    void createRouteBindsJsonRequestAndReturnsOrderJson() throws Exception {
        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", 2L)
                        .content("""
                                {
                                  "buyerId": 99,
                                  "sellerId": 1,
                                  "productId": 10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.productTitle").value("iPad Air"))
                .andExpect(jsonPath("$.data.price").value(2800))
                .andExpect(jsonPath("$.data.status").value("CREATED"));

        assertThat(orderService.createRequest.buyerId()).isEqualTo(99L);
        assertThat(orderService.createRequest.sellerId()).isEqualTo(1L);
        assertThat(orderService.createRequest.productId()).isEqualTo(10L);
        assertThat(orderService.authenticatedUserId).isEqualTo(2L);
    }

    @Test
    void findByIdRouteBindsPathVariable() throws Exception {
        mockMvc.perform(get("/order/7")
                        .header("X-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(7));

        assertThat(orderService.id).isEqualTo(7L);
        assertThat(orderService.authenticatedUserId).isEqualTo(2L);
    }

    @Test
    void buyerRouteBindsPathVariableAndSerializesList() throws Exception {
        mockMvc.perform(get("/order/buyer/2")
                        .header("X-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].buyerId").value(2));

        assertThat(orderService.buyerId).isEqualTo(2L);
        assertThat(orderService.authenticatedUserId).isEqualTo(2L);
    }

    @Test
    void sellerRouteBindsPathVariableAndSerializesList() throws Exception {
        mockMvc.perform(get("/order/seller/1")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].sellerId").value(1));

        assertThat(orderService.sellerId).isEqualTo(1L);
        assertThat(orderService.authenticatedUserId).isEqualTo(1L);
    }

    @Test
    void cancelRouteBindsPathVariableAndReturnsCancelledStatus() throws Exception {
        mockMvc.perform(put("/order/7/cancel")
                        .header("X-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        assertThat(orderService.id).isEqualTo(7L);
        assertThat(orderService.authenticatedUserId).isEqualTo(2L);
    }

    @Test
    void completeRouteBindsPathVariableAndReturnsCompletedStatus() throws Exception {
        mockMvc.perform(put("/order/7/complete")
                        .header("X-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        assertThat(orderService.id).isEqualTo(7L);
        assertThat(orderService.authenticatedUserId).isEqualTo(2L);
    }

    private static class CapturingOrderService extends OrderService {

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
            return ApiResponse.success(order(request.buyerId(), request.sellerId(), request.productId(),
                    "iPad Air", BigDecimal.valueOf(2800), OrderStatus.CREATED));
        }

        @Override
        public ApiResponse<OrderResponse> findById(Long id, Long authenticatedUserId) {
            this.id = id;
            this.authenticatedUserId = authenticatedUserId;
            return ApiResponse.success(order(2L, 1L, 10L, "iPad Air", BigDecimal.valueOf(2800),
                    OrderStatus.CREATED));
        }

        @Override
        public ApiResponse<List<OrderResponse>> listByBuyerId(Long buyerId, Long authenticatedUserId) {
            this.buyerId = buyerId;
            this.authenticatedUserId = authenticatedUserId;
            return ApiResponse.success(List.of(order(buyerId, 1L, 10L, "iPad Air", BigDecimal.valueOf(2800),
                    OrderStatus.CREATED)));
        }

        @Override
        public ApiResponse<List<OrderResponse>> listBySellerId(Long sellerId, Long authenticatedUserId) {
            this.sellerId = sellerId;
            this.authenticatedUserId = authenticatedUserId;
            return ApiResponse.success(List.of(order(2L, sellerId, 10L, "iPad Air", BigDecimal.valueOf(2800),
                    OrderStatus.CREATED)));
        }

        @Override
        public ApiResponse<OrderResponse> cancel(Long id, Long authenticatedUserId) {
            this.id = id;
            this.authenticatedUserId = authenticatedUserId;
            return ApiResponse.success(order(2L, 1L, 10L, "iPad Air", BigDecimal.valueOf(2800),
                    OrderStatus.CANCELLED));
        }

        @Override
        public ApiResponse<OrderResponse> complete(Long id, Long authenticatedUserId) {
            this.id = id;
            this.authenticatedUserId = authenticatedUserId;
            return ApiResponse.success(order(2L, 1L, 10L, "iPad Air", BigDecimal.valueOf(2800),
                    OrderStatus.COMPLETED));
        }

        private OrderResponse order(Long buyerId, Long sellerId, Long productId, String productTitle,
                                    BigDecimal price, OrderStatus status) {
            return new OrderResponse(7L, buyerId, sellerId, productId, productTitle, price, status);
        }
    }
}

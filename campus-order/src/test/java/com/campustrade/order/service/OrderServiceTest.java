package com.campustrade.order.service;

import com.campustrade.common.response.ApiResponse;
import com.campustrade.common.response.ResultCode;
import com.campustrade.order.dto.OrderCreateRequest;
import com.campustrade.order.dto.OrderResponse;
import com.campustrade.order.enums.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderServiceTest {

    private final OrderService orderService = new OrderService();

    @Test
    void createCreatesOrderWithCreatedStatus() {
        ApiResponse<OrderResponse> response = orderService.create(orderRequest(2L, 1L, 10L, "iPad Air",
                BigDecimal.valueOf(2800)));

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.message()).isEqualTo(ResultCode.SUCCESS.getMessage());
        assertThat(response.data().id()).isPositive();
        assertThat(response.data())
                .extracting(
                        OrderResponse::buyerId,
                        OrderResponse::sellerId,
                        OrderResponse::productId,
                        OrderResponse::productTitle,
                        OrderResponse::price,
                        OrderResponse::status
                )
                .containsExactly(2L, 1L, 10L, "iPad Air", BigDecimal.valueOf(2800), OrderStatus.CREATED);
    }

    @Test
    void createRejectsMissingBuyerId() {
        ApiResponse<OrderResponse> response = orderService.create(orderRequest(null, 1L, 10L, "iPad Air",
                BigDecimal.valueOf(2800)));

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("buyer id is required");
        assertThat(response.data()).isNull();
    }

    @Test
    void createRejectsMissingSellerId() {
        ApiResponse<OrderResponse> response = orderService.create(orderRequest(2L, null, 10L, "iPad Air",
                BigDecimal.valueOf(2800)));

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("seller id is required");
        assertThat(response.data()).isNull();
    }

    @Test
    void createRejectsSameBuyerAndSeller() {
        ApiResponse<OrderResponse> response = orderService.create(orderRequest(2L, 2L, 10L, "iPad Air",
                BigDecimal.valueOf(2800)));

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("buyer and seller cannot be the same");
        assertThat(response.data()).isNull();
    }

    @Test
    void createRejectsMissingProductId() {
        ApiResponse<OrderResponse> response = orderService.create(orderRequest(2L, 1L, null, "iPad Air",
                BigDecimal.valueOf(2800)));

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("product id is required");
        assertThat(response.data()).isNull();
    }

    @Test
    void createRejectsBlankProductTitle() {
        ApiResponse<OrderResponse> response = orderService.create(orderRequest(2L, 1L, 10L, " ",
                BigDecimal.valueOf(2800)));

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("product title is required");
        assertThat(response.data()).isNull();
    }

    @Test
    void createRejectsNegativePrice() {
        ApiResponse<OrderResponse> response = orderService.create(orderRequest(2L, 1L, 10L, "iPad Air",
                BigDecimal.valueOf(-1)));

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("price must be greater than or equal to 0");
        assertThat(response.data()).isNull();
    }

    @Test
    void createRejectsMissingPrice() {
        ApiResponse<OrderResponse> response = orderService.create(orderRequest(2L, 1L, 10L, "iPad Air", null));

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("price must be greater than or equal to 0");
        assertThat(response.data()).isNull();
    }

    @Test
    void findByIdReturnsNotFoundForMissingOrder() {
        ApiResponse<OrderResponse> response = orderService.findById(99L);

        assertThat(response.code()).isEqualTo(ResultCode.NOT_FOUND.getCode());
        assertThat(response.message()).isEqualTo("order not found");
        assertThat(response.data()).isNull();
    }

    @Test
    void listByBuyerIdReturnsOrdersSortedByIdAscending() {
        OrderResponse first = orderService.create(orderRequest(2L, 1L, 10L, "iPad Air",
                BigDecimal.valueOf(2800))).data();
        orderService.create(orderRequest(3L, 1L, 11L, "Desk Lamp", BigDecimal.valueOf(30)));
        OrderResponse second = orderService.create(orderRequest(2L, 4L, 12L, "Road Bike",
                BigDecimal.valueOf(500))).data();

        ApiResponse<List<OrderResponse>> response = orderService.listByBuyerId(2L);

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data()).extracting(OrderResponse::id).containsExactly(first.id(), second.id());
    }

    @Test
    void listBySellerIdReturnsOrdersSortedByIdAscending() {
        OrderResponse first = orderService.create(orderRequest(2L, 1L, 10L, "iPad Air",
                BigDecimal.valueOf(2800))).data();
        orderService.create(orderRequest(2L, 4L, 11L, "Desk Lamp", BigDecimal.valueOf(30)));
        OrderResponse second = orderService.create(orderRequest(3L, 1L, 12L, "Road Bike",
                BigDecimal.valueOf(500))).data();

        ApiResponse<List<OrderResponse>> response = orderService.listBySellerId(1L);

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data()).extracting(OrderResponse::id).containsExactly(first.id(), second.id());
    }

    @Test
    void cancelChangesOrderStatusToCancelled() {
        OrderResponse created = orderService.create(orderRequest(2L, 1L, 10L, "iPad Air",
                BigDecimal.valueOf(2800))).data();

        ApiResponse<OrderResponse> response = orderService.cancel(created.id());

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data().status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(orderService.findById(created.id()).data().status()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cancelRejectsCompletedOrder() {
        OrderResponse created = orderService.create(orderRequest(2L, 1L, 10L, "iPad Air",
                BigDecimal.valueOf(2800))).data();
        orderService.complete(created.id());

        ApiResponse<OrderResponse> response = orderService.cancel(created.id());

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("completed order cannot be cancelled");
        assertThat(response.data()).isNull();
    }

    @Test
    void cancelReturnsNotFoundForMissingOrder() {
        ApiResponse<OrderResponse> response = orderService.cancel(99L);

        assertThat(response.code()).isEqualTo(ResultCode.NOT_FOUND.getCode());
        assertThat(response.message()).isEqualTo("order not found");
        assertThat(response.data()).isNull();
    }

    @Test
    void completeChangesOrderStatusToCompleted() {
        OrderResponse created = orderService.create(orderRequest(2L, 1L, 10L, "iPad Air",
                BigDecimal.valueOf(2800))).data();

        ApiResponse<OrderResponse> response = orderService.complete(created.id());

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data().status()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(orderService.findById(created.id()).data().status()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void completeRejectsCancelledOrder() {
        OrderResponse created = orderService.create(orderRequest(2L, 1L, 10L, "iPad Air",
                BigDecimal.valueOf(2800))).data();
        orderService.cancel(created.id());

        ApiResponse<OrderResponse> response = orderService.complete(created.id());

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("cancelled order cannot be completed");
        assertThat(response.data()).isNull();
    }

    @Test
    void completeReturnsNotFoundForMissingOrder() {
        ApiResponse<OrderResponse> response = orderService.complete(99L);

        assertThat(response.code()).isEqualTo(ResultCode.NOT_FOUND.getCode());
        assertThat(response.message()).isEqualTo("order not found");
        assertThat(response.data()).isNull();
    }

    private OrderCreateRequest orderRequest(Long buyerId, Long sellerId, Long productId, String productTitle,
                                            BigDecimal price) {
        return new OrderCreateRequest(buyerId, sellerId, productId, productTitle, price);
    }
}

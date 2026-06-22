package com.campustrade.order.service;

import com.campustrade.common.response.ApiResponse;
import com.campustrade.common.response.ResultCode;
import com.campustrade.order.client.ProductClient;
import com.campustrade.order.client.UserClient;
import com.campustrade.order.client.dto.ProductClientResponse;
import com.campustrade.order.client.dto.ProductStatusUpdateClientRequest;
import com.campustrade.order.client.dto.UserProfileClientResponse;
import com.campustrade.order.client.enums.ProductClientStatus;
import com.campustrade.order.dto.OrderCreateRequest;
import com.campustrade.order.dto.OrderResponse;
import com.campustrade.order.enums.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderServiceTest {

    private final FakeUserClient userClient = new FakeUserClient();
    private final FakeProductClient productClient = new FakeProductClient();
    private final OrderService orderService = new OrderService(userClient, productClient);

    @Test
    void createLoadsProductSnapshotFromProductService() {
        ApiResponse<OrderResponse> response = orderService.create(orderRequest(2L, 1L, 10L));

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
        assertThat(userClient.requestedIds).containsExactly(2L, 1L);
        assertThat(productClient.requestedIds).containsExactly(10L);
        assertThat(productClient.soldProductIds).containsExactly(10L);
        assertThat(productClient.statusOf(10L)).isEqualTo(ProductClientStatus.SOLD);
    }

    @Test
    void createRejectsMissingBuyerId() {
        ApiResponse<OrderResponse> response = orderService.create(orderRequest(null, 1L, 10L));

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("buyer id is required");
        assertThat(response.data()).isNull();
        assertThat(userClient.requestedIds).isEmpty();
        assertThat(productClient.requestedIds).isEmpty();
    }

    @Test
    void createRejectsMissingSellerId() {
        ApiResponse<OrderResponse> response = orderService.create(orderRequest(2L, null, 10L));

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("seller id is required");
        assertThat(response.data()).isNull();
        assertThat(userClient.requestedIds).isEmpty();
        assertThat(productClient.requestedIds).isEmpty();
    }

    @Test
    void createRejectsSameBuyerAndSellerBeforeRemoteCalls() {
        ApiResponse<OrderResponse> response = orderService.create(orderRequest(2L, 2L, 10L));

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("buyer and seller cannot be the same");
        assertThat(response.data()).isNull();
        assertThat(userClient.requestedIds).isEmpty();
        assertThat(productClient.requestedIds).isEmpty();
    }

    @Test
    void createRejectsMissingProductId() {
        ApiResponse<OrderResponse> response = orderService.create(orderRequest(2L, 1L, null));

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("product id is required");
        assertThat(response.data()).isNull();
        assertThat(userClient.requestedIds).isEmpty();
        assertThat(productClient.requestedIds).isEmpty();
    }

    @Test
    void createRejectsMissingBuyerFromUserService() {
        userClient.missingIds.add(2L);

        ApiResponse<OrderResponse> response = orderService.create(orderRequest(2L, 1L, 10L));

        assertThat(response.code()).isEqualTo(ResultCode.NOT_FOUND.getCode());
        assertThat(response.message()).isEqualTo("buyer not found");
        assertThat(response.data()).isNull();
        assertThat(userClient.requestedIds).containsExactly(2L);
        assertThat(productClient.requestedIds).isEmpty();
    }

    @Test
    void createRejectsMissingSellerFromUserService() {
        userClient.missingIds.add(1L);

        ApiResponse<OrderResponse> response = orderService.create(orderRequest(2L, 1L, 10L));

        assertThat(response.code()).isEqualTo(ResultCode.NOT_FOUND.getCode());
        assertThat(response.message()).isEqualTo("seller not found");
        assertThat(response.data()).isNull();
        assertThat(userClient.requestedIds).containsExactly(2L, 1L);
        assertThat(productClient.requestedIds).isEmpty();
    }

    @Test
    void createRejectsMissingProductFromProductService() {
        productClient.missingIds.add(10L);

        ApiResponse<OrderResponse> response = orderService.create(orderRequest(2L, 1L, 10L));

        assertThat(response.code()).isEqualTo(ResultCode.NOT_FOUND.getCode());
        assertThat(response.message()).isEqualTo("product not found");
        assertThat(response.data()).isNull();
    }

    @Test
    void createRejectsProductThatIsNotOnSale() {
        productClient.defaultStatus = ProductClientStatus.OFF_SALE;

        ApiResponse<OrderResponse> response = orderService.create(orderRequest(2L, 1L, 10L));

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("product is not on sale");
        assertThat(response.data()).isNull();
    }

    @Test
    void createRejectsSellerThatDoesNotOwnProduct() {
        productClient.sellerId = 99L;

        ApiResponse<OrderResponse> response = orderService.create(orderRequest(2L, 1L, 10L));

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("seller does not match product owner");
        assertThat(response.data()).isNull();
    }

    @Test
    void createRejectsSecondOrderForSoldProduct() {
        ApiResponse<OrderResponse> first = orderService.create(orderRequest(2L, 1L, 10L));

        ApiResponse<OrderResponse> second = orderService.create(orderRequest(3L, 1L, 10L));

        assertThat(first.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(second.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(second.message()).isEqualTo("product is not on sale");
        assertThat(second.data()).isNull();
        assertThat(productClient.soldProductIds).containsExactly(10L);
    }

    @Test
    void createReturnsSystemErrorWhenUserServiceThrowsException() {
        userClient.throwException = true;

        ApiResponse<OrderResponse> response = orderService.create(orderRequest(2L, 1L, 10L));

        assertThat(response.code()).isEqualTo(ResultCode.SYSTEM_ERROR.getCode());
        assertThat(response.message()).isEqualTo("remote service unavailable");
        assertThat(response.data()).isNull();
    }

    @Test
    void createReturnsSystemErrorWhenProductServiceThrowsException() {
        productClient.throwException = true;

        ApiResponse<OrderResponse> response = orderService.create(orderRequest(2L, 1L, 10L));

        assertThat(response.code()).isEqualTo(ResultCode.SYSTEM_ERROR.getCode());
        assertThat(response.message()).isEqualTo("remote service unavailable");
        assertThat(response.data()).isNull();
    }

    @Test
    void createReturnsSystemErrorWhenProductStatusUpdateFails() {
        productClient.failStatusUpdate = true;

        ApiResponse<OrderResponse> response = orderService.create(orderRequest(2L, 1L, 10L));

        assertThat(response.code()).isEqualTo(ResultCode.SYSTEM_ERROR.getCode());
        assertThat(response.message()).isEqualTo("product status update failed");
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
        OrderResponse first = orderService.create(orderRequest(2L, 1L, 10L)).data();
        orderService.create(orderRequest(3L, 1L, 11L));
        OrderResponse second = orderService.create(orderRequest(2L, 1L, 12L)).data();

        ApiResponse<List<OrderResponse>> response = orderService.listByBuyerId(2L);

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data()).extracting(OrderResponse::id).containsExactly(first.id(), second.id());
    }

    @Test
    void listBySellerIdReturnsOrdersSortedByIdAscending() {
        OrderResponse first = orderService.create(orderRequest(2L, 1L, 10L)).data();
        orderService.create(orderRequest(2L, 4L, 11L));
        OrderResponse second = orderService.create(orderRequest(3L, 1L, 12L)).data();

        ApiResponse<List<OrderResponse>> response = orderService.listBySellerId(1L);

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data()).extracting(OrderResponse::id).containsExactly(first.id(), second.id());
    }

    @Test
    void cancelChangesOrderStatusToCancelled() {
        OrderResponse created = orderService.create(orderRequest(2L, 1L, 10L)).data();

        ApiResponse<OrderResponse> response = orderService.cancel(created.id());

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data().status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(orderService.findById(created.id()).data().status()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cancelRejectsCompletedOrder() {
        OrderResponse created = orderService.create(orderRequest(2L, 1L, 10L)).data();
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
        OrderResponse created = orderService.create(orderRequest(2L, 1L, 10L)).data();

        ApiResponse<OrderResponse> response = orderService.complete(created.id());

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data().status()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(orderService.findById(created.id()).data().status()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void completeRejectsCancelledOrder() {
        OrderResponse created = orderService.create(orderRequest(2L, 1L, 10L)).data();
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

    private OrderCreateRequest orderRequest(Long buyerId, Long sellerId, Long productId) {
        return new OrderCreateRequest(buyerId, sellerId, productId);
    }

    private static class FakeUserClient implements UserClient {

        private final List<Long> requestedIds = new java.util.ArrayList<>();
        private final List<Long> missingIds = new java.util.ArrayList<>();
        private boolean throwException;

        @Override
        public ApiResponse<UserProfileClientResponse> findProfile(Long id) {
            if (throwException) {
                throw new IllegalStateException("user service unavailable");
            }
            requestedIds.add(id);
            if (missingIds.contains(id)) {
                return ApiResponse.fail(ResultCode.NOT_FOUND, "user not found");
            }
            return remoteSuccess(new UserProfileClientResponse(id, "user" + id, "User " + id));
        }
    }

    private static class FakeProductClient implements ProductClient {

        private final List<Long> requestedIds = new java.util.ArrayList<>();
        private final List<Long> missingIds = new java.util.ArrayList<>();
        private final List<Long> soldProductIds = new java.util.ArrayList<>();
        private final java.util.Map<Long, ProductClientStatus> statusesById = new java.util.HashMap<>();
        private Long sellerId = 1L;
        private ProductClientStatus defaultStatus = ProductClientStatus.ON_SALE;
        private boolean throwException;
        private boolean failStatusUpdate;

        @Override
        public ApiResponse<ProductClientResponse> findById(Long id) {
            if (throwException) {
                throw new IllegalStateException("product service unavailable");
            }
            requestedIds.add(id);
            if (missingIds.contains(id)) {
                return ApiResponse.fail(ResultCode.NOT_FOUND, "product not found");
            }
            return remoteSuccess(product(id));
        }

        @Override
        public ApiResponse<ProductClientResponse> updateStatus(Long id, ProductStatusUpdateClientRequest request) {
            if (throwException) {
                throw new IllegalStateException("product service unavailable");
            }
            if (failStatusUpdate) {
                return ApiResponse.fail(ResultCode.SYSTEM_ERROR, "update failed");
            }
            soldProductIds.add(id);
            statusesById.put(id, ProductClientStatus.valueOf(request.status()));
            return remoteSuccess(product(id));
        }

        private ProductClientResponse product(Long id) {
            return new ProductClientResponse(
                    id,
                    sellerId,
                    id == 10L ? "iPad Air" : "Product " + id,
                    "Campus item",
                    "数码",
                    id == 10L ? BigDecimal.valueOf(2800) : BigDecimal.valueOf(30),
                    statusOf(id)
            );
        }

        private ProductClientStatus statusOf(Long id) {
            return statusesById.getOrDefault(id, defaultStatus);
        }
    }

    private static <T> ApiResponse<T> remoteSuccess(T data) {
        return new ApiResponse<>(Integer.parseInt("200"), "success", data);
    }
}

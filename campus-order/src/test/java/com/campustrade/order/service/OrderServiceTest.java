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
import com.campustrade.order.mapper.OrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderMapper orderMapper;

    private final FakeUserClient userClient = new FakeUserClient();
    private final FakeProductClient productClient = new FakeProductClient();
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(userClient, productClient, orderMapper);
    }

    @Test
    void createUsesAuthenticatedUserAsBuyerAndProductOwnerAsSeller() {
        ApiResponse<OrderResponse> response = orderService.create(orderRequest(99L, 88L, 10L), 2L);

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
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
        assertThat(productClient.statusUpdateProductIds).containsExactly(10L);
        assertThat(productClient.statusUpdateUserIds).containsExactly(1L);
        assertThat(productClient.statusOf(10L)).isEqualTo(ProductClientStatus.SOLD);
    }

    @Test
    void createRejectsMissingAuthenticatedUserId() {
        ApiResponse<OrderResponse> response = orderService.create(orderRequest(2L, 1L, 10L), null);

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("buyer id is required");
        assertThat(response.data()).isNull();
        assertThat(userClient.requestedIds).isEmpty();
        assertThat(productClient.requestedIds).isEmpty();
    }

    @Test
    void createRejectsMissingProductId() {
        ApiResponse<OrderResponse> response = orderService.create(orderRequest(2L, 1L, null), 2L);

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("product id is required");
        assertThat(response.data()).isNull();
        assertThat(userClient.requestedIds).isEmpty();
        assertThat(productClient.requestedIds).isEmpty();
    }

    @Test
    void createRejectsMissingBuyerFromUserService() {
        userClient.missingIds.add(2L);

        ApiResponse<OrderResponse> response = orderService.create(orderRequest(99L, 1L, 10L), 2L);

        assertThat(response.code()).isEqualTo(ResultCode.NOT_FOUND.getCode());
        assertThat(response.message()).isEqualTo("buyer not found");
        assertThat(response.data()).isNull();
        assertThat(userClient.requestedIds).containsExactly(2L);
        assertThat(productClient.requestedIds).isEmpty();
    }

    @Test
    void createRejectsMissingProductFromProductService() {
        productClient.missingIds.add(10L);

        ApiResponse<OrderResponse> response = orderService.create(orderRequest(2L, 1L, 10L), 2L);

        assertThat(response.code()).isEqualTo(ResultCode.NOT_FOUND.getCode());
        assertThat(response.message()).isEqualTo("product not found");
        assertThat(response.data()).isNull();
        assertThat(userClient.requestedIds).containsExactly(2L);
    }

    @Test
    void createRejectsMissingSellerFromUserService() {
        userClient.missingIds.add(1L);

        ApiResponse<OrderResponse> response = orderService.create(orderRequest(2L, 99L, 10L), 2L);

        assertThat(response.code()).isEqualTo(ResultCode.NOT_FOUND.getCode());
        assertThat(response.message()).isEqualTo("seller not found");
        assertThat(response.data()).isNull();
        assertThat(userClient.requestedIds).containsExactly(2L, 1L);
    }

    @Test
    void createRejectsProductThatIsNotOnSale() {
        productClient.defaultStatus = ProductClientStatus.OFF_SALE;

        ApiResponse<OrderResponse> response = orderService.create(orderRequest(2L, 1L, 10L), 2L);

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("product is not on sale");
        assertThat(response.data()).isNull();
    }

    @Test
    void createRejectsBuyerBuyingOwnProductUsingProductOwner() {
        productClient.defaultSellerId = 2L;

        ApiResponse<OrderResponse> response = orderService.create(orderRequest(99L, 1L, 10L), 2L);

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("buyer and seller cannot be the same");
        assertThat(response.data()).isNull();
    }

    @Test
    void createRejectsSecondOrderForSoldProduct() {
        ApiResponse<OrderResponse> first = orderService.create(orderRequest(2L, 1L, 10L), 2L);

        ApiResponse<OrderResponse> second = orderService.create(orderRequest(3L, 1L, 10L), 3L);

        assertThat(first.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(second.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(second.message()).isEqualTo("product is not on sale");
        assertThat(second.data()).isNull();
        assertThat(productClient.statusUpdateProductIds).containsExactly(10L);
        assertThat(productClient.statusUpdateUserIds).containsExactly(1L);
    }

    @Test
    void createReturnsSystemErrorWhenUserServiceThrowsException() {
        userClient.throwException = true;

        ApiResponse<OrderResponse> response = orderService.create(orderRequest(2L, 1L, 10L), 2L);

        assertThat(response.code()).isEqualTo(ResultCode.SYSTEM_ERROR.getCode());
        assertThat(response.message()).isEqualTo("remote service unavailable");
        assertThat(response.data()).isNull();
    }

    @Test
    void createReturnsSystemErrorWhenProductServiceThrowsException() {
        productClient.throwException = true;

        ApiResponse<OrderResponse> response = orderService.create(orderRequest(2L, 1L, 10L), 2L);

        assertThat(response.code()).isEqualTo(ResultCode.SYSTEM_ERROR.getCode());
        assertThat(response.message()).isEqualTo("remote service unavailable");
        assertThat(response.data()).isNull();
    }

    @Test
    void createReturnsSystemErrorWhenProductStatusUpdateFails() {
        productClient.failStatusUpdate = true;

        ApiResponse<OrderResponse> response = orderService.create(orderRequest(2L, 1L, 10L), 2L);

        assertThat(response.code()).isEqualTo(ResultCode.SYSTEM_ERROR.getCode());
        assertThat(response.message()).isEqualTo("product status update failed");
        assertThat(response.data()).isNull();
    }

    @Test
    void findByIdAllowsOrderParticipant() {
        OrderResponse created = orderService.create(orderRequest(2L, 1L, 10L), 2L).data();

        ApiResponse<OrderResponse> buyerResponse = orderService.findById(created.id(), 2L);
        ApiResponse<OrderResponse> sellerResponse = orderService.findById(created.id(), 1L);

        assertThat(buyerResponse.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(sellerResponse.code()).isEqualTo(ResultCode.SUCCESS.getCode());
    }

    @Test
    void findByIdRejectsNonParticipant() {
        OrderResponse created = orderService.create(orderRequest(2L, 1L, 10L), 2L).data();

        ApiResponse<OrderResponse> response = orderService.findById(created.id(), 99L);

        assertThat(response.code()).isEqualTo(ResultCode.FORBIDDEN.getCode());
        assertThat(response.data()).isNull();
    }

    @Test
    void findByIdReturnsNotFoundForMissingOrder() {
        ApiResponse<OrderResponse> response = orderService.findById(99L, 2L);

        assertThat(response.code()).isEqualTo(ResultCode.NOT_FOUND.getCode());
        assertThat(response.message()).isEqualTo("order not found");
        assertThat(response.data()).isNull();
    }

    @Test
    void listByBuyerIdReturnsOrdersSortedByIdAscendingForSameUser() {
        OrderResponse first = orderService.create(orderRequest(2L, 1L, 10L), 2L).data();
        orderService.create(orderRequest(3L, 1L, 11L), 3L);
        OrderResponse second = orderService.create(orderRequest(2L, 1L, 12L), 2L).data();

        ApiResponse<List<OrderResponse>> response = orderService.listByBuyerId(2L, 2L);

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data()).extracting(OrderResponse::id).containsExactly(first.id(), second.id());
    }

    @Test
    void listByBuyerIdRejectsDifferentAuthenticatedUser() {
        ApiResponse<List<OrderResponse>> response = orderService.listByBuyerId(2L, 99L);

        assertThat(response.code()).isEqualTo(ResultCode.FORBIDDEN.getCode());
        assertThat(response.data()).isNull();
    }

    @Test
    void listBySellerIdReturnsOrdersSortedByIdAscendingForSameUser() {
        OrderResponse first = orderService.create(orderRequest(2L, 1L, 10L), 2L).data();
        productClient.sellersById.put(11L, 4L);
        orderService.create(orderRequest(2L, 4L, 11L), 2L);
        OrderResponse second = orderService.create(orderRequest(3L, 1L, 12L), 3L).data();

        ApiResponse<List<OrderResponse>> response = orderService.listBySellerId(1L, 1L);

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data()).extracting(OrderResponse::id).containsExactly(first.id(), second.id());
    }

    @Test
    void listBySellerIdRejectsDifferentAuthenticatedUser() {
        ApiResponse<List<OrderResponse>> response = orderService.listBySellerId(1L, 99L);

        assertThat(response.code()).isEqualTo(ResultCode.FORBIDDEN.getCode());
        assertThat(response.data()).isNull();
    }

    @Test
    void cancelChangesOrderStatusToCancelledForParticipantAndRestoresProductWithSellerHeader() {
        OrderResponse created = orderService.create(orderRequest(2L, 1L, 10L), 2L).data();

        ApiResponse<OrderResponse> response = orderService.cancel(created.id(), 2L);

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data().status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(orderService.findById(created.id(), 2L).data().status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(productClient.statusOf(10L)).isEqualTo(ProductClientStatus.ON_SALE);
        assertThat(productClient.statusUpdateUserIds).containsExactly(1L, 1L);
    }

    @Test
    void cancelRejectsNonParticipant() {
        OrderResponse created = orderService.create(orderRequest(2L, 1L, 10L), 2L).data();

        ApiResponse<OrderResponse> response = orderService.cancel(created.id(), 99L);

        assertThat(response.code()).isEqualTo(ResultCode.FORBIDDEN.getCode());
        assertThat(response.data()).isNull();
        assertThat(productClient.statusOf(10L)).isEqualTo(ProductClientStatus.SOLD);
    }

    @Test
    void cancelReturnsSystemErrorWhenProductRestoreFails() {
        OrderResponse created = orderService.create(orderRequest(2L, 1L, 10L), 2L).data();
        productClient.failStatusUpdate = true;

        ApiResponse<OrderResponse> response = orderService.cancel(created.id(), 1L);

        assertThat(response.code()).isEqualTo(ResultCode.SYSTEM_ERROR.getCode());
        assertThat(response.message()).isEqualTo("product status restore failed");
        assertThat(response.data()).isNull();
        assertThat(orderService.findById(created.id(), 2L).data().status()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void cancelReturnsSystemErrorWhenProductServiceThrowsDuringRestore() {
        OrderResponse created = orderService.create(orderRequest(2L, 1L, 10L), 2L).data();
        productClient.throwException = true;

        ApiResponse<OrderResponse> response = orderService.cancel(created.id(), 2L);

        assertThat(response.code()).isEqualTo(ResultCode.SYSTEM_ERROR.getCode());
        assertThat(response.message()).isEqualTo("remote service unavailable");
        assertThat(response.data()).isNull();
    }

    @Test
    void cancelIsIdempotentWhenAlreadyCancelled() {
        OrderResponse created = orderService.create(orderRequest(2L, 1L, 10L), 2L).data();
        orderService.cancel(created.id(), 2L);

        ApiResponse<OrderResponse> response = orderService.cancel(created.id(), 1L);

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data().status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(productClient.statusOf(10L)).isEqualTo(ProductClientStatus.ON_SALE);
    }

    @Test
    void cancelRejectsCompletedOrder() {
        OrderResponse created = orderService.create(orderRequest(2L, 1L, 10L), 2L).data();
        orderService.complete(created.id(), 1L);

        ApiResponse<OrderResponse> response = orderService.cancel(created.id(), 2L);

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("completed order cannot be cancelled");
        assertThat(response.data()).isNull();
    }

    @Test
    void cancelReturnsNotFoundForMissingOrder() {
        ApiResponse<OrderResponse> response = orderService.cancel(99L, 2L);

        assertThat(response.code()).isEqualTo(ResultCode.NOT_FOUND.getCode());
        assertThat(response.message()).isEqualTo("order not found");
        assertThat(response.data()).isNull();
    }

    @Test
    void completeChangesOrderStatusToCompletedForParticipant() {
        OrderResponse created = orderService.create(orderRequest(2L, 1L, 10L), 2L).data();

        ApiResponse<OrderResponse> response = orderService.complete(created.id(), 1L);

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data().status()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(orderService.findById(created.id(), 2L).data().status()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void completeRejectsNonParticipant() {
        OrderResponse created = orderService.create(orderRequest(2L, 1L, 10L), 2L).data();

        ApiResponse<OrderResponse> response = orderService.complete(created.id(), 99L);

        assertThat(response.code()).isEqualTo(ResultCode.FORBIDDEN.getCode());
        assertThat(response.data()).isNull();
        assertThat(orderService.findById(created.id(), 2L).data().status()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void completeRejectsCancelledOrder() {
        OrderResponse created = orderService.create(orderRequest(2L, 1L, 10L), 2L).data();
        orderService.cancel(created.id(), 2L);

        ApiResponse<OrderResponse> response = orderService.complete(created.id(), 1L);

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("cancelled order cannot be completed");
        assertThat(response.data()).isNull();
    }

    @Test
    void completeReturnsNotFoundForMissingOrder() {
        ApiResponse<OrderResponse> response = orderService.complete(99L, 2L);

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
        private final List<Long> statusUpdateProductIds = new java.util.ArrayList<>();
        private final List<Long> statusUpdateUserIds = new java.util.ArrayList<>();
        private final java.util.Map<Long, Long> sellersById = new java.util.HashMap<>();
        private final java.util.Map<Long, ProductClientStatus> statusesById = new java.util.HashMap<>();
        private Long defaultSellerId = 1L;
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
        public ApiResponse<ProductClientResponse> updateStatus(
                Long id,
                ProductStatusUpdateClientRequest request,
                Long userId
        ) {
            if (throwException) {
                throw new IllegalStateException("product service unavailable");
            }
            if (failStatusUpdate) {
                return ApiResponse.fail(ResultCode.SYSTEM_ERROR, "update failed");
            }
            statusUpdateProductIds.add(id);
            statusUpdateUserIds.add(userId);
            statusesById.put(id, ProductClientStatus.valueOf(request.status()));
            return remoteSuccess(product(id));
        }

        private ProductClientResponse product(Long id) {
            return new ProductClientResponse(
                    id,
                    sellersById.getOrDefault(id, defaultSellerId),
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

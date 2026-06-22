package com.campustrade.message.service;

import com.campustrade.common.response.ApiResponse;
import com.campustrade.common.response.ResultCode;
import com.campustrade.message.client.ProductClient;
import com.campustrade.message.client.UserClient;
import com.campustrade.message.client.dto.ProductClientResponse;
import com.campustrade.message.client.dto.UserProfileClientResponse;
import com.campustrade.message.dto.MessageCreateRequest;
import com.campustrade.message.dto.MessageResponse;
import com.campustrade.message.enums.MessageStatus;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MessageServiceTest {

    private final FakeUserClient userClient = new FakeUserClient();
    private final FakeProductClient productClient = new FakeProductClient();
    private final MessageService messageService = new MessageService(userClient, productClient);

    @Test
    void postCreatesVisibleMessage() {
        ApiResponse<MessageResponse> response = messageService.post(message(100L, 2L, "  在吗，能便宜点吗  "));

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data().id()).isPositive();
        assertThat(response.data())
                .extracting(
                        MessageResponse::productId,
                        MessageResponse::senderId,
                        MessageResponse::content,
                        MessageResponse::status
                )
                .containsExactly(100L, 2L, "在吗，能便宜点吗", MessageStatus.VISIBLE);
        assertThat(userClient.requestedIds).containsExactly(2L);
        assertThat(productClient.requestedIds).containsExactly(100L);
    }

    @Test
    void postRejectsMissingProductId() {
        ApiResponse<MessageResponse> response = messageService.post(message(null, 2L, "hi"));

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("product id is required");
        assertThat(userClient.requestedIds).isEmpty();
        assertThat(productClient.requestedIds).isEmpty();
    }

    @Test
    void postRejectsMissingSenderId() {
        ApiResponse<MessageResponse> response = messageService.post(message(100L, null, "hi"));

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("sender id is required");
    }

    @Test
    void postRejectsBlankContent() {
        ApiResponse<MessageResponse> response = messageService.post(message(100L, 2L, "   "));

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("content is required");
    }

    @Test
    void postRejectsMissingSenderFromUserService() {
        userClient.missingIds.add(2L);

        ApiResponse<MessageResponse> response = messageService.post(message(100L, 2L, "hi"));

        assertThat(response.code()).isEqualTo(ResultCode.NOT_FOUND.getCode());
        assertThat(response.message()).isEqualTo("sender not found");
        assertThat(productClient.requestedIds).isEmpty();
    }

    @Test
    void postRejectsMissingProductFromProductService() {
        productClient.missingIds.add(100L);

        ApiResponse<MessageResponse> response = messageService.post(message(100L, 2L, "hi"));

        assertThat(response.code()).isEqualTo(ResultCode.NOT_FOUND.getCode());
        assertThat(response.message()).isEqualTo("product not found");
    }

    @Test
    void postReturnsSystemErrorWhenUserServiceThrows() {
        userClient.throwException = true;

        ApiResponse<MessageResponse> response = messageService.post(message(100L, 2L, "hi"));

        assertThat(response.code()).isEqualTo(ResultCode.SYSTEM_ERROR.getCode());
        assertThat(response.message()).isEqualTo("remote service unavailable");
    }

    @Test
    void postReturnsSystemErrorWhenProductServiceThrows() {
        productClient.throwException = true;

        ApiResponse<MessageResponse> response = messageService.post(message(100L, 2L, "hi"));

        assertThat(response.code()).isEqualTo(ResultCode.SYSTEM_ERROR.getCode());
        assertThat(response.message()).isEqualTo("remote service unavailable");
    }

    @Test
    void listByProductIdReturnsVisibleMessagesSortedByIdAscending() {
        MessageResponse first = messageService.post(message(100L, 2L, "first")).data();
        MessageResponse hidden = messageService.post(message(100L, 3L, "spam")).data();
        MessageResponse second = messageService.post(message(100L, 4L, "second")).data();
        messageService.post(message(200L, 2L, "other product"));
        messageService.hide(hidden.id());

        ApiResponse<List<MessageResponse>> response = messageService.listByProductId(100L);

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data())
                .extracting(MessageResponse::id)
                .containsExactly(first.id(), second.id());
    }

    @Test
    void listByProductIdRejectsMissingProductId() {
        ApiResponse<List<MessageResponse>> response = messageService.listByProductId(null);

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("product id is required");
    }

    @Test
    void hideChangesStatusToHidden() {
        MessageResponse created = messageService.post(message(100L, 2L, "hi")).data();

        ApiResponse<MessageResponse> response = messageService.hide(created.id());

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data().status()).isEqualTo(MessageStatus.HIDDEN);
        assertThat(messageService.listByProductId(100L).data()).isEmpty();
    }

    @Test
    void hideIsIdempotentWhenAlreadyHidden() {
        MessageResponse created = messageService.post(message(100L, 2L, "hi")).data();
        messageService.hide(created.id());

        ApiResponse<MessageResponse> response = messageService.hide(created.id());

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data().status()).isEqualTo(MessageStatus.HIDDEN);
    }

    @Test
    void hideReturnsNotFoundForMissingMessage() {
        ApiResponse<MessageResponse> response = messageService.hide(99L);

        assertThat(response.code()).isEqualTo(ResultCode.NOT_FOUND.getCode());
        assertThat(response.message()).isEqualTo("message not found");
    }

    @Test
    void deleteRemovesMessage() {
        MessageResponse created = messageService.post(message(100L, 2L, "hi")).data();

        ApiResponse<Void> response = messageService.delete(created.id());

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(messageService.listByProductId(100L).data()).isEmpty();
        assertThat(messageService.hide(created.id()).code()).isEqualTo(ResultCode.NOT_FOUND.getCode());
    }

    @Test
    void deleteReturnsNotFoundForMissingMessage() {
        ApiResponse<Void> response = messageService.delete(99L);

        assertThat(response.code()).isEqualTo(ResultCode.NOT_FOUND.getCode());
        assertThat(response.message()).isEqualTo("message not found");
    }

    private MessageCreateRequest message(Long productId, Long senderId, String content) {
        return new MessageCreateRequest(productId, senderId, content);
    }

    private static class FakeUserClient implements UserClient {

        private final List<Long> requestedIds = new ArrayList<>();
        private final List<Long> missingIds = new ArrayList<>();
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
            return new ApiResponse<>(
                    ResultCode.SUCCESS.getCode(),
                    "success",
                    new UserProfileClientResponse(id, "user" + id, "User " + id)
            );
        }
    }

    private static class FakeProductClient implements ProductClient {

        private final List<Long> requestedIds = new ArrayList<>();
        private final List<Long> missingIds = new ArrayList<>();
        private boolean throwException;

        @Override
        public ApiResponse<ProductClientResponse> findById(Long id) {
            if (throwException) {
                throw new IllegalStateException("product service unavailable");
            }
            requestedIds.add(id);
            if (missingIds.contains(id)) {
                return ApiResponse.fail(ResultCode.NOT_FOUND, "product not found");
            }
            return new ApiResponse<>(
                    ResultCode.SUCCESS.getCode(),
                    "success",
                    new ProductClientResponse(id, "Product " + id)
            );
        }
    }
}

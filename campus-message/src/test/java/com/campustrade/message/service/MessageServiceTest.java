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
import com.campustrade.message.mapper.MessageMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class MessageServiceTest {

    @Autowired
    private MessageMapper messageMapper;

    private final FakeUserClient userClient = new FakeUserClient();
    private final FakeProductClient productClient = new FakeProductClient();
    private MessageService messageService;

    @BeforeEach
    void setUp() {
        // 真实 MessageMapper（参与测试事务，跑 H2）+ 假的 Feign client 组装 service，
        // 避免与 Spring Cloud 默认 @Primary 的 Feign client bean 冲突。
        messageService = new MessageService(userClient, productClient, messageMapper);
    }

    @Test
    void postCreatesVisibleMessage() {
        ApiResponse<MessageResponse> response = messageService.post(message(100L, 2L, "  在吗，能便宜点吗  "), 2L);

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
    void postUsesAuthenticatedUserIdAndIgnoresRequestSenderId() {
        ApiResponse<MessageResponse> response = messageService.post(message(100L, 99L, "hi"), 2L);

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data().senderId()).isEqualTo(2L);
        assertThat(userClient.requestedIds).containsExactly(2L);
    }

    @Test
    void postRejectsMissingProductId() {
        ApiResponse<MessageResponse> response = messageService.post(message(null, 2L, "hi"), 2L);

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("product id is required");
        assertThat(userClient.requestedIds).isEmpty();
        assertThat(productClient.requestedIds).isEmpty();
    }

    @Test
    void postRejectsMissingAuthenticatedUserId() {
        ApiResponse<MessageResponse> response = messageService.post(message(100L, 2L, "hi"), null);

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("sender id is required");
    }

    @Test
    void postRejectsBlankContent() {
        ApiResponse<MessageResponse> response = messageService.post(message(100L, 2L, "   "), 2L);

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("content is required");
    }

    @Test
    void postRejectsMissingSenderFromUserService() {
        userClient.missingIds.add(2L);

        ApiResponse<MessageResponse> response = messageService.post(message(100L, 99L, "hi"), 2L);

        assertThat(response.code()).isEqualTo(ResultCode.NOT_FOUND.getCode());
        assertThat(response.message()).isEqualTo("sender not found");
        assertThat(productClient.requestedIds).isEmpty();
    }

    @Test
    void postRejectsMissingProductFromProductService() {
        productClient.missingIds.add(100L);

        ApiResponse<MessageResponse> response = messageService.post(message(100L, 2L, "hi"), 2L);

        assertThat(response.code()).isEqualTo(ResultCode.NOT_FOUND.getCode());
        assertThat(response.message()).isEqualTo("product not found");
    }

    @Test
    void postReturnsSystemErrorWhenUserServiceThrows() {
        userClient.throwException = true;

        ApiResponse<MessageResponse> response = messageService.post(message(100L, 2L, "hi"), 2L);

        assertThat(response.code()).isEqualTo(ResultCode.SYSTEM_ERROR.getCode());
        assertThat(response.message()).isEqualTo("remote service unavailable");
    }

    @Test
    void postReturnsSystemErrorWhenProductServiceThrows() {
        productClient.throwException = true;

        ApiResponse<MessageResponse> response = messageService.post(message(100L, 2L, "hi"), 2L);

        assertThat(response.code()).isEqualTo(ResultCode.SYSTEM_ERROR.getCode());
        assertThat(response.message()).isEqualTo("remote service unavailable");
    }

    @Test
    void listByProductIdReturnsVisibleMessagesSortedByIdAscending() {
        MessageResponse first = messageService.post(message(100L, 2L, "first"), 2L).data();
        MessageResponse hidden = messageService.post(message(100L, 3L, "spam"), 3L).data();
        MessageResponse second = messageService.post(message(100L, 4L, "second"), 4L).data();
        messageService.post(message(200L, 2L, "other product"), 2L);
        messageService.hide(hidden.id(), 3L);

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
        MessageResponse created = messageService.post(message(100L, 2L, "hi"), 2L).data();

        ApiResponse<MessageResponse> response = messageService.hide(created.id(), 2L);

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data().status()).isEqualTo(MessageStatus.HIDDEN);
        assertThat(messageService.listByProductId(100L).data()).isEmpty();
    }

    @Test
    void hideIsIdempotentWhenAlreadyHidden() {
        MessageResponse created = messageService.post(message(100L, 2L, "hi"), 2L).data();
        messageService.hide(created.id(), 2L);

        ApiResponse<MessageResponse> response = messageService.hide(created.id(), 2L);

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data().status()).isEqualTo(MessageStatus.HIDDEN);
    }

    @Test
    void hideReturnsNotFoundForMissingMessage() {
        ApiResponse<MessageResponse> response = messageService.hide(99L, 2L);

        assertThat(response.code()).isEqualTo(ResultCode.NOT_FOUND.getCode());
        assertThat(response.message()).isEqualTo("message not found");
    }

    @Test
    void hideRejectsNonSender() {
        MessageResponse created = messageService.post(message(100L, 2L, "hi"), 2L).data();

        ApiResponse<MessageResponse> response = messageService.hide(created.id(), 3L);

        assertThat(response.code()).isEqualTo(ResultCode.FORBIDDEN.getCode());
        assertThat(response.message()).isEqualTo("only message sender can modify this message");
        assertThat(messageService.listByProductId(100L).data())
                .extracting(MessageResponse::id)
                .containsExactly(created.id());
    }

    @Test
    void deleteRemovesMessageForSender() {
        MessageResponse created = messageService.post(message(100L, 2L, "hi"), 2L).data();

        ApiResponse<Void> response = messageService.delete(created.id(), 2L);

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(messageService.listByProductId(100L).data()).isEmpty();
        assertThat(messageService.hide(created.id(), 2L).code()).isEqualTo(ResultCode.NOT_FOUND.getCode());
    }

    @Test
    void deleteReturnsNotFoundForMissingMessage() {
        ApiResponse<Void> response = messageService.delete(99L, 2L);

        assertThat(response.code()).isEqualTo(ResultCode.NOT_FOUND.getCode());
        assertThat(response.message()).isEqualTo("message not found");
    }

    @Test
    void deleteRejectsNonSender() {
        MessageResponse created = messageService.post(message(100L, 2L, "hi"), 2L).data();

        ApiResponse<Void> response = messageService.delete(created.id(), 3L);

        assertThat(response.code()).isEqualTo(ResultCode.FORBIDDEN.getCode());
        assertThat(response.message()).isEqualTo("only message sender can modify this message");
        assertThat(messageService.listByProductId(100L).data())
                .extracting(MessageResponse::id)
                .containsExactly(created.id());
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

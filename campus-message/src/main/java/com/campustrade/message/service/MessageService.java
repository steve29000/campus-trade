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
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MessageService {

    private final UserClient userClient;
    private final ProductClient productClient;
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Map<Long, MessageResponse> messagesById = new ConcurrentHashMap<>();

    public MessageService(UserClient userClient, ProductClient productClient) {
        this.userClient = userClient;
        this.productClient = productClient;
    }

    public ApiResponse<MessageResponse> post(MessageCreateRequest request) {
        if (request == null || request.productId() == null) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "product id is required");
        }
        if (request.senderId() == null) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "sender id is required");
        }
        if (isBlank(request.content())) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "content is required");
        }

        ApiResponse<UserProfileClientResponse> senderResponse;
        try {
            senderResponse = userClient.findProfile(request.senderId());
        } catch (RuntimeException exception) {
            return ApiResponse.fail(ResultCode.SYSTEM_ERROR, "remote service unavailable");
        }
        if (!isSuccessWithData(senderResponse)) {
            return ApiResponse.fail(ResultCode.NOT_FOUND, "sender not found");
        }

        ApiResponse<ProductClientResponse> productResponse;
        try {
            productResponse = productClient.findById(request.productId());
        } catch (RuntimeException exception) {
            return ApiResponse.fail(ResultCode.SYSTEM_ERROR, "remote service unavailable");
        }
        if (!isSuccessWithData(productResponse)) {
            return ApiResponse.fail(ResultCode.NOT_FOUND, "product not found");
        }

        Long id = idGenerator.getAndIncrement();
        MessageResponse message = new MessageResponse(
                id,
                request.productId(),
                request.senderId(),
                request.content().trim(),
                MessageStatus.VISIBLE
        );
        messagesById.put(id, message);

        return ApiResponse.success(message);
    }

    public ApiResponse<List<MessageResponse>> listByProductId(Long productId) {
        if (productId == null) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "product id is required");
        }

        List<MessageResponse> messages = messagesById.values().stream()
                .filter(message -> message.productId().equals(productId))
                .filter(message -> message.status() == MessageStatus.VISIBLE)
                .sorted(Comparator.comparing(MessageResponse::id))
                .toList();

        return ApiResponse.success(messages);
    }

    public ApiResponse<MessageResponse> hide(Long id) {
        MessageResponse message = messagesById.get(id);
        if (message == null) {
            return ApiResponse.fail(ResultCode.NOT_FOUND, "message not found");
        }
        if (message.status() == MessageStatus.HIDDEN) {
            return ApiResponse.success(message);
        }

        MessageResponse updated = new MessageResponse(
                message.id(),
                message.productId(),
                message.senderId(),
                message.content(),
                MessageStatus.HIDDEN
        );
        messagesById.put(id, updated);

        return ApiResponse.success(updated);
    }

    public ApiResponse<Void> delete(Long id) {
        MessageResponse removed = messagesById.remove(id);
        if (removed == null) {
            return ApiResponse.fail(ResultCode.NOT_FOUND, "message not found");
        }

        return ApiResponse.success();
    }

    private boolean isSuccessWithData(ApiResponse<?> response) {
        return response != null
                && ResultCode.SUCCESS.getCode().equals(response.code())
                && response.data() != null;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

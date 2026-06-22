package com.campustrade.message.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campustrade.common.response.ApiResponse;
import com.campustrade.common.response.ResultCode;
import com.campustrade.message.client.ProductClient;
import com.campustrade.message.client.UserClient;
import com.campustrade.message.client.dto.ProductClientResponse;
import com.campustrade.message.client.dto.UserProfileClientResponse;
import com.campustrade.message.dto.MessageCreateRequest;
import com.campustrade.message.dto.MessageResponse;
import com.campustrade.message.entity.MessageEntity;
import com.campustrade.message.enums.MessageStatus;
import com.campustrade.message.mapper.MessageMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    private final UserClient userClient;
    private final ProductClient productClient;
    private final MessageMapper messageMapper;

    public MessageService(UserClient userClient, ProductClient productClient, MessageMapper messageMapper) {
        this.userClient = userClient;
        this.productClient = productClient;
        this.messageMapper = messageMapper;
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

        MessageEntity message = new MessageEntity();
        message.setProductId(request.productId());
        message.setSenderId(request.senderId());
        message.setContent(request.content().trim());
        message.setStatus(MessageStatus.VISIBLE);
        messageMapper.insert(message);

        return ApiResponse.success(toResponse(message));
    }

    public ApiResponse<List<MessageResponse>> listByProductId(Long productId) {
        if (productId == null) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "product id is required");
        }

        List<MessageResponse> messages = messageMapper.selectList(
                        new LambdaQueryWrapper<MessageEntity>()
                                .eq(MessageEntity::getProductId, productId)
                                .eq(MessageEntity::getStatus, MessageStatus.VISIBLE)
                                .orderByAsc(MessageEntity::getId))
                .stream()
                .map(this::toResponse)
                .toList();

        return ApiResponse.success(messages);
    }

    public ApiResponse<MessageResponse> hide(Long id) {
        MessageEntity message = messageMapper.selectById(id);
        if (message == null) {
            return ApiResponse.fail(ResultCode.NOT_FOUND, "message not found");
        }
        if (message.getStatus() == MessageStatus.HIDDEN) {
            return ApiResponse.success(toResponse(message));
        }

        message.setStatus(MessageStatus.HIDDEN);
        messageMapper.updateById(message);

        return ApiResponse.success(toResponse(message));
    }

    public ApiResponse<Void> delete(Long id) {
        int removed = messageMapper.deleteById(id);
        if (removed == 0) {
            return ApiResponse.fail(ResultCode.NOT_FOUND, "message not found");
        }

        return ApiResponse.success();
    }

    private boolean isSuccessWithData(ApiResponse<?> response) {
        return response != null
                && ResultCode.SUCCESS.getCode().equals(response.code())
                && response.data() != null;
    }

    private MessageResponse toResponse(MessageEntity message) {
        return new MessageResponse(
                message.getId(),
                message.getProductId(),
                message.getSenderId(),
                message.getContent(),
                message.getStatus()
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

package com.campustrade.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.campustrade.order.entity.OrderEntity;
import com.campustrade.order.enums.OrderStatus;
import com.campustrade.order.mapper.OrderMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final UserClient userClient;
    private final ProductClient productClient;
    private final OrderMapper orderMapper;

    public OrderService(UserClient userClient, ProductClient productClient, OrderMapper orderMapper) {
        this.userClient = userClient;
        this.productClient = productClient;
        this.orderMapper = orderMapper;
    }

    public ApiResponse<OrderResponse> create(OrderCreateRequest request) {
        ApiResponse<OrderResponse> validation = validateCreateRequest(request);
        if (validation != null) {
            return validation;
        }

        ApiResponse<UserProfileClientResponse> buyerResponse;
        try {
            buyerResponse = userClient.findProfile(request.buyerId());
        } catch (RuntimeException exception) {
            return ApiResponse.fail(ResultCode.SYSTEM_ERROR, "remote service unavailable");
        }
        if (!isSuccessWithData(buyerResponse)) {
            return ApiResponse.fail(ResultCode.NOT_FOUND, "buyer not found");
        }

        ApiResponse<UserProfileClientResponse> sellerResponse;
        try {
            sellerResponse = userClient.findProfile(request.sellerId());
        } catch (RuntimeException exception) {
            return ApiResponse.fail(ResultCode.SYSTEM_ERROR, "remote service unavailable");
        }
        if (!isSuccessWithData(sellerResponse)) {
            return ApiResponse.fail(ResultCode.NOT_FOUND, "seller not found");
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

        ProductClientResponse product = productResponse.data();
        if (product.status() != ProductClientStatus.ON_SALE) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "product is not on sale");
        }
        if (!request.sellerId().equals(product.sellerId())) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "seller does not match product owner");
        }

        ApiResponse<ProductClientResponse> soldResponse;
        try {
            soldResponse = productClient.updateStatus(
                    request.productId(),
                    new ProductStatusUpdateClientRequest(ProductClientStatus.SOLD.name())
            );
        } catch (RuntimeException exception) {
            return ApiResponse.fail(ResultCode.SYSTEM_ERROR, "remote service unavailable");
        }
        if (!isSuccessWithData(soldResponse)) {
            return ApiResponse.fail(ResultCode.SYSTEM_ERROR, "product status update failed");
        }

        OrderEntity order = new OrderEntity();
        order.setBuyerId(request.buyerId());
        order.setSellerId(request.sellerId());
        order.setProductId(request.productId());
        order.setProductTitle(product.title());
        order.setPrice(product.price());
        order.setStatus(OrderStatus.CREATED);
        orderMapper.insert(order);

        return ApiResponse.success(toResponse(order));
    }

    public ApiResponse<OrderResponse> findById(Long id) {
        OrderEntity order = orderMapper.selectById(id);
        if (order == null) {
            return ApiResponse.fail(ResultCode.NOT_FOUND, "order not found");
        }

        return ApiResponse.success(toResponse(order));
    }

    public ApiResponse<List<OrderResponse>> listByBuyerId(Long buyerId) {
        if (buyerId == null) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "buyer id is required");
        }

        List<OrderResponse> orders = orderMapper.selectList(
                        new LambdaQueryWrapper<OrderEntity>()
                                .eq(OrderEntity::getBuyerId, buyerId)
                                .orderByAsc(OrderEntity::getId))
                .stream()
                .map(this::toResponse)
                .toList();

        return ApiResponse.success(orders);
    }

    public ApiResponse<List<OrderResponse>> listBySellerId(Long sellerId) {
        if (sellerId == null) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "seller id is required");
        }

        List<OrderResponse> orders = orderMapper.selectList(
                        new LambdaQueryWrapper<OrderEntity>()
                                .eq(OrderEntity::getSellerId, sellerId)
                                .orderByAsc(OrderEntity::getId))
                .stream()
                .map(this::toResponse)
                .toList();

        return ApiResponse.success(orders);
    }

    public ApiResponse<OrderResponse> cancel(Long id) {
        OrderEntity order = orderMapper.selectById(id);
        if (order == null) {
            return ApiResponse.fail(ResultCode.NOT_FOUND, "order not found");
        }
        if (order.getStatus() == OrderStatus.COMPLETED) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "completed order cannot be cancelled");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            // 已取消则幂等返回，避免重复把商品状态改回 ON_SALE。
            return ApiResponse.success(toResponse(order));
        }

        // 取消订单时把商品状态从 SOLD 回滚为 ON_SALE，让商品可以被再次购买。
        ApiResponse<ProductClientResponse> restoreResponse;
        try {
            restoreResponse = productClient.updateStatus(
                    order.getProductId(),
                    new ProductStatusUpdateClientRequest(ProductClientStatus.ON_SALE.name())
            );
        } catch (RuntimeException exception) {
            return ApiResponse.fail(ResultCode.SYSTEM_ERROR, "remote service unavailable");
        }
        if (!isSuccessWithData(restoreResponse)) {
            return ApiResponse.fail(ResultCode.SYSTEM_ERROR, "product status restore failed");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderMapper.updateById(order);

        return ApiResponse.success(toResponse(order));
    }

    public ApiResponse<OrderResponse> complete(Long id) {
        OrderEntity order = orderMapper.selectById(id);
        if (order == null) {
            return ApiResponse.fail(ResultCode.NOT_FOUND, "order not found");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "cancelled order cannot be completed");
        }

        order.setStatus(OrderStatus.COMPLETED);
        orderMapper.updateById(order);

        return ApiResponse.success(toResponse(order));
    }

    private ApiResponse<OrderResponse> validateCreateRequest(OrderCreateRequest request) {
        if (request == null || request.buyerId() == null) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "buyer id is required");
        }
        if (request.sellerId() == null) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "seller id is required");
        }
        if (request.buyerId().equals(request.sellerId())) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "buyer and seller cannot be the same");
        }
        if (request.productId() == null) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "product id is required");
        }

        return null;
    }

    private boolean isSuccessWithData(ApiResponse<?> response) {
        return response != null
                && ResultCode.SUCCESS.getCode().equals(response.code())
                && response.data() != null;
    }

    private OrderResponse toResponse(OrderEntity order) {
        return new OrderResponse(
                order.getId(),
                order.getBuyerId(),
                order.getSellerId(),
                order.getProductId(),
                order.getProductTitle(),
                order.getPrice(),
                order.getStatus()
        );
    }

}

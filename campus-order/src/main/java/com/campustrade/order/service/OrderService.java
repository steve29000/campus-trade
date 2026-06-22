package com.campustrade.order.service;

import com.campustrade.common.response.ApiResponse;
import com.campustrade.common.response.ResultCode;
import com.campustrade.order.dto.OrderCreateRequest;
import com.campustrade.order.dto.OrderResponse;
import com.campustrade.order.enums.OrderStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderService {

    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Map<Long, OrderResponse> ordersById = new ConcurrentHashMap<>();

    public ApiResponse<OrderResponse> create(OrderCreateRequest request) {
        ApiResponse<OrderResponse> validation = validateCreateRequest(request);
        if (validation != null) {
            return validation;
        }

        Long id = idGenerator.getAndIncrement();
        OrderResponse order = new OrderResponse(
                id,
                request.buyerId(),
                request.sellerId(),
                request.productId(),
                request.productTitle().trim(),
                request.price(),
                OrderStatus.CREATED
        );
        ordersById.put(id, order);

        return ApiResponse.success(order);
    }

    public ApiResponse<OrderResponse> findById(Long id) {
        OrderResponse order = ordersById.get(id);
        if (order == null) {
            return ApiResponse.fail(ResultCode.NOT_FOUND, "order not found");
        }

        return ApiResponse.success(order);
    }

    public ApiResponse<List<OrderResponse>> listByBuyerId(Long buyerId) {
        if (buyerId == null) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "buyer id is required");
        }

        List<OrderResponse> orders = ordersById.values().stream()
                .filter(order -> order.buyerId().equals(buyerId))
                .sorted(Comparator.comparing(OrderResponse::id))
                .toList();

        return ApiResponse.success(orders);
    }

    public ApiResponse<List<OrderResponse>> listBySellerId(Long sellerId) {
        if (sellerId == null) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "seller id is required");
        }

        List<OrderResponse> orders = ordersById.values().stream()
                .filter(order -> order.sellerId().equals(sellerId))
                .sorted(Comparator.comparing(OrderResponse::id))
                .toList();

        return ApiResponse.success(orders);
    }

    public ApiResponse<OrderResponse> cancel(Long id) {
        OrderResponse order = ordersById.get(id);
        if (order == null) {
            return ApiResponse.fail(ResultCode.NOT_FOUND, "order not found");
        }
        if (order.status() == OrderStatus.COMPLETED) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "completed order cannot be cancelled");
        }

        OrderResponse updated = withStatus(order, OrderStatus.CANCELLED);
        ordersById.put(id, updated);

        return ApiResponse.success(updated);
    }

    public ApiResponse<OrderResponse> complete(Long id) {
        OrderResponse order = ordersById.get(id);
        if (order == null) {
            return ApiResponse.fail(ResultCode.NOT_FOUND, "order not found");
        }
        if (order.status() == OrderStatus.CANCELLED) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "cancelled order cannot be completed");
        }

        OrderResponse updated = withStatus(order, OrderStatus.COMPLETED);
        ordersById.put(id, updated);

        return ApiResponse.success(updated);
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
        if (isBlank(request.productTitle())) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "product title is required");
        }
        if (request.price() == null || request.price().compareTo(BigDecimal.ZERO) < 0) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "price must be greater than or equal to 0");
        }

        return null;
    }

    private OrderResponse withStatus(OrderResponse order, OrderStatus status) {
        return new OrderResponse(
                order.id(),
                order.buyerId(),
                order.sellerId(),
                order.productId(),
                order.productTitle(),
                order.price(),
                status
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

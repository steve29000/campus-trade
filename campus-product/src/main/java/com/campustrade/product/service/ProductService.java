package com.campustrade.product.service;

import com.campustrade.common.response.ApiResponse;
import com.campustrade.common.response.ResultCode;
import com.campustrade.product.dto.ProductCreateRequest;
import com.campustrade.product.dto.ProductResponse;
import com.campustrade.product.dto.ProductStatusUpdateRequest;
import com.campustrade.product.enums.ProductStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProductService {

    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Map<Long, ProductResponse> productsById = new ConcurrentHashMap<>();

    public ApiResponse<ProductResponse> publish(ProductCreateRequest request) {
        if (request == null || request.sellerId() == null) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "seller id is required");
        }
        if (isBlank(request.title()) || isBlank(request.description()) || isBlank(request.category())) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "title, description and category are required");
        }
        if (request.price() == null || request.price().compareTo(BigDecimal.ZERO) < 0) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "price must be greater than or equal to 0");
        }

        Long id = idGenerator.getAndIncrement();
        ProductResponse product = new ProductResponse(
                id,
                request.sellerId(),
                request.title().trim(),
                request.description().trim(),
                request.category().trim(),
                request.price(),
                ProductStatus.ON_SALE
        );
        productsById.put(id, product);

        return ApiResponse.success(product);
    }

    public ApiResponse<List<ProductResponse>> list(String keyword, String category, String status) {
        ProductStatus parsedStatus = null;
        if (!isBlank(status)) {
            parsedStatus = parseStatus(status);
            if (parsedStatus == null) {
                return ApiResponse.fail(ResultCode.BAD_REQUEST, "invalid product status");
            }
        }

        String normalizedKeyword = isBlank(keyword) ? null : keyword.trim().toLowerCase(Locale.ROOT);
        String trimmedCategory = isBlank(category) ? null : category.trim();
        ProductStatus statusFilter = parsedStatus;

        List<ProductResponse> products = productsById.values().stream()
                .filter(product -> matchesKeyword(product, normalizedKeyword))
                .filter(product -> trimmedCategory == null || product.category().equals(trimmedCategory))
                .filter(product -> statusFilter == null || product.status() == statusFilter)
                .sorted(Comparator.comparing(ProductResponse::id))
                .toList();

        return ApiResponse.success(products);
    }

    public ApiResponse<ProductResponse> findById(Long id) {
        ProductResponse product = productsById.get(id);
        if (product == null) {
            return ApiResponse.fail(ResultCode.NOT_FOUND, "product not found");
        }

        return ApiResponse.success(product);
    }

    public ApiResponse<ProductResponse> updateStatus(Long id, ProductStatusUpdateRequest request) {
        ProductStatus status = request == null ? null : parseStatus(request.status());
        if (status == null) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "invalid product status");
        }

        ProductResponse product = productsById.get(id);
        if (product == null) {
            return ApiResponse.fail(ResultCode.NOT_FOUND, "product not found");
        }

        ProductResponse updated = new ProductResponse(
                product.id(),
                product.sellerId(),
                product.title(),
                product.description(),
                product.category(),
                product.price(),
                status
        );
        productsById.put(id, updated);

        return ApiResponse.success(updated);
    }

    private boolean matchesKeyword(ProductResponse product, String keyword) {
        if (keyword == null) {
            return true;
        }

        return product.title().toLowerCase(Locale.ROOT).contains(keyword)
                || product.description().toLowerCase(Locale.ROOT).contains(keyword);
    }

    private ProductStatus parseStatus(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return ProductStatus.valueOf(value.trim());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

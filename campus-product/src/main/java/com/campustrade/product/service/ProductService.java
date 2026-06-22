package com.campustrade.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campustrade.common.response.ApiResponse;
import com.campustrade.common.response.ResultCode;
import com.campustrade.product.client.AiClient;
import com.campustrade.product.client.dto.ContentCheckClientRequest;
import com.campustrade.product.client.dto.ContentCheckClientResponse;
import com.campustrade.product.dto.ProductCreateRequest;
import com.campustrade.product.dto.ProductResponse;
import com.campustrade.product.dto.ProductStatusUpdateRequest;
import com.campustrade.product.entity.ProductEntity;
import com.campustrade.product.enums.ProductStatus;
import com.campustrade.product.mapper.ProductMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
public class ProductService {

    private final AiClient aiClient;
    private final ProductMapper productMapper;

    public ProductService(AiClient aiClient, ProductMapper productMapper) {
        this.aiClient = aiClient;
        this.productMapper = productMapper;
    }

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

        // 发布前调用 campus-ai 做内容安全检查，命中违规关键词时拒绝发布。
        ApiResponse<ProductResponse> contentRejection = checkContent(request);
        if (contentRejection != null) {
            return contentRejection;
        }

        ProductEntity product = new ProductEntity();
        product.setSellerId(request.sellerId());
        product.setTitle(request.title().trim());
        product.setDescription(request.description().trim());
        product.setCategory(request.category().trim());
        product.setPrice(request.price());
        product.setStatus(ProductStatus.ON_SALE);
        productMapper.insert(product);

        return ApiResponse.success(toResponse(product));
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

        LambdaQueryWrapper<ProductEntity> wrapper = new LambdaQueryWrapper<>();
        if (normalizedKeyword != null) {
            // 标题或描述大小写不敏感地包含关键词（LOWER 在 MySQL 与 H2 下行为一致）。
            String pattern = "%" + normalizedKeyword + "%";
            wrapper.and(inner -> inner
                    .apply("LOWER(title) LIKE {0}", pattern)
                    .or()
                    .apply("LOWER(description) LIKE {0}", pattern));
        }
        if (trimmedCategory != null) {
            wrapper.eq(ProductEntity::getCategory, trimmedCategory);
        }
        if (parsedStatus != null) {
            wrapper.eq(ProductEntity::getStatus, parsedStatus);
        }
        wrapper.orderByAsc(ProductEntity::getId);

        List<ProductResponse> products = productMapper.selectList(wrapper).stream()
                .map(this::toResponse)
                .toList();

        return ApiResponse.success(products);
    }

    public ApiResponse<ProductResponse> findById(Long id) {
        ProductEntity product = productMapper.selectById(id);
        if (product == null) {
            return ApiResponse.fail(ResultCode.NOT_FOUND, "product not found");
        }

        return ApiResponse.success(toResponse(product));
    }

    public ApiResponse<ProductResponse> updateStatus(Long id, ProductStatusUpdateRequest request) {
        ProductStatus status = request == null ? null : parseStatus(request.status());
        if (status == null) {
            return ApiResponse.fail(ResultCode.BAD_REQUEST, "invalid product status");
        }

        ProductEntity product = productMapper.selectById(id);
        if (product == null) {
            return ApiResponse.fail(ResultCode.NOT_FOUND, "product not found");
        }

        product.setStatus(status);
        productMapper.updateById(product);

        return ApiResponse.success(toResponse(product));
    }

    /**
     * 调用 campus-ai 内容检查。通过返回 {@code null}；命中违规或远程异常时返回对应的失败响应。
     */
    private ApiResponse<ProductResponse> checkContent(ProductCreateRequest request) {
        String content = request.title().trim() + " " + request.description().trim();

        ApiResponse<ContentCheckClientResponse> response;
        try {
            response = aiClient.checkContent(new ContentCheckClientRequest(content));
        } catch (RuntimeException exception) {
            return ApiResponse.fail(ResultCode.SYSTEM_ERROR, "remote service unavailable");
        }

        boolean successWithData = response != null
                && ResultCode.SUCCESS.getCode().equals(response.code())
                && response.data() != null;
        if (!successWithData) {
            return ApiResponse.fail(ResultCode.SYSTEM_ERROR, "content check failed");
        }

        ContentCheckClientResponse result = response.data();
        if (!Boolean.TRUE.equals(result.passed())) {
            String reason = isBlank(result.reason()) ? "content rejected by safety check" : result.reason();
            return ApiResponse.fail(ResultCode.FORBIDDEN, reason);
        }

        return null;
    }

    private ProductResponse toResponse(ProductEntity product) {
        return new ProductResponse(
                product.getId(),
                product.getSellerId(),
                product.getTitle(),
                product.getDescription(),
                product.getCategory(),
                product.getPrice(),
                product.getStatus()
        );
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

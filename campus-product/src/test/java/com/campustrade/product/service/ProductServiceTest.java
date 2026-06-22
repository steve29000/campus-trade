package com.campustrade.product.service;

import com.campustrade.common.response.ApiResponse;
import com.campustrade.common.response.ResultCode;
import com.campustrade.product.client.AiClient;
import com.campustrade.product.client.dto.ContentCheckClientRequest;
import com.campustrade.product.client.dto.ContentCheckClientResponse;
import com.campustrade.product.dto.ProductCreateRequest;
import com.campustrade.product.dto.ProductResponse;
import com.campustrade.product.dto.ProductStatusUpdateRequest;
import com.campustrade.product.enums.ProductStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductServiceTest {

    private final FakeAiClient aiClient = new FakeAiClient();
    private final ProductService productService = new ProductService(aiClient);

    @Test
    void publishCreatesProductOnSale() {
        ApiResponse<ProductResponse> response = productService.publish(
                new ProductCreateRequest(1L, "iPad Air", "Used for one year", "Digital", BigDecimal.valueOf(2800))
        );

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.message()).isEqualTo(ResultCode.SUCCESS.getMessage());
        assertThat(response.data().id()).isPositive();
        assertThat(response.data())
                .extracting(
                        ProductResponse::sellerId,
                        ProductResponse::title,
                        ProductResponse::description,
                        ProductResponse::category,
                        ProductResponse::price,
                        ProductResponse::status
                )
                .containsExactly(1L, "iPad Air", "Used for one year", "Digital", BigDecimal.valueOf(2800),
                        ProductStatus.ON_SALE);
        assertThat(aiClient.lastContent).isEqualTo("iPad Air Used for one year");
    }

    @Test
    void publishRejectsContentThatFailsSafetyCheck() {
        aiClient.passed = false;
        aiClient.reason = "content contains prohibited keyword: 枪";

        ApiResponse<ProductResponse> response = productService.publish(
                new ProductCreateRequest(1L, "出售仿真枪", "全新仿真枪一把", "Other", BigDecimal.valueOf(100))
        );

        assertThat(response.code()).isEqualTo(ResultCode.FORBIDDEN.getCode());
        assertThat(response.message()).isEqualTo("content contains prohibited keyword: 枪");
        assertThat(response.data()).isNull();
        assertThat(productService.list(null, null, null).data()).isEmpty();
    }

    @Test
    void publishReturnsSystemErrorWhenAiServiceThrows() {
        aiClient.throwException = true;

        ApiResponse<ProductResponse> response = productService.publish(
                new ProductCreateRequest(1L, "iPad Air", "Used for one year", "Digital", BigDecimal.valueOf(2800))
        );

        assertThat(response.code()).isEqualTo(ResultCode.SYSTEM_ERROR.getCode());
        assertThat(response.message()).isEqualTo("remote service unavailable");
        assertThat(response.data()).isNull();
    }

    @Test
    void publishReturnsSystemErrorWhenContentCheckResponseHasNoData() {
        aiClient.returnFailure = true;

        ApiResponse<ProductResponse> response = productService.publish(
                new ProductCreateRequest(1L, "iPad Air", "Used for one year", "Digital", BigDecimal.valueOf(2800))
        );

        assertThat(response.code()).isEqualTo(ResultCode.SYSTEM_ERROR.getCode());
        assertThat(response.message()).isEqualTo("content check failed");
        assertThat(response.data()).isNull();
    }

    @Test
    void publishSkipsContentCheckWhenValidationFails() {
        ApiResponse<ProductResponse> response = productService.publish(
                new ProductCreateRequest(null, "iPad Air", "Used for one year", "Digital", BigDecimal.valueOf(2800))
        );

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(aiClient.lastContent).isNull();
    }

    @Test
    void publishRejectsMissingSellerId() {
        ApiResponse<ProductResponse> response = productService.publish(
                new ProductCreateRequest(null, "iPad Air", "Used for one year", "Digital", BigDecimal.valueOf(2800))
        );

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("seller id is required");
        assertThat(response.data()).isNull();
    }

    @Test
    void publishRejectsBlankRequiredTextFields() {
        ApiResponse<ProductResponse> response = productService.publish(
                new ProductCreateRequest(1L, " ", "Used for one year", "Digital", BigDecimal.valueOf(2800))
        );

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("title, description and category are required");
        assertThat(response.data()).isNull();
    }

    @Test
    void publishRejectsNegativePrice() {
        ApiResponse<ProductResponse> response = productService.publish(
                new ProductCreateRequest(1L, "iPad Air", "Used for one year", "Digital", BigDecimal.valueOf(-1))
        );

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("price must be greater than or equal to 0");
        assertThat(response.data()).isNull();
    }

    @Test
    void findByIdReturnsNotFoundForMissingProduct() {
        ApiResponse<ProductResponse> response = productService.findById(99L);

        assertThat(response.code()).isEqualTo(ResultCode.NOT_FOUND.getCode());
        assertThat(response.message()).isEqualTo("product not found");
        assertThat(response.data()).isNull();
    }

    @Test
    void listFiltersByKeywordCategoryAndStatus() {
        productService.publish(new ProductCreateRequest(1L, "iPad Air", "Light tablet", "Digital",
                BigDecimal.valueOf(2800)));
        ApiResponse<ProductResponse> bike = productService.publish(new ProductCreateRequest(2L, "Road Bike",
                "Fast campus commute", "Sports", BigDecimal.valueOf(500)));
        productService.publish(new ProductCreateRequest(3L, "Desk Lamp", "Warm light", "Home",
                BigDecimal.valueOf(30)));
        productService.updateStatus(bike.data().id(), new ProductStatusUpdateRequest("SOLD"));

        ApiResponse<List<ProductResponse>> response = productService.list("CAMPUS", " Sports ", "SOLD");

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data())
                .extracting(ProductResponse::title)
                .containsExactly("Road Bike");
    }

    @Test
    void listRejectsInvalidStatus() {
        ApiResponse<List<ProductResponse>> response = productService.list(null, null, "gone");

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("invalid product status");
        assertThat(response.data()).isNull();
    }

    @Test
    void updateStatusChangesStoredProductStatus() {
        ApiResponse<ProductResponse> created = productService.publish(
                new ProductCreateRequest(1L, "iPad Air", "Used for one year", "Digital", BigDecimal.valueOf(2800))
        );

        ApiResponse<ProductResponse> response = productService.updateStatus(
                created.data().id(),
                new ProductStatusUpdateRequest("OFF_SALE")
        );

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data().status()).isEqualTo(ProductStatus.OFF_SALE);
        assertThat(productService.findById(created.data().id()).data().status()).isEqualTo(ProductStatus.OFF_SALE);
    }

    @Test
    void updateStatusReturnsNotFoundForMissingProduct() {
        ApiResponse<ProductResponse> response = productService.updateStatus(
                99L,
                new ProductStatusUpdateRequest("SOLD")
        );

        assertThat(response.code()).isEqualTo(ResultCode.NOT_FOUND.getCode());
        assertThat(response.message()).isEqualTo("product not found");
        assertThat(response.data()).isNull();
    }

    private static class FakeAiClient implements AiClient {

        private String lastContent;
        private boolean passed = true;
        private String reason = "content passed mock safety check";
        private boolean throwException;
        private boolean returnFailure;

        @Override
        public ApiResponse<ContentCheckClientResponse> checkContent(ContentCheckClientRequest request) {
            if (throwException) {
                throw new IllegalStateException("ai service unavailable");
            }
            this.lastContent = request.content();
            if (returnFailure) {
                return ApiResponse.fail(ResultCode.SYSTEM_ERROR, "ai error");
            }
            return new ApiResponse<>(
                    ResultCode.SUCCESS.getCode(),
                    "success",
                    new ContentCheckClientResponse(passed, reason)
            );
        }
    }
}

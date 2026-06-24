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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @MockBean
    private AiClient aiClient;

    @BeforeEach
    void stubContentCheckPassedByDefault() {
        when(aiClient.checkContent(any())).thenReturn(contentResult(true, "content passed mock safety check"));
    }

    @Test
    void publishCreatesProductOnSale() {
        ApiResponse<ProductResponse> response = productService.publish(
                new ProductCreateRequest(99L, "iPad Air", "Used for one year", "Digital", BigDecimal.valueOf(2800)),
                1L
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

        ArgumentCaptor<ContentCheckClientRequest> captor = ArgumentCaptor.forClass(ContentCheckClientRequest.class);
        verify(aiClient).checkContent(captor.capture());
        assertThat(captor.getValue().content()).isEqualTo("iPad Air Used for one year");
    }

    @Test
    void publishUsesAuthenticatedUserIdInsteadOfRequestSellerId() {
        ApiResponse<ProductResponse> response = productService.publish(
                new ProductCreateRequest(999L, "iPad Air", "Used for one year", "Digital", BigDecimal.valueOf(2800)),
                42L
        );

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data().sellerId()).isEqualTo(42L);
    }

    @Test
    void publishRejectsContentThatFailsSafetyCheck() {
        when(aiClient.checkContent(any()))
                .thenReturn(contentResult(false, "content contains prohibited keyword: 枪"));

        ApiResponse<ProductResponse> response = productService.publish(
                new ProductCreateRequest(1L, "出售仿真枪", "全新仿真枪一把", "Other", BigDecimal.valueOf(100)),
                1L
        );

        assertThat(response.code()).isEqualTo(ResultCode.FORBIDDEN.getCode());
        assertThat(response.message()).isEqualTo("content contains prohibited keyword: 枪");
        assertThat(response.data()).isNull();
        assertThat(productService.list(null, null, null).data()).isEmpty();
    }

    @Test
    void publishReturnsSystemErrorWhenAiServiceThrows() {
        when(aiClient.checkContent(any())).thenThrow(new IllegalStateException("ai down"));

        ApiResponse<ProductResponse> response = productService.publish(
                new ProductCreateRequest(1L, "iPad Air", "Used for one year", "Digital", BigDecimal.valueOf(2800)),
                1L
        );

        assertThat(response.code()).isEqualTo(ResultCode.SYSTEM_ERROR.getCode());
        assertThat(response.message()).isEqualTo("remote service unavailable");
        assertThat(response.data()).isNull();
    }

    @Test
    void publishReturnsSystemErrorWhenContentCheckResponseHasNoData() {
        when(aiClient.checkContent(any())).thenReturn(ApiResponse.fail(ResultCode.SYSTEM_ERROR, "ai error"));

        ApiResponse<ProductResponse> response = productService.publish(
                new ProductCreateRequest(1L, "iPad Air", "Used for one year", "Digital", BigDecimal.valueOf(2800)),
                1L
        );

        assertThat(response.code()).isEqualTo(ResultCode.SYSTEM_ERROR.getCode());
        assertThat(response.message()).isEqualTo("content check failed");
        assertThat(response.data()).isNull();
    }

    @Test
    void publishSkipsContentCheckWhenValidationFails() {
        ApiResponse<ProductResponse> response = productService.publish(
                new ProductCreateRequest(1L, "iPad Air", "Used for one year", "Digital", BigDecimal.valueOf(2800)),
                null
        );

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        verify(aiClient, never()).checkContent(any());
    }

    @Test
    void publishRejectsMissingAuthenticatedUserId() {
        ApiResponse<ProductResponse> response = productService.publish(
                new ProductCreateRequest(null, "iPad Air", "Used for one year", "Digital", BigDecimal.valueOf(2800)),
                null
        );

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("seller id is required");
        assertThat(response.data()).isNull();
    }

    @Test
    void publishRejectsBlankRequiredTextFields() {
        ApiResponse<ProductResponse> response = productService.publish(
                new ProductCreateRequest(1L, " ", "Used for one year", "Digital", BigDecimal.valueOf(2800)),
                1L
        );

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("title, description and category are required");
        assertThat(response.data()).isNull();
    }

    @Test
    void publishRejectsNegativePrice() {
        ApiResponse<ProductResponse> response = productService.publish(
                new ProductCreateRequest(1L, "iPad Air", "Used for one year", "Digital", BigDecimal.valueOf(-1)),
                1L
        );

        assertThat(response.code()).isEqualTo(ResultCode.BAD_REQUEST.getCode());
        assertThat(response.message()).isEqualTo("price must be greater than or equal to 0");
        assertThat(response.data()).isNull();
    }

    @Test
    void findByIdReturnsNotFoundForMissingProduct() {
        ApiResponse<ProductResponse> response = productService.findById(9999L);

        assertThat(response.code()).isEqualTo(ResultCode.NOT_FOUND.getCode());
        assertThat(response.message()).isEqualTo("product not found");
        assertThat(response.data()).isNull();
    }

    @Test
    void listFiltersByKeywordCategoryAndStatus() {
        productService.publish(new ProductCreateRequest(1L, "iPad Air", "Light tablet", "Digital",
                BigDecimal.valueOf(2800)), 1L);
        ApiResponse<ProductResponse> bike = productService.publish(new ProductCreateRequest(2L, "Road Bike",
                "Fast campus commute", "Sports", BigDecimal.valueOf(500)), 2L);
        productService.publish(new ProductCreateRequest(3L, "Desk Lamp", "Warm light", "Home",
                BigDecimal.valueOf(30)), 3L);
        productService.updateStatus(bike.data().id(), new ProductStatusUpdateRequest("SOLD"), 2L);

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
                new ProductCreateRequest(1L, "iPad Air", "Used for one year", "Digital", BigDecimal.valueOf(2800)),
                1L
        );

        ApiResponse<ProductResponse> response = productService.updateStatus(
                created.data().id(),
                new ProductStatusUpdateRequest("OFF_SALE"),
                1L
        );

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data().status()).isEqualTo(ProductStatus.OFF_SALE);
        assertThat(productService.findById(created.data().id()).data().status()).isEqualTo(ProductStatus.OFF_SALE);
    }

    @Test
    void updateStatusRejectsNonSellerAndLeavesProductUnchanged() {
        ApiResponse<ProductResponse> created = productService.publish(
                new ProductCreateRequest(1L, "iPad Air", "Used for one year", "Digital", BigDecimal.valueOf(2800)),
                1L
        );

        ApiResponse<ProductResponse> response = productService.updateStatus(
                created.data().id(),
                new ProductStatusUpdateRequest("SOLD"),
                2L
        );

        assertThat(response.code()).isEqualTo(ResultCode.FORBIDDEN.getCode());
        assertThat(response.message()).isEqualTo("only product seller can update status");
        assertThat(response.data()).isNull();
        assertThat(productService.findById(created.data().id()).data().status()).isEqualTo(ProductStatus.ON_SALE);
    }

    @Test
    void updateStatusReturnsNotFoundForMissingProduct() {
        ApiResponse<ProductResponse> response = productService.updateStatus(
                9999L,
                new ProductStatusUpdateRequest("SOLD"),
                1L
        );

        assertThat(response.code()).isEqualTo(ResultCode.NOT_FOUND.getCode());
        assertThat(response.message()).isEqualTo("product not found");
        assertThat(response.data()).isNull();
    }

    private ApiResponse<ContentCheckClientResponse> contentResult(boolean passed, String reason) {
        return new ApiResponse<>(
                ResultCode.SUCCESS.getCode(),
                "success",
                new ContentCheckClientResponse(passed, reason)
        );
    }
}

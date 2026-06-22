package com.campustrade.product.controller;

import com.campustrade.common.response.ApiResponse;
import com.campustrade.common.response.ResultCode;
import com.campustrade.product.dto.ProductCreateRequest;
import com.campustrade.product.dto.ProductResponse;
import com.campustrade.product.dto.ProductStatusUpdateRequest;
import com.campustrade.product.enums.ProductStatus;
import com.campustrade.product.service.ProductService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductControllerTest {

    private final CapturingProductService productService = new CapturingProductService();
    private final ProductController productController = new ProductController(productService);

    @Test
    void publishDelegatesToProductService() {
        ProductCreateRequest request = new ProductCreateRequest(
                1L,
                "iPad Air",
                "Used for one year",
                "Digital",
                BigDecimal.valueOf(2800)
        );

        ApiResponse<ProductResponse> response = productController.publish(request);

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data()).isEqualTo(productService.product);
        assertThat(productService.createRequest).isEqualTo(request);
    }

    @Test
    void listDelegatesToProductService() {
        ApiResponse<List<ProductResponse>> response = productController.list("ipad", "Digital", "ON_SALE");

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data()).containsExactly(productService.product);
        assertThat(productService.keyword).isEqualTo("ipad");
        assertThat(productService.category).isEqualTo("Digital");
        assertThat(productService.status).isEqualTo("ON_SALE");
    }

    @Test
    void findByIdDelegatesToProductService() {
        ApiResponse<ProductResponse> response = productController.findById(7L);

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data()).isEqualTo(productService.product);
        assertThat(productService.id).isEqualTo(7L);
    }

    @Test
    void updateStatusDelegatesToProductService() {
        ProductStatusUpdateRequest request = new ProductStatusUpdateRequest("SOLD");

        ApiResponse<ProductResponse> response = productController.updateStatus(7L, request);

        assertThat(response.code()).isEqualTo(ResultCode.SUCCESS.getCode());
        assertThat(response.data()).isEqualTo(productService.product);
        assertThat(productService.id).isEqualTo(7L);
        assertThat(productService.statusRequest).isEqualTo(request);
    }

    private static class CapturingProductService extends ProductService {

        private CapturingProductService() {
            super(null, null);
        }

        private final ProductResponse product = new ProductResponse(
                7L,
                1L,
                "iPad Air",
                "Used for one year",
                "Digital",
                BigDecimal.valueOf(2800),
                ProductStatus.ON_SALE
        );

        private ProductCreateRequest createRequest;
        private ProductStatusUpdateRequest statusRequest;
        private Long id;
        private String keyword;
        private String category;
        private String status;

        @Override
        public ApiResponse<ProductResponse> publish(ProductCreateRequest request) {
            this.createRequest = request;
            return ApiResponse.success(product);
        }

        @Override
        public ApiResponse<List<ProductResponse>> list(String keyword, String category, String status) {
            this.keyword = keyword;
            this.category = category;
            this.status = status;
            return ApiResponse.success(List.of(product));
        }

        @Override
        public ApiResponse<ProductResponse> findById(Long id) {
            this.id = id;
            return ApiResponse.success(product);
        }

        @Override
        public ApiResponse<ProductResponse> updateStatus(Long id, ProductStatusUpdateRequest request) {
            this.id = id;
            this.statusRequest = request;
            return ApiResponse.success(product);
        }
    }
}

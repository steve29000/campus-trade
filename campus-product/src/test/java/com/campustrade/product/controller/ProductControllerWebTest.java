package com.campustrade.product.controller;

import com.campustrade.common.response.ApiResponse;
import com.campustrade.product.dto.ProductCreateRequest;
import com.campustrade.product.dto.ProductResponse;
import com.campustrade.product.dto.ProductStatusUpdateRequest;
import com.campustrade.product.enums.ProductStatus;
import com.campustrade.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductControllerWebTest {

    private final CapturingProductService productService = new CapturingProductService();
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new ProductController(productService))
            .build();

    @Test
    void publishRouteBindsJsonRequestAndReturnsProductJson() throws Exception {
        mockMvc.perform(post("/product")
                        .header("X-User-Id", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sellerId": 2,
                                  "title": "iPad Air",
                                  "description": "Used for one year",
                                  "category": "Digital",
                                  "price": 2800
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("iPad Air"))
                .andExpect(jsonPath("$.data.status").value("ON_SALE"));

        assertThat(productService.createRequest.sellerId()).isEqualTo(2L);
        assertThat(productService.authenticatedUserId).isEqualTo(42L);
        assertThat(productService.createRequest.price()).isEqualByComparingTo(BigDecimal.valueOf(2800));
    }

    @Test
    void listRouteBindsQueryParameters() throws Exception {
        mockMvc.perform(get("/product")
                        .param("keyword", "ipad")
                        .param("category", "Digital")
                        .param("status", "ON_SALE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].title").value("iPad Air"));

        assertThat(productService.keyword).isEqualTo("ipad");
        assertThat(productService.category).isEqualTo("Digital");
        assertThat(productService.status).isEqualTo("ON_SALE");
    }

    @Test
    void updateStatusRouteBindsPathAndJsonRequest() throws Exception {
        mockMvc.perform(put("/product/1/status")
                        .header("X-User-Id", 42L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "SOLD"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("SOLD"));

        assertThat(productService.id).isEqualTo(1L);
        assertThat(productService.statusRequest.status()).isEqualTo("SOLD");
        assertThat(productService.authenticatedUserId).isEqualTo(42L);
    }

    private static class CapturingProductService extends ProductService {

        private CapturingProductService() {
            super(null, null);
        }

        private ProductCreateRequest createRequest;
        private ProductStatusUpdateRequest statusRequest;
        private Long id;
        private Long authenticatedUserId;
        private String keyword;
        private String category;
        private String status;

        @Override
        public ApiResponse<ProductResponse> publish(ProductCreateRequest request, Long authenticatedUserId) {
            this.createRequest = request;
            this.authenticatedUserId = authenticatedUserId;
            return ApiResponse.success(new ProductResponse(
                    1L,
                    authenticatedUserId,
                    request.title(),
                    request.description(),
                    request.category(),
                    request.price(),
                    ProductStatus.ON_SALE
            ));
        }

        @Override
        public ApiResponse<ProductResponse> updateStatus(
                Long id,
                ProductStatusUpdateRequest request,
                Long authenticatedUserId
        ) {
            this.id = id;
            this.statusRequest = request;
            this.authenticatedUserId = authenticatedUserId;
            return ApiResponse.success(new ProductResponse(
                    id,
                    2L,
                    "iPad Air",
                    "Used for one year",
                    "Digital",
                    BigDecimal.valueOf(2800),
                    ProductStatus.valueOf(request.status())
            ));
        }

        @Override
        public ApiResponse<List<ProductResponse>> list(String keyword, String category, String status) {
            this.keyword = keyword;
            this.category = category;
            this.status = status;
            return ApiResponse.success(List.of(new ProductResponse(
                    1L,
                    2L,
                    "iPad Air",
                    "Used for one year",
                    "Digital",
                    BigDecimal.valueOf(2800),
                    ProductStatus.ON_SALE
            )));
        }
    }
}

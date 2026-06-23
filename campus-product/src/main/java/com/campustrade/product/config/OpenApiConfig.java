package com.campustrade.product.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI productServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("CampusTrade 商品服务 API")
                .version("0.0.1")
                .description("商品发布（含 AI 内容审核）、列表筛选、详情和状态更新接口。"));
    }
}

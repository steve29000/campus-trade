package com.campustrade.order.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("CampusTrade 订单服务 API")
                .version("0.0.1")
                .description("订单创建（跨服务校验）、查询、买家/卖家列表、取消和完成接口。"));
    }
}

package com.campustrade.message.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI messageServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("CampusTrade 留言服务 API")
                .version("0.0.1")
                .description("商品留言发布（跨服务校验）、按商品查询、隐藏和删除接口。"));
    }
}

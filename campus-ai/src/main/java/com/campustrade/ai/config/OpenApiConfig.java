package com.campustrade.ai.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI aiServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("CampusTrade AI 服务 API")
                .version("0.0.1")
                .description("商品描述优化、智能分类和内容安全检查的 mock 接口。"));
    }
}

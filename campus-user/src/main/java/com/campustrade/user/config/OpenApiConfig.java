package com.campustrade.user.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("CampusTrade 用户服务 API")
                .version("0.0.1")
                .description("用户注册、登录（签发 JWT）和资料查询接口。"));
    }
}

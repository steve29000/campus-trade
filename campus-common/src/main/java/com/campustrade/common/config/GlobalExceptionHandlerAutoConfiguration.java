package com.campustrade.common.config;

import com.campustrade.common.exception.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * 自动注册 {@link GlobalExceptionHandler}，让引入了 servlet MVC 的服务无需额外配置即可获得统一异常兜底。
 *
 * <p>{@link ConditionalOnClass} 保证只有当 classpath 上存在 {@link DispatcherServlet}（即 spring-webmvc）
 * 时才会生效，因此 campus-gateway（WebFlux）会自动跳过，不会因为 servlet 处理器而报错。</p>
 */
@AutoConfiguration
@ConditionalOnClass(DispatcherServlet.class)
public class GlobalExceptionHandlerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}

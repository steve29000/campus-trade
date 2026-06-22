package com.campustrade.common.config;

import com.campustrade.common.security.JwtProperties;
import com.campustrade.common.security.JwtUtil;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 当配置了 {@code jwt.secret} 时自动注册 {@link JwtUtil}。
 *
 * <p>不加 servlet 条件，因此 servlet 服务（campus-user）和 WebFlux 网关（campus-gateway）都能拿到。</p>
 */
@AutoConfiguration
@EnableConfigurationProperties(JwtProperties.class)
@ConditionalOnProperty(prefix = "jwt", name = "secret")
public class JwtAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtUtil jwtUtil(JwtProperties properties) {
        return new JwtUtil(properties.getSecret(), properties.getExpiration());
    }
}

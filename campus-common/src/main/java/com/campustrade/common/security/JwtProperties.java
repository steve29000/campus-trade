package com.campustrade.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 配置。各服务在 application.yml 中通过 {@code jwt.secret} / {@code jwt.expiration} 设置，
 * user 与 gateway 必须使用相同的 secret。
 */
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * HMAC 签名密钥，长度需满足 HS256 要求（至少 32 字节）。
     */
    private String secret;

    /**
     * token 有效期（毫秒），默认 24 小时。
     */
    private long expiration = 24 * 60 * 60 * 1000L;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }
}

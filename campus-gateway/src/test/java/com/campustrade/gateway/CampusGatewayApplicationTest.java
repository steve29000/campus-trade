package com.campustrade.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.nacos.config.enabled=false",
        "spring.config.import=",
        "jwt.secret=campus-trade-jwt-secret-key-for-gateway-tests-0123456789"
})
class CampusGatewayApplicationTest {

    @Test
    void contextLoads() {
    }
}

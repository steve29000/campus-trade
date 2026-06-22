package com.campustrade.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
class GatewayLoadBalancerSupportTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void reactiveLoadBalancerFilterIsAvailableForLbRoutes() {
        assertThat(applicationContext.getBeansOfType(ReactiveLoadBalancerClientFilter.class))
                .isNotEmpty();
    }
}

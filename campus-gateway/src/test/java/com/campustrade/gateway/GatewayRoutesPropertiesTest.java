package com.campustrade.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@SpringBootTest(properties = {
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.nacos.config.enabled=false",
        "spring.config.import=",
        "jwt.secret=campus-trade-jwt-secret-key-for-gateway-tests-0123456789"
})
class GatewayRoutesPropertiesTest {

    @Autowired
    private GatewayProperties gatewayProperties;

    @Test
    void routeDefinitionsMatchGatewayContract() {
        assertThat(gatewayProperties.getRoutes())
                .extracting(
                        RouteDefinition::getId,
                        route -> route.getUri().toString(),
                        GatewayRoutesPropertiesTest::pathPredicate
                )
                .containsExactly(
                        tuple("campus-user", "lb://campus-user", "Path=/user/**"),
                        tuple("campus-product", "lb://campus-product", "Path=/product/**"),
                        tuple("campus-order", "lb://campus-order", "Path=/order/**"),
                        tuple("campus-ai", "lb://campus-ai", "Path=/ai/**"),
                        tuple("campus-message", "lb://campus-message", "Path=/message/**")
                );
    }

    private static String pathPredicate(RouteDefinition route) {
        PredicateDefinition predicate = route.getPredicates().get(0);

        assertThat(route.getPredicates())
                .hasSize(1)
                .first()
                .extracting(PredicateDefinition::getName)
                .isEqualTo("Path");

        assertThat(predicate.getArgs())
                .hasSize(1);

        return predicate.getName() + "=" + predicate.getArgs().values().iterator().next();
    }
}

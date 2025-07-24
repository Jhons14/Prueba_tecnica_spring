package com.microservices.products.config;

import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class CustomHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return Health.up()
                .withDetail("service", "products-service")
                .withDetail("status", "UP")
                .withDetail("version", "1.0.0")
                .build();
    }
}
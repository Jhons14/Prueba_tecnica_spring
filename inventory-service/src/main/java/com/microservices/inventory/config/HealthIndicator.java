package com.microservices.inventory.config;

import com.microservices.inventory.service.ProductsServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class CustomHealthIndicator implements HealthIndicator {

    @Autowired
    private ProductsServiceClient productsServiceClient;

    @Override
    public Health health() {
        try {
            return Health.up()
                    .withDetail("service", "inventory-service")
                    .withDetail("status", "UP")
                    .withDetail("version", "1.0.0")
                    .withDetail("products-service-connectivity", "OK")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("service", "inventory-service")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
package com.microservices.inventory.service;

import com.microservices.inventory.dto.JsonApiResponse;
import com.microservices.inventory.dto.ProductInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

@Service
public class ProductsServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(ProductsServiceClient.class);
    
    private final WebClient webClient;
    private final String productsServiceApiKey;

    public ProductsServiceClient(
            @Value("${app.products-service.url}") String productsServiceUrl,
            @Value("${app.products-service.api-key}") String productsServiceApiKey) {
        this.productsServiceApiKey = productsServiceApiKey;
        this.webClient = WebClient.builder()
                .baseUrl(productsServiceUrl)
                .build();
    }

    public ProductInfo getProductInfo(Long productId) {
        logger.info("Fetching product info for ID: {}", productId);
        
        try {
            JsonApiResponse response = webClient.get()
                    .uri("/api/products/{id}", productId)
                    .header("X-API-Key", productsServiceApiKey)
                    .retrieve()
                    .bodyToMono(JsonApiResponse.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                            .filter(throwable -> !(throwable instanceof WebClientResponseException.NotFound)))
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response != null && response.getData() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getData();
                Map<String, Object> attributes = (Map<String, Object>) data.get("attributes");
                
                return new ProductInfo(
                        (String) attributes.get("name"),
                        java.math.BigDecimal.valueOf(((Number) attributes.get("price")).doubleValue()),
                        (String) attributes.get("description")
                );
            }
            
            return null;
        } catch (WebClientResponseException.NotFound e) {
            logger.warn("Product not found with ID: {}", productId);
            return null;
        } catch (Exception e) {
            logger.error("Error fetching product info for ID: {}", productId, e);
            throw new RuntimeException("Failed to fetch product information", e);
        }
    }
}
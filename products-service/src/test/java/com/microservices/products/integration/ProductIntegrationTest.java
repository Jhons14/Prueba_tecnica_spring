package com.microservices.products.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.products.dto.JsonApiResponse;
import com.microservices.products.dto.ProductDto;
import com.microservices.products.model.Product;
import com.microservices.products.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    void createProduct_EndToEnd_ShouldPersistProduct() throws Exception {
        ProductDto productDto = new ProductDto(null, "Integration Test Product", 
                new BigDecimal("99.99"), "Integration test description");
        JsonApiResponse<ProductDto> request = JsonApiResponse.success(productDto);

        mockMvc.perform(post("/api/products")
                .header("X-API-Key", "products-service-api-key-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpected(jsonPath("$.data.attributes.name").value("Integration Test Product"))
                .andExpect(jsonPath("$.data.attributes.price").value(99.99));

        assertThat(productRepository.count()).isEqualTo(1);
        Product savedProduct = productRepository.findAll().get(0);
        assertThat(savedProduct.getName()).isEqualTo("Integration Test Product");
        assertThat(savedProduct.getPrice()).isEqualTo(new BigDecimal("99.99"));
    }

    @Test
    void getProduct_AfterCreation_ShouldReturnProduct() throws Exception {
        Product product = new Product("Test Product", new BigDecimal("29.99"), "Test Description");
        Product savedProduct = productRepository.save(product);

        mockMvc.perform(get("/api/products/" + savedProduct.getId())
                .header("X-API-Key", "products-service-api-key-123"))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.data.attributes.name").value("Test Product"))
                .andExpect(jsonPath("$.data.attributes.price").value(29.99));
    }

    @Test
    void getAllProducts_WithMultipleProducts_ShouldReturnAllProducts() throws Exception {
        Product product1 = new Product("Product 1", new BigDecimal("10.00"), "Description 1");
        Product product2 = new Product("Product 2", new BigDecimal("20.00"), "Description 2");
        productRepository.save(product1);
        productRepository.save(product2);

        mockMvc.perform(get("/api/products")
                .header("X-API-Key", "products-service-api-key-123"))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.dataList").isArray())
                .andExpect(jsonPath("$.dataList.length()").value(2));
    }
}
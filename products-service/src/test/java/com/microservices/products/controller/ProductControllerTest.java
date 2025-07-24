package com.microservices.products.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.products.dto.JsonApiResponse;
import com.microservices.products.dto.ProductDto;
import com.microservices.products.dto.ProductCreateDto;
import com.microservices.products.model.Product;
import com.microservices.products.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private Product testProduct;
    private ProductDto testProductDto;
    private ProductCreateDto requestBody;

    @BeforeEach
    void setUp() {
        testProduct = new Product("Test Product", new BigDecimal("19.99"), "Test Description");
        testProduct.setId(1L);

        testProductDto = new ProductDto("1", "Test Product", new BigDecimal("19.99"), "Test Description");

        requestBody = new ProductCreateDto("Test Product", new BigDecimal("19.99"), "Test Description");
    }

    @Test
    @WithMockUser
    void createProduct_WithValidInput_ShouldReturnCreatedProduct() throws Exception {
        when(productService.createProduct(any(Product.class))).thenReturn(testProduct);

        mockMvc.perform(post("/api/products")
                .with(csrf())
                .header("X-API-Key", "products-service-api-key-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.type").value("product"))
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.attributes.name").value("Test Product"))
                .andExpect(jsonPath("$.data.attributes.price").value(19.99))
                .andExpect(jsonPath("$.data.attributes.description").value("Test Description"));
    }

    @Test
    @WithMockUser
    void getProduct_WhenProductExists_ShouldReturnProduct() throws Exception {
        when(productService.getProductById(1L)).thenReturn(Optional.of(testProduct));

        mockMvc.perform(get("/api/products/1")
                .header("X-API-Key", "products-service-api-key-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("product"))
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.attributes.name").value("Test Product"));
    }

    @Test
    @WithMockUser
    void getProduct_WhenProductNotExists_ShouldReturnNotFound() throws Exception {
        when(productService.getProductById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/products/999")
                .header("X-API-Key", "products-service-api-key-123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].status").value("404"))
                .andExpect(jsonPath("$.errors[0].title").value("Not Found"));
    }

    @Test
    @WithMockUser
    void getAllProducts_ShouldReturnAllProducts() throws Exception {
        List<Product> products = Arrays.asList(testProduct);
        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/api/products")
                .header("X-API-Key", "products-service-api-key-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataList").isArray())
                .andExpect(jsonPath("$.dataList[0].type").value("product"))
                .andExpect(jsonPath("$.dataList[0].attributes.name").value("Test Product"));
    }

    @Test
    void createProduct_WithoutApiKey_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void createProduct_WithNegativePrice_ShouldReturnValidationError() throws Exception {
        ProductCreateDto invalidProductDto = new ProductCreateDto("Test Product", new BigDecimal("-1.00"), "Test Description");

        mockMvc.perform(post("/api/products")
                .with(csrf())
                .header("X-API-Key", "products-service-api-key-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProductDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].status").value("400"))
                .andExpect(jsonPath("$.errors[0].title").value("Validation Error"));
    }

    @Test
    @WithMockUser
    void createProduct_WithZeroPrice_ShouldReturnValidationError() throws Exception {
        ProductCreateDto invalidProductDto = new ProductCreateDto("Test Product", new BigDecimal("0.00"), "Test Description");

        mockMvc.perform(post("/api/products")
                .with(csrf())
                .header("X-API-Key", "products-service-api-key-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProductDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].status").value("400"))
                .andExpect(jsonPath("$.errors[0].title").value("Validation Error"));
    }
}
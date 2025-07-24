package com.microservices.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.inventory.dto.*;
import com.microservices.inventory.model.Purchase;
import com.microservices.inventory.service.InventoryService;
import com.microservices.inventory.service.PurchaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PurchaseController.class)
class PurchaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PurchaseService purchaseService;

    @MockBean
    private InventoryService inventoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private Purchase testPurchase;
    private ProductInfo testProductInfo;
    private JsonApiResponse<PurchaseRequestDto> validRequest;

    @BeforeEach
    void setUp() {
        testPurchase = new Purchase(1L, 2, new BigDecimal("39.98"));
        testPurchase.setId(1L);
        
        testProductInfo = new ProductInfo("Test Product", new BigDecimal("19.99"), "Test Description");

        PurchaseRequestDto requestDto = new PurchaseRequestDto();
        requestDto.setAttributes(new PurchaseRequestDto.PurchaseRequestAttributes(1L, 2));
        validRequest = JsonApiResponse.success(requestDto);
    }

    @Test
    @WithMockUser
    void processPurchase_WithValidRequest_ShouldReturnCreatedPurchase() throws Exception {
        when(purchaseService.processPurchase(1L, 2)).thenReturn(testPurchase);
        when(inventoryService.getProductInfo(1L)).thenReturn(testProductInfo);

        mockMvc.perform(post("/api/purchases")
                .with(csrf())
                .header("X-API-Key", "inventory-service-api-key-456")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.type").value("purchase"))
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.attributes.productId").value(1))
                .andExpect(jsonPath("$.data.attributes.quantity").value(2))
                .andExpect(jsonPath("$.data.attributes.totalPrice").value(39.98));
    }

    @Test
    @WithMockUser
    void processPurchase_WhenProductNotFound_ShouldReturnNotFound() throws Exception {
        when(purchaseService.processPurchase(999L, 2))
                .thenThrow(new RuntimeException("Product not found with ID: 999"));

        PurchaseRequestDto requestDto = new PurchaseRequestDto();
        requestDto.setAttributes(new PurchaseRequestDto.PurchaseRequestAttributes(999L, 2));
        JsonApiResponse<PurchaseRequestDto> request = JsonApiResponse.success(requestDto);

        mockMvc.perform(post("/api/purchases")
                .with(csrf())
                .header("X-API-Key", "inventory-service-api-key-456")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].status").value("404"))
                .andExpect(jsonPath("$.errors[0].title").value("Not Found"));
    }

    @Test
    @WithMockUser
    void processPurchase_WhenInsufficientInventory_ShouldReturnBadRequest() throws Exception {
        when(purchaseService.processPurchase(1L, 100))
                .thenThrow(new RuntimeException("Insufficient inventory for product ID: 1"));

        PurchaseRequestDto requestDto = new PurchaseRequestDto();
        requestDto.setAttributes(new PurchaseRequestDto.PurchaseRequestAttributes(1L, 100));
        JsonApiResponse<PurchaseRequestDto> request = JsonApiResponse.success(requestDto);

        mockMvc.perform(post("/api/purchases")
                .with(csrf())
                .header("X-API-Key", "inventory-service-api-key-456")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].status").value("400"))
                .andExpect(jsonPath("$.errors[0].title").value("Insufficient Inventory"));
    }

    @Test
    void processPurchase_WithoutApiKey_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/purchases")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized());
    }
}
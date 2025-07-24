package com.microservices.inventory.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.inventory.dto.*;
import com.microservices.inventory.model.Inventory;
import com.microservices.inventory.repository.InventoryRepository;
import com.microservices.inventory.repository.PurchaseRepository;
import com.microservices.inventory.service.ProductsServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class InventoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @MockBean
    private ProductsServiceClient productsServiceClient;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductInfo testProductInfo;

    @BeforeEach
    void setUp() {
        inventoryRepository.deleteAll();
        purchaseRepository.deleteAll();
        
        testProductInfo = new ProductInfo("Test Product", new BigDecimal("19.99"), "Test Description");
    }

    @Test
    void getInventoryByProductId_WhenProductExists_ShouldReturnInventoryWithProductInfo() throws Exception {
        when(productsServiceClient.getProductInfo(1L)).thenReturn(testProductInfo);
        Inventory inventory = new Inventory(1L, 10);
        inventoryRepository.save(inventory);

        mockMvc.perform(get("/api/inventory/products/1")
                .header("X-API-Key", "inventory-service-api-key-456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("inventory"))
                .andExpected(jsonPath("$.data.attributes.productId").value(1))
                .andExpect(jsonPath("$.data.attributes.quantity").value(10))
                .andExpect(jsonPath("$.data.attributes.productInfo.name").value("Test Product"));
    }

    @Test
    void updateInventoryQuantity_ShouldPersistChanges() throws Exception {
        when(productsServiceClient.getProductInfo(1L)).thenReturn(testProductInfo);
        
        InventoryDto inventoryDto = new InventoryDto(null, 1L, 25, null);
        JsonApiResponse<InventoryDto> request = JsonApiResponse.success(inventoryDto);

        mockMvc.perform(put("/api/inventory/products/1")
                .header("X-API-Key", "inventory-service-api-key-456")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.data.attributes.quantity").value(25));

        Inventory savedInventory = inventoryRepository.findByProductId(1L).orElse(null);
        assertThat(savedInventory).isNotNull();
        assertThat(savedInventory.getQuantity()).isEqualTo(25);
    }

    @Test
    void processPurchase_EndToEnd_ShouldCreatePurchaseAndUpdateInventory() throws Exception {
        when(productsServiceClient.getProductInfo(1L)).thenReturn(testProductInfo);
        
        Inventory initialInventory = new Inventory(1L, 20);
        inventoryRepository.save(initialInventory);

        PurchaseRequestDto purchaseRequest = new PurchaseRequestDto();
        purchaseRequest.setAttributes(new PurchaseRequestDto.PurchaseRequestAttributes(1L, 3));
        JsonApiResponse<PurchaseRequestDto> request = JsonApiResponse.success(purchaseRequest);

        mockMvc.perform(post("/api/purchases")
                .header("X-API-Key", "inventory-service-api-key-456")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpected(jsonPath("$.data.attributes.productId").value(1))
                .andExpect(jsonPath("$.data.attributes.quantity").value(3))
                .andExpect(jsonPath("$.data.attributes.totalPrice").value(59.97));

        Inventory updatedInventory = inventoryRepository.findByProductId(1L).orElse(null);
        assertThat(updatedInventory).isNotNull();
        assertThat(updatedInventory.getQuantity()).isEqualTo(17);

        assertThat(purchaseRepository.count()).isEqualTo(1);
    }

    @Test
    void processPurchase_WithInsufficientInventory_ShouldFailWithoutChanges() throws Exception {
        when(productsServiceClient.getProductInfo(1L)).thenReturn(testProductInfo);
        
        Inventory limitedInventory = new Inventory(1L, 2);
        inventoryRepository.save(limitedInventory);

        PurchaseRequestDto purchaseRequest = new PurchaseRequestDto();
        purchaseRequest.setAttributes(new PurchaseRequestDto.PurchaseRequestAttributes(1L, 5));
        JsonApiResponse<PurchaseRequestDto> request = JsonApiResponse.success(purchaseRequest);

        mockMvc.perform(post("/api/purchases")
                .header("X-API-Key", "inventory-service-api-key-456")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpected(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].title").value("Insufficient Inventory"));

        Inventory unchangedInventory = inventoryRepository.findByProductId(1L).orElse(null);
        assertThat(unchangedInventory.getQuantity()).isEqualTo(2);
        assertThat(purchaseRepository.count()).isEqualTo(0);
    }
}
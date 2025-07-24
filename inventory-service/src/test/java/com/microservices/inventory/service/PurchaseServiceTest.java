package com.microservices.inventory.service;

import com.microservices.inventory.dto.ProductInfo;
import com.microservices.inventory.model.Purchase;
import com.microservices.inventory.repository.PurchaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private PurchaseService purchaseService;

    private ProductInfo testProductInfo;
    private Purchase testPurchase;

    @BeforeEach
    void setUp() {
        testProductInfo = new ProductInfo("Test Product", new BigDecimal("19.99"), "Test Description");
        testPurchase = new Purchase(1L, 2, new BigDecimal("39.98"));
        testPurchase.setId(1L);
    }

    @Test
    void processPurchase_WithValidProductAndSufficientInventory_ShouldCreatePurchase() {
        when(inventoryService.getProductInfo(1L)).thenReturn(testProductInfo);
        when(inventoryService.decreaseInventory(1L, 2)).thenReturn(true);
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(testPurchase);

        Purchase result = purchaseService.processPurchase(1L, 2);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getProductId()).isEqualTo(1L);
        assertThat(result.getQuantity()).isEqualTo(2);
        assertThat(result.getTotalPrice()).isEqualTo(new BigDecimal("39.98"));
        
        verify(inventoryService).getProductInfo(1L);
        verify(inventoryService).decreaseInventory(1L, 2);
        verify(purchaseRepository).save(any(Purchase.class));
    }

    @Test
    void processPurchase_WhenProductNotFound_ShouldThrowException() {
        when(inventoryService.getProductInfo(999L)).thenReturn(null);

        assertThatThrownBy(() -> purchaseService.processPurchase(999L, 2))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product not found with ID: 999");

        verify(inventoryService).getProductInfo(999L);
        verify(inventoryService, never()).decreaseInventory(anyLong(), anyInt());
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void processPurchase_WhenInsufficientInventory_ShouldThrowException() {
        when(inventoryService.getProductInfo(1L)).thenReturn(testProductInfo);
        when(inventoryService.decreaseInventory(1L, 20)).thenReturn(false);

        assertThatThrownBy(() -> purchaseService.processPurchase(1L, 20))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Insufficient inventory for product ID: 1");

        verify(inventoryService).getProductInfo(1L);
        verify(inventoryService).decreaseInventory(1L, 20);
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void processPurchase_ShouldCalculateCorrectTotalPrice() {
        ProductInfo expensiveProduct = new ProductInfo("Expensive Product", new BigDecimal("100.00"), "Expensive");
        when(inventoryService.getProductInfo(2L)).thenReturn(expensiveProduct);
        when(inventoryService.decreaseInventory(2L, 3)).thenReturn(true);
        
        Purchase expectedPurchase = new Purchase(2L, 3, new BigDecimal("300.00"));
        expectedPurchase.setId(2L);
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(expectedPurchase);

        Purchase result = purchaseService.processPurchase(2L, 3);

        assertThat(result.getTotalPrice()).isEqualTo(new BigDecimal("300.00"));
    }
}
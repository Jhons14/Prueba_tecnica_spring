package com.microservices.inventory.service;

import com.microservices.inventory.dto.ProductInfo;
import com.microservices.inventory.model.Inventory;
import com.microservices.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductsServiceClient productsServiceClient;

    @InjectMocks
    private InventoryService inventoryService;

    private Inventory testInventory;
    private ProductInfo testProductInfo;

    @BeforeEach
    void setUp() {
        testInventory = new Inventory(1L, 10);
        testInventory.setId(1L);
        
        testProductInfo = new ProductInfo("Test Product", new BigDecimal("19.99"), "Test Description");
    }

    @Test
    void getInventoryByProductId_WhenInventoryExists_ShouldReturnInventory() {
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(testInventory));

        Optional<Inventory> result = inventoryService.getInventoryByProductId(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getProductId()).isEqualTo(1L);
        assertThat(result.get().getQuantity()).isEqualTo(10);
        verify(inventoryRepository).findByProductId(1L);
    }

    @Test
    void getInventoryByProductId_WhenInventoryNotExists_ShouldReturnEmpty() {
        when(inventoryRepository.findByProductId(999L)).thenReturn(Optional.empty());

        Optional<Inventory> result = inventoryService.getInventoryByProductId(999L);

        assertThat(result).isEmpty();
        verify(inventoryRepository).findByProductId(999L);
    }

    @Test
    void getProductInfo_ShouldCallProductsServiceClient() {
        when(productsServiceClient.getProductInfo(1L)).thenReturn(testProductInfo);

        ProductInfo result = inventoryService.getProductInfo(1L);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Product");
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("19.99"));
        verify(productsServiceClient).getProductInfo(1L);
    }

    @Test
    void updateInventoryQuantity_WhenInventoryExists_ShouldUpdateQuantity() {
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        Inventory result = inventoryService.updateInventoryQuantity(1L, 20);

        assertThat(result.getQuantity()).isEqualTo(20);
        verify(inventoryRepository).findByProductId(1L);
        verify(inventoryRepository).save(testInventory);
    }

    @Test
    void updateInventoryQuantity_WhenInventoryNotExists_ShouldCreateNewInventory() {
        when(inventoryRepository.findByProductId(2L)).thenReturn(Optional.empty());
        Inventory newInventory = new Inventory(2L, 15);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(newInventory);

        Inventory result = inventoryService.updateInventoryQuantity(2L, 15);

        assertThat(result.getProductId()).isEqualTo(2L);
        assertThat(result.getQuantity()).isEqualTo(15);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void decreaseInventory_WhenSufficientStock_ShouldDecreaseAndReturnTrue() {
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(testInventory));

        boolean result = inventoryService.decreaseInventory(1L, 5);

        assertThat(result).isTrue();
        assertThat(testInventory.getQuantity()).isEqualTo(5);
        verify(inventoryRepository).save(testInventory);
    }

    @Test
    void decreaseInventory_WhenInsufficientStock_ShouldReturnFalse() {
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(testInventory));

        boolean result = inventoryService.decreaseInventory(1L, 15);

        assertThat(result).isFalse();
        assertThat(testInventory.getQuantity()).isEqualTo(10);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void decreaseInventory_WhenInventoryNotExists_ShouldReturnFalse() {
        when(inventoryRepository.findByProductId(999L)).thenReturn(Optional.empty());

        boolean result = inventoryService.decreaseInventory(999L, 5);

        assertThat(result).isFalse();
        verify(inventoryRepository, never()).save(any());
    }
}
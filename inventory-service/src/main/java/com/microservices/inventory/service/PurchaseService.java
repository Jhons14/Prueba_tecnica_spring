package com.microservices.inventory.service;

import com.microservices.inventory.dto.ProductInfo;
import com.microservices.inventory.model.Purchase;
import com.microservices.inventory.repository.PurchaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PurchaseService {
    private static final Logger logger = LoggerFactory.getLogger(PurchaseService.class);
    
    private final PurchaseRepository purchaseRepository;
    private final InventoryService inventoryService;

    @Autowired
    public PurchaseService(PurchaseRepository purchaseRepository, InventoryService inventoryService) {
        this.purchaseRepository = purchaseRepository;
        this.inventoryService = inventoryService;
    }

    @Transactional
    public Purchase processPurchase(Long productId, Integer quantity) {
        logger.info("Processing purchase for product ID: {} with quantity: {}", productId, quantity);
        
        ProductInfo productInfo = inventoryService.getProductInfo(productId);
        if (productInfo == null) {
            throw new RuntimeException("Product not found with ID: " + productId);
        }
        
        boolean inventoryDecreased = inventoryService.decreaseInventory(productId, quantity);
        if (!inventoryDecreased) {
            throw new RuntimeException("Insufficient inventory for product ID: " + productId);
        }
        
        BigDecimal totalPrice = productInfo.getPrice().multiply(BigDecimal.valueOf(quantity));
        Purchase purchase = new Purchase(productId, quantity, totalPrice);
        
        Purchase savedPurchase = purchaseRepository.save(purchase);
        logger.info("Purchase processed successfully with ID: {}", savedPurchase.getId());
        
        return savedPurchase;
    }
}
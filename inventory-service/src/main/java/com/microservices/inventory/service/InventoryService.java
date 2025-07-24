package com.microservices.inventory.service;

import com.microservices.inventory.dto.ProductInfo;
import com.microservices.inventory.model.Inventory;
import com.microservices.inventory.repository.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class InventoryService {
    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);
    
    private final InventoryRepository inventoryRepository;
    private final ProductsServiceClient productsServiceClient;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepository, ProductsServiceClient productsServiceClient) {
        this.inventoryRepository = inventoryRepository;
        this.productsServiceClient = productsServiceClient;
    }

    public Optional<Inventory> getInventoryByProductId(Long productId) {
        logger.info("Fetching inventory for product ID: {}", productId);
        return inventoryRepository.findByProductId(productId);
    }

    public ProductInfo getProductInfo(Long productId) {
        return productsServiceClient.getProductInfo(productId);
    }

    @Transactional
    public Inventory updateInventoryQuantity(Long productId, Integer newQuantity) {
        logger.info("Updating inventory for product ID: {} to quantity: {}", productId, newQuantity);
        
        Optional<Inventory> inventoryOpt = inventoryRepository.findByProductId(productId);
        Inventory inventory;
        
        if (inventoryOpt.isPresent()) {
            inventory = inventoryOpt.get();
            inventory.setQuantity(newQuantity);
        } else {
            inventory = new Inventory(productId, newQuantity);
        }
        
        Inventory saved = inventoryRepository.save(inventory);
        logger.info("Inventory updated for product ID: {}, new quantity: {}", productId, saved.getQuantity());
        return saved;
    }

    @Transactional
    public boolean decreaseInventory(Long productId, Integer quantity) {
        logger.info("Attempting to decrease inventory for product ID: {} by quantity: {}", productId, quantity);
        
        Optional<Inventory> inventoryOpt = inventoryRepository.findByProductId(productId);
        
        if (inventoryOpt.isEmpty()) {
            logger.warn("No inventory found for product ID: {}", productId);
            return false;
        }
        
        Inventory inventory = inventoryOpt.get();
        if (inventory.getQuantity() < quantity) {
            logger.warn("Insufficient inventory for product ID: {}. Available: {}, Requested: {}", 
                    productId, inventory.getQuantity(), quantity);
            return false;
        }
        
        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventoryRepository.save(inventory);
        
        logger.info("Inventory decreased for product ID: {}. New quantity: {}", productId, inventory.getQuantity());
        return true;
    }
}
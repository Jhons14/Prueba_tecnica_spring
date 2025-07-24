package com.microservices.inventory.controller;

import com.microservices.inventory.dto.*;
import com.microservices.inventory.model.Inventory;
import com.microservices.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventory")
@Tag(name = "Inventory", description = "Inventory management API following JSON API specification")
public class InventoryController {
    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);
    
    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/products/{productId}")
    @Operation(summary = "Get inventory by product ID")
    @ApiResponse(responseCode = "200", description = "Inventory found")
    @ApiResponse(responseCode = "404", description = "Product or inventory not found")
    public ResponseEntity<JsonApiResponse<InventoryDto>> getInventoryByProductId(@PathVariable Long productId) {
        logger.info("Fetching inventory for product ID: {}", productId);
        
        try {
            ProductInfo productInfo = inventoryService.getProductInfo(productId);
            if (productInfo == null) {
                List<JsonApiError> errors = List.of(
                    new JsonApiError("404", "Not Found", "Product with ID " + productId + " not found")
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(JsonApiResponse.error(errors));
            }

            Optional<Inventory> inventory = inventoryService.getInventoryByProductId(productId);
            if (inventory.isPresent()) {
                Inventory inv = inventory.get();
                InventoryDto inventoryDto = new InventoryDto(
                    inv.getId().toString(),
                    inv.getProductId(),
                    inv.getQuantity(),
                    productInfo
                );
                return ResponseEntity.ok(JsonApiResponse.success(inventoryDto));
            } else {
                InventoryDto inventoryDto = new InventoryDto(
                    null,
                    productId,
                    0,
                    productInfo
                );
                return ResponseEntity.ok(JsonApiResponse.success(inventoryDto));
            }
        } catch (Exception e) {
            logger.error("Error fetching inventory for product ID: {}", productId, e);
            List<JsonApiError> errors = List.of(
                new JsonApiError("500", "Internal Server Error", "Failed to fetch inventory")
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(JsonApiResponse.error(errors));
        }
    }

    @PutMapping("/products/{productId}")
    @Operation(summary = "Update inventory quantity for a product")
    @ApiResponse(responseCode = "200", description = "Inventory updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Product not found")
    public ResponseEntity<JsonApiResponse<InventoryDto>> updateInventoryQuantity(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryDto request,
            BindingResult bindingResult) {
        
        if (bindingResult.hasErrors()) {
            List<JsonApiError> errors = bindingResult.getFieldErrors().stream()
                .map(error -> new JsonApiError("400", "Validation Error", error.getDefaultMessage()))
                .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(JsonApiResponse.error(errors));
        }

        try {
            ProductInfo productInfo = inventoryService.getProductInfo(productId);
            if (productInfo == null) {
                List<JsonApiError> errors = List.of(
                    new JsonApiError("404", "Not Found", "Product with ID " + productId + " not found")
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(JsonApiResponse.error(errors));
            }

            Integer newQuantity = request.getAttributes().getQuantity();
            Inventory updatedInventory = inventoryService.updateInventoryQuantity(productId, newQuantity);
            
            InventoryDto inventoryDto = new InventoryDto(
                updatedInventory.getId().toString(),
                updatedInventory.getProductId(),
                updatedInventory.getQuantity(),
                productInfo
            );

            return ResponseEntity.ok(JsonApiResponse.success(inventoryDto));
        } catch (Exception e) {
            logger.error("Error updating inventory for product ID: {}", productId, e);
            List<JsonApiError> errors = List.of(
                new JsonApiError("500", "Internal Server Error", "Failed to update inventory")
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(JsonApiResponse.error(errors));
        }
    }
}
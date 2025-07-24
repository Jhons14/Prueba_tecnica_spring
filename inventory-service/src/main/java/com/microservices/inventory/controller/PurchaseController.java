package com.microservices.inventory.controller;

import com.microservices.inventory.dto.*;
import com.microservices.inventory.model.Purchase;
import com.microservices.inventory.service.InventoryService;
import com.microservices.inventory.service.PurchaseService;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/purchases")
@Tag(name = "Purchases", description = "Purchase processing API following JSON API specification")
public class PurchaseController {
    private static final Logger logger = LoggerFactory.getLogger(PurchaseController.class);
    
    private final PurchaseService purchaseService;
    private final InventoryService inventoryService;

    @Autowired
    public PurchaseController(PurchaseService purchaseService, InventoryService inventoryService) {
        this.purchaseService = purchaseService;
        this.inventoryService = inventoryService;
    }

    @PostMapping
    @Operation(summary = "Process a purchase")
    @ApiResponse(responseCode = "201", description = "Purchase processed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input or insufficient inventory")
    @ApiResponse(responseCode = "404", description = "Product not found")
    public ResponseEntity<JsonApiResponse<PurchaseDto>> processPurchase(
            @Valid @RequestBody PurchaseRequestDto request,
            BindingResult bindingResult) {
        
        if (bindingResult.hasErrors()) {
            List<JsonApiError> errors = bindingResult.getFieldErrors().stream()
                .map(error -> new JsonApiError("400", "Validation Error", error.getDefaultMessage()))
                .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(JsonApiResponse.error(errors));
        }

        try {
            Long productId = request.getAttributes().getProductId();
            Integer quantity = request.getAttributes().getQuantity();

            Purchase purchase = purchaseService.processPurchase(productId, quantity);
            ProductInfo productInfo = inventoryService.getProductInfo(productId);
            
            PurchaseDto purchaseDto = new PurchaseDto(
                purchase.getId().toString(),
                purchase.getProductId(),
                purchase.getQuantity(),
                purchase.getTotalPrice(),
                purchase.getPurchaseDate(),
                productInfo
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(JsonApiResponse.success(purchaseDto));
                
        } catch (RuntimeException e) {
            logger.error("Error processing purchase", e);
            
            String errorMessage = e.getMessage();
            String status = "400";
            String title = "Bad Request";
            
            if (errorMessage.contains("Product not found")) {
                status = "404";
                title = "Not Found";
            } else if (errorMessage.contains("Insufficient inventory")) {
                title = "Insufficient Inventory";
            }
            
            List<JsonApiError> errors = List.of(
                new JsonApiError(status, title, errorMessage)
            );
            
            return ResponseEntity.status(Integer.parseInt(status))
                .body(JsonApiResponse.error(errors));
                
        } catch (Exception e) {
            logger.error("Unexpected error processing purchase", e);
            List<JsonApiError> errors = List.of(
                new JsonApiError("500", "Internal Server Error", "Failed to process purchase")
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(JsonApiResponse.error(errors));
        }
    }
}
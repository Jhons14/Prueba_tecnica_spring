package com.microservices.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class PurchaseRequestDto {
    private String type = "purchase-request";
    private PurchaseRequestAttributes attributes;

    public PurchaseRequestDto() {}

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public PurchaseRequestAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(PurchaseRequestAttributes attributes) {
        this.attributes = attributes;
    }

    public static class PurchaseRequestAttributes {
        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        private Integer quantity;

        public PurchaseRequestAttributes() {}

        public PurchaseRequestAttributes(Long productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}
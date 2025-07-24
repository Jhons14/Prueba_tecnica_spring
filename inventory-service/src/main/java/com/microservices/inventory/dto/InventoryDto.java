package com.microservices.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class InventoryDto {
    private String type = "inventory";
    private String id;
    private InventoryAttributes attributes;

    public InventoryDto() {}

    public InventoryDto(String id, Long productId, Integer quantity, ProductInfo productInfo) {
        this.id = id;
        this.attributes = new InventoryAttributes(productId, quantity, productInfo);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public InventoryAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(InventoryAttributes attributes) {
        this.attributes = attributes;
    }

    public static class InventoryAttributes {
        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotNull(message = "Quantity is required")
        @PositiveOrZero(message = "Quantity must be zero or positive")
        private Integer quantity;

        private ProductInfo productInfo;

        public InventoryAttributes() {}

        public InventoryAttributes(Long productId, Integer quantity, ProductInfo productInfo) {
            this.productId = productId;
            this.quantity = quantity;
            this.productInfo = productInfo;
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

        public ProductInfo getProductInfo() {
            return productInfo;
        }

        public void setProductInfo(ProductInfo productInfo) {
            this.productInfo = productInfo;
        }
    }
}
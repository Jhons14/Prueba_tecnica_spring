package com.microservices.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PurchaseDto {
    private String type = "purchase";
    private String id;
    private PurchaseAttributes attributes;

    public PurchaseDto() {}

    public PurchaseDto(String id, Long productId, Integer quantity, BigDecimal totalPrice, LocalDateTime purchaseDate, ProductInfo productInfo) {
        this.id = id;
        this.attributes = new PurchaseAttributes(productId, quantity, totalPrice, purchaseDate, productInfo);
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

    public PurchaseAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(PurchaseAttributes attributes) {
        this.attributes = attributes;
    }

    public static class PurchaseAttributes {
        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        private Integer quantity;

        private BigDecimal totalPrice;
        private LocalDateTime purchaseDate;
        private ProductInfo productInfo;

        public PurchaseAttributes() {}

        public PurchaseAttributes(Long productId, Integer quantity, BigDecimal totalPrice, LocalDateTime purchaseDate, ProductInfo productInfo) {
            this.productId = productId;
            this.quantity = quantity;
            this.totalPrice = totalPrice;
            this.purchaseDate = purchaseDate;
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

        public BigDecimal getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
        }

        public LocalDateTime getPurchaseDate() {
            return purchaseDate;
        }

        public void setPurchaseDate(LocalDateTime purchaseDate) {
            this.purchaseDate = purchaseDate;
        }

        public ProductInfo getProductInfo() {
            return productInfo;
        }

        public void setProductInfo(ProductInfo productInfo) {
            this.productInfo = productInfo;
        }
    }
}
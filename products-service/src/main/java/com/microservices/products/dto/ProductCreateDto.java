package com.microservices.products.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class ProductCreateDto {
    private String type = "product";
    @Valid
    @NotNull(message = "Attributes are required")
    private ProductCreateAttributes attributes;

    public ProductCreateDto() {}

    public ProductCreateDto(String name, BigDecimal price, String description) {
        this.attributes = new ProductCreateAttributes(name, price, description);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ProductCreateAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(ProductCreateAttributes attributes) {
        this.attributes = attributes;
    }

    public static class ProductCreateAttributes {
        @NotBlank(message = "Name is required")
        private String name;

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        private BigDecimal price;

        private String description;

        public ProductCreateAttributes() {}

        public ProductCreateAttributes(String name, BigDecimal price, String description) {
            this.name = name;
            this.price = price;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
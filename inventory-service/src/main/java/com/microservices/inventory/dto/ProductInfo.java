package com.microservices.inventory.dto;

import java.math.BigDecimal;

public class ProductInfo {
    private String name;
    private BigDecimal price;
    private String description;

    public ProductInfo() {}

    public ProductInfo(String name, BigDecimal price, String description) {
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
package com.carexport.dto;

import java.math.BigDecimal;

public class SearchRequest {
    private String brand;
    private String model;
    private BigDecimal maxPrice;

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
}

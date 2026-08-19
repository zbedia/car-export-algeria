package com.carexport.dto;

import com.carexport.model.FuelType;

import java.math.BigDecimal;

public class SearchRequest {
    private String brand;
    private String model;
    private BigDecimal maxPrice;
    private Integer maxMileageKm;
    private String garageCity;
    private FuelType fuelType;

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
    public Integer getMaxMileageKm() { return maxMileageKm; }
    public void setMaxMileageKm(Integer maxMileageKm) { this.maxMileageKm = maxMileageKm; }
    public String getGarageCity() { return garageCity; }
    public void setGarageCity(String garageCity) { this.garageCity = garageCity; }
    public FuelType getFuelType() { return fuelType; }
    public void setFuelType(FuelType fuelType) { this.fuelType = fuelType; }
}

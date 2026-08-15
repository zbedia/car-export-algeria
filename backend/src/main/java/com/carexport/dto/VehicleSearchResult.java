package com.carexport.dto;

import java.math.BigDecimal;

public class VehicleSearchResult {
    private Long id;
    private String source;
    private String externalUrl;
    private String brand;
    private String model;
    private int year;
    private int mileageKm;
    private BigDecimal price;
    private String currency;
    private String garageCity;
    private boolean bestPrice;
    private String fuelType;
    private Integer engineDisplacementCm3;
    private Integer engineDisplacementThresholdCm3;
    private BigDecimal customsDiscountPercentage;
    private String customsDiscountReasonCode;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getExternalUrl() { return externalUrl; }
    public void setExternalUrl(String externalUrl) { this.externalUrl = externalUrl; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public int getMileageKm() { return mileageKm; }
    public void setMileageKm(int mileageKm) { this.mileageKm = mileageKm; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getGarageCity() { return garageCity; }
    public void setGarageCity(String garageCity) { this.garageCity = garageCity; }
    public boolean isBestPrice() { return bestPrice; }
    public void setBestPrice(boolean bestPrice) { this.bestPrice = bestPrice; }
    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }
    public Integer getEngineDisplacementCm3() { return engineDisplacementCm3; }
    public void setEngineDisplacementCm3(Integer engineDisplacementCm3) { this.engineDisplacementCm3 = engineDisplacementCm3; }
    public Integer getEngineDisplacementThresholdCm3() { return engineDisplacementThresholdCm3; }
    public void setEngineDisplacementThresholdCm3(Integer engineDisplacementThresholdCm3) { this.engineDisplacementThresholdCm3 = engineDisplacementThresholdCm3; }
    public BigDecimal getCustomsDiscountPercentage() { return customsDiscountPercentage; }
    public void setCustomsDiscountPercentage(BigDecimal customsDiscountPercentage) { this.customsDiscountPercentage = customsDiscountPercentage; }
    public String getCustomsDiscountReasonCode() { return customsDiscountReasonCode; }
    public void setCustomsDiscountReasonCode(String customsDiscountReasonCode) { this.customsDiscountReasonCode = customsDiscountReasonCode; }
}

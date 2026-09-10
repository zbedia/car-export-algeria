package com.carexport.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
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
    private String cheapestSource;
    private String fuelType;
    private Integer engineDisplacementCm3;
    private Integer engineDisplacementThresholdCm3;
    private BigDecimal customsDiscountPercentage;
    private String customsDiscountReasonCode;
    private String imageUrl;

    /** Age of the vehicle in whole months since first registration, for the eligibility badge. */
    private Integer ageMonths;

    /** Decomposed customs estimate (duty after discount + VAT), in EUR and DZD. */
    private CustomsEstimate customs;
}
package com.carexport.dto;

import com.carexport.model.FuelType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SearchRequest {
    private String brand;
    private String model;
    private BigDecimal maxPrice;
    private Integer maxMileageKm;
    private String garageCity;
    private List<FuelType> fuelTypes;
    private String source;
}
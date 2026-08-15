package com.carexport.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "vehicle_listing", indexes = {
    @Index(name = "idx_external_url", columnList = "externalUrl", unique = true),
    @Index(name = "idx_brand_model", columnList = "brand, model")
})
public class VehicleListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false, unique = true, length = 1000)
    private String externalUrl;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(name = "vehicle_year", nullable = false)
    private int year;

    private int mileageKm;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currency;

    private String garageCity;

    @Column(nullable = false)
    private LocalDateTime scrapedAt;

    /**
     * Fuel type. Required for Algerian import eligibility rules:
     * only ESSENCE, HYBRIDE and ELECTRIQUE are importable by private individuals
     * for vehicles under 3 years old — DIESEL is strictly banned.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FuelType fuelType;

    /**
     * Engine displacement in cm3. Null for electric vehicles.
     * Determines the customs duty reduction tier for combustion/hybrid vehicles.
     */
    @Column(name = "engine_displacement_cm3")
    private Integer engineDisplacementCm3;

    /**
     * Date of first registration (1ere mise en circulation).
     * This is the authoritative date used to compute import eligibility
     * to the day, as required by Algerian import regulations.
     */
    @Column(name = "first_registration_date", nullable = false)
    private LocalDate firstRegistrationDate;

    public VehicleListing() {}

    public Long getId() { return id; }
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
    public LocalDateTime getScrapedAt() { return scrapedAt; }
    public void setScrapedAt(LocalDateTime scrapedAt) { this.scrapedAt = scrapedAt; }
    public FuelType getFuelType() { return fuelType; }
    public void setFuelType(FuelType fuelType) { this.fuelType = fuelType; }
    public Integer getEngineDisplacementCm3() { return engineDisplacementCm3; }
    public void setEngineDisplacementCm3(Integer engineDisplacementCm3) { this.engineDisplacementCm3 = engineDisplacementCm3; }
    public LocalDate getFirstRegistrationDate() { return firstRegistrationDate; }
    public void setFirstRegistrationDate(LocalDate firstRegistrationDate) { this.firstRegistrationDate = firstRegistrationDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VehicleListing)) return false;
        VehicleListing that = (VehicleListing) o;
        return Objects.equals(externalUrl, that.externalUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(externalUrl);
    }
}

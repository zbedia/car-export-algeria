package com.carexport.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_listing", indexes = {
        @Index(name = "idx_external_url", columnList = "externalUrl", unique = true),
        @Index(name = "idx_brand_model", columnList = "brand, model")
})
@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class VehicleListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Optimistic locking version column. Incremented by Hibernate on every
     * UPDATE; JPA checks it during the write so two concurrent transactions
     * modifying the same listing cannot silently overwrite each other
     * (a conflict raises OptimisticLockingFailureException).
     */
    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false, unique = true, length = 1000)
    @EqualsAndHashCode.Include
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

    /**
     * Main photo of the listing, scraped from the marketplace
     * (JSON-LD "image" / Open Graph og:image). Null when the site
     * exposes no image.
     */
    @Column(name = "image_url", length = 2000)
    private String imageUrl;
}
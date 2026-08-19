package com.carexport.repository;

import com.carexport.model.FuelType;
import com.carexport.model.VehicleListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleListingRepository extends JpaRepository<VehicleListing, Long> {

    Optional<VehicleListing> findByExternalUrl(String externalUrl);

    List<VehicleListing> findByBrandIgnoreCaseAndModelIgnoreCase(String brand, String model);

    /**
     * Searches vehicles matching the user criteria (brand, model, price,
     * mileage, city, fuel type) AND the Algerian import eligibility rules:
     * fuel type must not be DIESEL, and the vehicle must not be older than
     * the eligibility cutoff date (2 years 10 months, computed to the day).
     * garageCity uses a partial, case-insensitive match (e.g. "lyo" matches
     * "Lyon") — brand and model remain exact matches.
     */
    @Query("""
        SELECT v FROM VehicleListing v
        WHERE (:brand IS NULL OR LOWER(v.brand) = LOWER(:brand))
        AND (:model IS NULL OR LOWER(v.model) = LOWER(:model))
        AND v.price <= :maxPrice
        AND (:maxMileageKm IS NULL OR v.mileageKm <= :maxMileageKm)
        AND (:garageCity IS NULL OR LOWER(v.garageCity) LIKE LOWER(CONCAT('%', :garageCity, '%')))
        AND (:fuelType IS NULL OR v.fuelType = :fuelType)
        AND v.fuelType <> com.carexport.model.FuelType.DIESEL
        AND v.firstRegistrationDate >= :oldestEligibleRegistrationDate
        ORDER BY v.price ASC
        """)
    List<VehicleListing> search(
        @Param("brand") String brand,
        @Param("model") String model,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("maxMileageKm") Integer maxMileageKm,
        @Param("garageCity") String garageCity,
        @Param("fuelType") FuelType fuelType,
        @Param("oldestEligibleRegistrationDate") LocalDate oldestEligibleRegistrationDate
    );

    @Query("""
        SELECT v FROM VehicleListing v
        WHERE v.brand = :brand AND v.model = :model
        ORDER BY v.price ASC
        LIMIT 1
        """)
    Optional<VehicleListing> findBestPrice(@Param("brand") String brand, @Param("model") String model);
}

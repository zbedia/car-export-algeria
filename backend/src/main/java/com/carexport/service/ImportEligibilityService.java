package com.carexport.service;

import com.carexport.model.FuelType;
import com.carexport.model.VehicleListing;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Encapsulates the Algerian vehicle import regulations
 * (Decret executif n. 23-74 and the finance law).
 *
 * Rules implemented:
 * 1. Strict 3-year age rule, computed to the day between first registration
 *    and customs clearance. A 2-year-10-month safety margin is applied here
 *    instead of the full 3 years, to absorb delays between the search and
 *    the actual customs crossing.
 * 2. Only ESSENCE, HYBRIDE and ELECTRIQUE vehicles are importable by private
 *    individuals. DIESEL is strictly banned for vehicles under 3 years old.
 * 3. Customs duty reduction tiers:
 *    - Electric: -80%
 *    - Essence/Hybrid <= 1800 cm3: -50%
 *    - Essence/Hybrid > 1800 cm3: -20%
 */
@Service
public class ImportEligibilityService {

    private static final int SAFETY_MARGIN_YEARS = 2;
    private static final int SAFETY_MARGIN_MONTHS = 10;
    private static final int ENGINE_DISPLACEMENT_THRESHOLD_CM3 = 1800;

    private static final BigDecimal ELECTRIC_DISCOUNT = new BigDecimal("80");
    private static final BigDecimal SMALL_ENGINE_DISCOUNT = new BigDecimal("50");
    private static final BigDecimal LARGE_ENGINE_DISCOUNT = new BigDecimal("20");

    /**
     * The oldest first-registration date still eligible for import today,
     * applying the 2-year-10-month safety margin.
     */
    public LocalDate getOldestEligibleRegistrationDate() {
        return LocalDate.now()
            .minusYears(SAFETY_MARGIN_YEARS)
            .minusMonths(SAFETY_MARGIN_MONTHS);
    }

    public boolean isEligibleForImport(VehicleListing vehicle) {
        return isFuelTypeAllowed(vehicle.getFuelType())
            && isWithinAgeLimit(vehicle.getFirstRegistrationDate());
    }

    public boolean isFuelTypeAllowed(FuelType fuelType) {
        return fuelType != FuelType.DIESEL;
    }

    public boolean isWithinAgeLimit(LocalDate firstRegistrationDate) {
        return !firstRegistrationDate.isBefore(getOldestEligibleRegistrationDate());
    }

    /**
     * Customs duty reduction percentage applicable to the vehicle,
     * based on fuel type and, for combustion/hybrid engines, displacement.
     * Returns zero for non-importable fuel types (the vehicle should have
     * already been filtered out upstream in that case).
     */
    public BigDecimal getCustomsDiscountPercentage(VehicleListing vehicle) {
        if (vehicle.getFuelType() == FuelType.ELECTRIQUE) {
            return ELECTRIC_DISCOUNT;
        }
        if (vehicle.getFuelType() == FuelType.DIESEL) {
            return BigDecimal.ZERO;
        }
        // ESSENCE or HYBRIDE
        Integer displacement = vehicle.getEngineDisplacementCm3();
        if (displacement != null && displacement <= ENGINE_DISPLACEMENT_THRESHOLD_CM3) {
            return SMALL_ENGINE_DISCOUNT;
        }
        return LARGE_ENGINE_DISCOUNT;
    }

    /**
     * Human-readable explanation of why the vehicle got its specific
     * customs discount tier, so the reasoning is transparent to the user
     * instead of just showing a bare percentage.
     */
    public String getCustomsDiscountReason(VehicleListing vehicle) {
        if (vehicle.getFuelType() == FuelType.ELECTRIQUE) {
            return "Electric vehicles get an 80% customs duty reduction.";
        }
        if (vehicle.getFuelType() == FuelType.DIESEL) {
            return "Diesel vehicles are not eligible for private import.";
        }

        String fuelLabel = vehicle.getFuelType() == FuelType.HYBRIDE ? "Hybrid" : "Essence";
        Integer displacement = vehicle.getEngineDisplacementCm3();

        if (displacement != null && displacement <= ENGINE_DISPLACEMENT_THRESHOLD_CM3) {
            return fuelLabel + " engines up to " + ENGINE_DISPLACEMENT_THRESHOLD_CM3
                + " cm³ (this one: " + displacement + " cm³) get a 50% customs duty reduction.";
        }

        String displacementText = displacement != null ? displacement + " cm³" : "unknown displacement";
        return fuelLabel + " engines over " + ENGINE_DISPLACEMENT_THRESHOLD_CM3
            + " cm³ (this one: " + displacementText + ") get a 20% customs duty reduction.";
    }
}

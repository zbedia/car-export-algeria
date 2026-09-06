package com.carexport.service;

import com.carexport.model.CustomsDiscountReasonCode;
import com.carexport.model.FuelType;
import com.carexport.model.VehicleListing;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Encapsulates the Algerian vehicle import regulations
 * (Decret executif n. 23-74 and the finance law).
 */
public interface ImportEligibilityService {

    /**
     * The oldest first-registration date still eligible for import today,
     * applying the 2-year-10-month safety margin.
     */
    LocalDate getOldestEligibleRegistrationDate();

    boolean isEligibleForImport(VehicleListing vehicle);

    boolean isFuelTypeAllowed(FuelType fuelType);

    boolean isWithinAgeLimit(LocalDate firstRegistrationDate);

    /** The displacement threshold (cm3) separating the two combustion/hybrid discount tiers. */
    int getEngineDisplacementThresholdCm3();

    /**
     * Customs duty reduction percentage applicable to the vehicle, based on
     * fuel type and, for combustion/hybrid engines, displacement.
     */
    BigDecimal getCustomsDiscountPercentage(VehicleListing vehicle);

    /**
     * Machine-readable reason code for the vehicle's customs discount tier.
     * The frontend maps it to a translated explanation.
     */
    CustomsDiscountReasonCode getCustomsDiscountReasonCode(VehicleListing vehicle);
}
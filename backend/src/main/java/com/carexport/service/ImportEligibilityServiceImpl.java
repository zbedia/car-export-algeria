package com.carexport.service;

import com.carexport.dto.CustomsEstimate;
import com.carexport.model.CustomsDiscountReasonCode;
import com.carexport.model.FuelType;
import com.carexport.model.VehicleListing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
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
 *
 * This service returns machine-readable reason codes (CustomsDiscountReasonCode)
 * rather than pre-built sentences, so the frontend can render a fully
 * translated explanation in any supported language.
 */
@Service
public class ImportEligibilityServiceImpl implements ImportEligibilityService {

    private static final int SAFETY_MARGIN_YEARS = 2;
    private static final int SAFETY_MARGIN_MONTHS = 10;
    private static final int ENGINE_DISPLACEMENT_THRESHOLD_CM3 = 1800;

    private static final BigDecimal ELECTRIC_DISCOUNT = new BigDecimal("80");
    private static final BigDecimal SMALL_ENGINE_DISCOUNT = new BigDecimal("50");
    private static final BigDecimal LARGE_ENGINE_DISCOUNT = new BigDecimal("20");

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    // Rates are configurable, but initialized fields keep unit tests using
    // "new ImportEligibilityServiceImpl()" working without a Spring context.
    @Value("${app.customs.duty-rate-percent:30.00}")
    private BigDecimal dutyRatePercent = new BigDecimal("30.00");

    @Value("${app.customs.vat-percent:19.00}")
    private BigDecimal vatPercent = new BigDecimal("19.00");

    @Value("${app.customs.dzd-per-eur:250.00}")
    private BigDecimal dzdPerEur = new BigDecimal("250.00");

    @Override
    public LocalDate getOldestEligibleRegistrationDate() {
        return LocalDate.now()
            .minusYears(SAFETY_MARGIN_YEARS)
            .minusMonths(SAFETY_MARGIN_MONTHS);
    }

    @Override
    public boolean isEligibleForImport(VehicleListing vehicle) {
        return isFuelTypeAllowed(vehicle.getFuelType())
            && isWithinAgeLimit(vehicle.getFirstRegistrationDate());
    }

    @Override
    public boolean isFuelTypeAllowed(FuelType fuelType) {
        return fuelType != FuelType.DIESEL;
    }

    @Override
    public boolean isWithinAgeLimit(LocalDate firstRegistrationDate) {
        return !firstRegistrationDate.isBefore(getOldestEligibleRegistrationDate());
    }

    @Override
    public int getEngineDisplacementThresholdCm3() {
        return ENGINE_DISPLACEMENT_THRESHOLD_CM3;
    }

    @Override
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

    @Override
    public CustomsDiscountReasonCode getCustomsDiscountReasonCode(VehicleListing vehicle) {
        if (vehicle.getFuelType() == FuelType.ELECTRIQUE) {
            return CustomsDiscountReasonCode.ELECTRIC;
        }
        if (vehicle.getFuelType() == FuelType.DIESEL) {
            return CustomsDiscountReasonCode.DIESEL_NOT_ELIGIBLE;
        }

        Integer displacement = vehicle.getEngineDisplacementCm3();
        if (displacement != null && displacement <= ENGINE_DISPLACEMENT_THRESHOLD_CM3) {
            return CustomsDiscountReasonCode.SMALL_ENGINE;
        }
        return CustomsDiscountReasonCode.LARGE_ENGINE;
    }

    @Override
    public CustomsEstimate estimateCustoms(VehicleListing vehicle) {
        BigDecimal price = vehicle.getPrice() != null ? vehicle.getPrice() : BigDecimal.ZERO;

        BigDecimal discount = getCustomsDiscountPercentage(vehicle);
        BigDecimal dutyBefore = price
            .multiply(dutyRatePercent)
            .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
        BigDecimal dutyAfter = dutyBefore
            .multiply(BigDecimal.ONE.subtract(discount.divide(ONE_HUNDRED, 6, RoundingMode.HALF_UP)))
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal vat = price
            .add(dutyAfter)
            .multiply(vatPercent)
            .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
        BigDecimal totalEur = dutyAfter.add(vat).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalDzd = totalEur
            .multiply(dzdPerEur)
            .setScale(2, RoundingMode.HALF_UP);

        CustomsEstimate estimate = new CustomsEstimate();
        estimate.setDutyEur(dutyAfter);
        estimate.setVatEur(vat);
        estimate.setTotalEur(totalEur);
        estimate.setTotalDzd(totalDzd);
        estimate.setDutyRatePercent(dutyRatePercent);
        return estimate;
    }
}
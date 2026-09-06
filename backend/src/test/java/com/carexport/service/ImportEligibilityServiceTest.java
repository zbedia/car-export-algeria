package com.carexport.service;

import com.carexport.model.CustomsDiscountReasonCode;
import com.carexport.model.FuelType;
import com.carexport.model.VehicleListing;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ImportEligibilityServiceTest {

    private final ImportEligibilityService service = new ImportEligibilityServiceImpl();

    @Test
    void diesel_isNeverEligible_evenIfRecent() {
        VehicleListing v = buildVehicle(FuelType.DIESEL, 1500, LocalDate.now().minusMonths(1));
        assertThat(service.isEligibleForImport(v)).isFalse();
    }

    @Test
    void essence_isEligible_withinSafetyMargin() {
        VehicleListing v = buildVehicle(FuelType.ESSENCE, 1600, LocalDate.now().minusMonths(20));
        assertThat(service.isEligibleForImport(v)).isTrue();
    }

    @Test
    void essence_isNotEligible_beyondSafetyMargin() {
        // 2 years 10 months = 34 months; 40 months exceeds it
        VehicleListing v = buildVehicle(FuelType.ESSENCE, 1600, LocalDate.now().minusMonths(40));
        assertThat(service.isEligibleForImport(v)).isFalse();
    }

    @Test
    void electric_getsEightyPercentDiscount() {
        VehicleListing v = buildVehicle(FuelType.ELECTRIQUE, null, LocalDate.now().minusMonths(5));
        assertThat(service.getCustomsDiscountPercentage(v)).isEqualByComparingTo(new BigDecimal("80"));
    }

    @Test
    void essence_smallEngine_getsFiftyPercentDiscount() {
        VehicleListing v = buildVehicle(FuelType.ESSENCE, 1800, LocalDate.now().minusMonths(5));
        assertThat(service.getCustomsDiscountPercentage(v)).isEqualByComparingTo(new BigDecimal("50"));
    }

    @Test
    void hybrid_largeEngine_getsTwentyPercentDiscount() {
        VehicleListing v = buildVehicle(FuelType.HYBRIDE, 2000, LocalDate.now().minusMonths(5));
        assertThat(service.getCustomsDiscountPercentage(v)).isEqualByComparingTo(new BigDecimal("20"));
    }

    @Test
    void electric_discountReasonCode_isElectric() {
        VehicleListing v = buildVehicle(FuelType.ELECTRIQUE, null, LocalDate.now().minusMonths(5));
        assertThat(service.getCustomsDiscountReasonCode(v)).isEqualTo(CustomsDiscountReasonCode.ELECTRIC);
    }

    @Test
    void diesel_discountReasonCode_isDieselNotEligible() {
        VehicleListing v = buildVehicle(FuelType.DIESEL, 1500, LocalDate.now().minusMonths(5));
        assertThat(service.getCustomsDiscountReasonCode(v)).isEqualTo(CustomsDiscountReasonCode.DIESEL_NOT_ELIGIBLE);
    }

    @Test
    void essence_smallEngine_discountReasonCode_isSmallEngine() {
        VehicleListing v = buildVehicle(FuelType.ESSENCE, 1600, LocalDate.now().minusMonths(5));
        assertThat(service.getCustomsDiscountReasonCode(v)).isEqualTo(CustomsDiscountReasonCode.SMALL_ENGINE);
    }

    @Test
    void hybrid_largeEngine_discountReasonCode_isLargeEngine() {
        VehicleListing v = buildVehicle(FuelType.HYBRIDE, 2000, LocalDate.now().minusMonths(5));
        assertThat(service.getCustomsDiscountReasonCode(v)).isEqualTo(CustomsDiscountReasonCode.LARGE_ENGINE);
    }

    private VehicleListing buildVehicle(FuelType fuelType, Integer displacementCm3, LocalDate firstRegistrationDate) {
        VehicleListing v = new VehicleListing();
        v.setFuelType(fuelType);
        v.setEngineDisplacementCm3(displacementCm3);
        v.setFirstRegistrationDate(firstRegistrationDate);
        return v;
    }
}

package com.carexport.service;

import com.carexport.model.FuelType;
import com.carexport.model.VehicleListing;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ImportEligibilityServiceTest {

    private final ImportEligibilityService service = new ImportEligibilityService();

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
    void electric_discountReason_mentionsElectric() {
        VehicleListing v = buildVehicle(FuelType.ELECTRIQUE, null, LocalDate.now().minusMonths(5));
        assertThat(service.getCustomsDiscountReason(v)).contains("Electric").contains("80%");
    }

    @Test
    void essence_smallEngine_discountReason_mentionsDisplacement() {
        VehicleListing v = buildVehicle(FuelType.ESSENCE, 1600, LocalDate.now().minusMonths(5));
        assertThat(service.getCustomsDiscountReason(v)).contains("1600 cm³").contains("50%");
    }

    @Test
    void hybrid_largeEngine_discountReason_mentionsDisplacement() {
        VehicleListing v = buildVehicle(FuelType.HYBRIDE, 2000, LocalDate.now().minusMonths(5));
        assertThat(service.getCustomsDiscountReason(v)).contains("2000 cm³").contains("20%");
    }

    private VehicleListing buildVehicle(FuelType fuelType, Integer displacementCm3, LocalDate firstRegistrationDate) {
        VehicleListing v = new VehicleListing();
        v.setFuelType(fuelType);
        v.setEngineDisplacementCm3(displacementCm3);
        v.setFirstRegistrationDate(firstRegistrationDate);
        return v;
    }
}

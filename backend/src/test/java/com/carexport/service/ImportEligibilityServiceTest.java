package com.carexport.service;

import com.carexport.dto.CustomsEstimate;
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

    @Test
    void estimateCustoms_appliesElectricDiscount_andComputesVatAndDzd() {
        // Price 20 000 €, 30% base duty, 80% electric discount:
        // duty = 6000 × 20% = 1200 €; VAT = (20000+1200) × 19% = 4028 €;
        // total = 5228 €; at 250 DZD/€ → 1 307 000 DZD.
        VehicleListing v = buildVehicle(FuelType.ELECTRIQUE, null, LocalDate.now().minusMonths(5));
        v.setPrice(new BigDecimal("20000"));

        CustomsEstimate estimate = service.estimateCustoms(v);

        assertThat(estimate.getDutyEur()).isEqualByComparingTo("1200.00");
        assertThat(estimate.getVatEur()).isEqualByComparingTo("4028.00");
        assertThat(estimate.getTotalEur()).isEqualByComparingTo("5228.00");
        assertThat(estimate.getTotalDzd()).isEqualByComparingTo("1307000.00");
        assertThat(estimate.getDutyRatePercent()).isEqualByComparingTo("30.00");
    }

    @Test
    void estimateCustoms_appliesSmallEngineDiscount() {
        // Price 15 000 €, 50% essence/hybrid discount:
        // duty = 4500 × 50% = 2250 €; VAT = (15000+2250) × 19% = 3277.50 €;
        // total = 5527.50 €.
        VehicleListing v = buildVehicle(FuelType.ESSENCE, 1800, LocalDate.now().minusMonths(5));
        v.setPrice(new BigDecimal("15000"));

        CustomsEstimate estimate = service.estimateCustoms(v);

        assertThat(estimate.getDutyEur()).isEqualByComparingTo("2250.00");
        assertThat(estimate.getVatEur()).isEqualByComparingTo("3277.50");
        assertThat(estimate.getTotalEur()).isEqualByComparingTo("5527.50");
    }

    private VehicleListing buildVehicle(FuelType fuelType, Integer displacementCm3, LocalDate firstRegistrationDate) {
        VehicleListing v = new VehicleListing();
        v.setFuelType(fuelType);
        v.setEngineDisplacementCm3(displacementCm3);
        v.setFirstRegistrationDate(firstRegistrationDate);
        return v;
    }
}

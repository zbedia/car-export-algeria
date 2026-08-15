package com.carexport.shipping;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShippingCostServiceTest {

    private ShippingCostService service;

    @BeforeEach
    void setUp() {
        service = new ShippingCostService(new BigDecimal("150.00"));
    }

    @Test
    void estimate_returnsBaseFreightPlusHandlingFee() {
        ShippingEstimateResponse result = service.estimate(OriginPort.MARSEILLE, DestinationPort.ALGER);

        assertThat(result.getBaseFreightCost()).isEqualByComparingTo(new BigDecimal("850"));
        assertThat(result.getHandlingFee()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(result.getTotalCost()).isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    @Test
    void estimate_worksForAllSupportedRoutes() {
        for (OriginPort origin : OriginPort.values()) {
            for (DestinationPort destination : DestinationPort.values()) {
                ShippingEstimateResponse result = service.estimate(origin, destination);
                assertThat(result.getTotalCost()).isPositive();
            }
        }
    }
}

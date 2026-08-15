package com.carexport.currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ExchangeRateServiceTest {

    private ExchangeRateService service;

    @BeforeEach
    void setUp() {
        service = new ExchangeRateService(new BigDecimal("145.50"), new BigDecimal("255.00"));
    }

    @Test
    void convert_eurToDzd_usingOfficialRate() {
        ConversionResponse result = service.convert(new BigDecimal("100"), CurrencyCode.EUR, CurrencyCode.DZD, RateType.OFFICIAL);
        assertThat(result.getConvertedAmount()).isEqualByComparingTo(new BigDecimal("14550.00"));
    }

    @Test
    void convert_eurToDzd_usingParallelRate() {
        ConversionResponse result = service.convert(new BigDecimal("100"), CurrencyCode.EUR, CurrencyCode.DZD, RateType.PARALLEL);
        assertThat(result.getConvertedAmount()).isEqualByComparingTo(new BigDecimal("25500.00"));
    }

    @Test
    void convert_dzdToEur_usingOfficialRate() {
        ConversionResponse result = service.convert(new BigDecimal("14550"), CurrencyCode.DZD, CurrencyCode.EUR, RateType.OFFICIAL);
        assertThat(result.getConvertedAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void convert_sameCurrency_returnsSameAmount() {
        ConversionResponse result = service.convert(new BigDecimal("100"), CurrencyCode.EUR, CurrencyCode.EUR, RateType.OFFICIAL);
        assertThat(result.getConvertedAmount()).isEqualByComparingTo(new BigDecimal("100"));
    }
}

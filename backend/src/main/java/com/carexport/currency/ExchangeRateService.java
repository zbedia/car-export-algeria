package com.carexport.currency;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Converts amounts between EUR and DZD using either the official
 * Banque d'Algerie rate or the informal parallel market rate.
 *
 * Rates are configured in application.properties (see currency.rate.*)
 * rather than fetched live, since there is no single authoritative,
 * machine-readable source for the parallel market rate. Update these
 * values periodically to keep conversions accurate.
 */
@Service
public class ExchangeRateService {

    private static final int DZD_SCALE = 2;

    private final BigDecimal officialRateEurToDzd;
    private final BigDecimal parallelRateEurToDzd;
    private final LocalDateTime lastUpdated = LocalDateTime.now();

    public ExchangeRateService(
        @Value("${currency.rate.official-eur-to-dzd}") BigDecimal officialRateEurToDzd,
        @Value("${currency.rate.parallel-eur-to-dzd}") BigDecimal parallelRateEurToDzd
    ) {
        this.officialRateEurToDzd = officialRateEurToDzd;
        this.parallelRateEurToDzd = parallelRateEurToDzd;
    }

    public ExchangeRatesResponse getCurrentRates() {
        return new ExchangeRatesResponse(officialRateEurToDzd, parallelRateEurToDzd, lastUpdated);
    }

    public BigDecimal getRate(RateType rateType) {
        return rateType == RateType.OFFICIAL ? officialRateEurToDzd : parallelRateEurToDzd;
    }

    public ConversionResponse convert(BigDecimal amount, CurrencyCode from, CurrencyCode to, RateType rateType) {
        if (from == to) {
            return new ConversionResponse(amount, from, to, rateType, BigDecimal.ONE, amount);
        }

        BigDecimal rate = getRate(rateType);
        BigDecimal convertedAmount;

        if (from == CurrencyCode.EUR && to == CurrencyCode.DZD) {
            convertedAmount = amount.multiply(rate).setScale(DZD_SCALE, RoundingMode.HALF_UP);
        } else if (from == CurrencyCode.DZD && to == CurrencyCode.EUR) {
            convertedAmount = amount.divide(rate, DZD_SCALE, RoundingMode.HALF_UP);
        } else {
            throw new UnsupportedConversionException(from, to);
        }

        return new ConversionResponse(amount, from, to, rateType, rate, convertedAmount);
    }
}

package com.carexport.currency;

import java.math.BigDecimal;

public class ConversionResponse {
    private BigDecimal originalAmount;
    private CurrencyCode from;
    private CurrencyCode to;
    private RateType rateType;
    private BigDecimal rateUsed;
    private BigDecimal convertedAmount;

    public ConversionResponse(BigDecimal originalAmount, CurrencyCode from, CurrencyCode to,
                               RateType rateType, BigDecimal rateUsed, BigDecimal convertedAmount) {
        this.originalAmount = originalAmount;
        this.from = from;
        this.to = to;
        this.rateType = rateType;
        this.rateUsed = rateUsed;
        this.convertedAmount = convertedAmount;
    }

    public BigDecimal getOriginalAmount() { return originalAmount; }
    public CurrencyCode getFrom() { return from; }
    public CurrencyCode getTo() { return to; }
    public RateType getRateType() { return rateType; }
    public BigDecimal getRateUsed() { return rateUsed; }
    public BigDecimal getConvertedAmount() { return convertedAmount; }
}

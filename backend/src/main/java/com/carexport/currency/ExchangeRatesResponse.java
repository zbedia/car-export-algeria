package com.carexport.currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ExchangeRatesResponse {
    private BigDecimal officialRateEurToDzd;
    private BigDecimal parallelRateEurToDzd;
    private LocalDateTime lastUpdated;

    public ExchangeRatesResponse(BigDecimal officialRateEurToDzd, BigDecimal parallelRateEurToDzd, LocalDateTime lastUpdated) {
        this.officialRateEurToDzd = officialRateEurToDzd;
        this.parallelRateEurToDzd = parallelRateEurToDzd;
        this.lastUpdated = lastUpdated;
    }

    public BigDecimal getOfficialRateEurToDzd() { return officialRateEurToDzd; }
    public BigDecimal getParallelRateEurToDzd() { return parallelRateEurToDzd; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
}

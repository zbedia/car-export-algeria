package com.carexport.currency;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/currency")
public class CurrencyController {

    private final ExchangeRateService exchangeRateService;

    public CurrencyController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping("/rates")
    public ResponseEntity<ExchangeRatesResponse> getRates() {
        return ResponseEntity.ok(exchangeRateService.getCurrentRates());
    }

    @GetMapping("/convert")
    public ResponseEntity<ConversionResponse> convert(
        @RequestParam BigDecimal amount,
        @RequestParam CurrencyCode from,
        @RequestParam CurrencyCode to,
        @RequestParam(defaultValue = "OFFICIAL") RateType rateType
    ) {
        return ResponseEntity.ok(exchangeRateService.convert(amount, from, to, rateType));
    }
}

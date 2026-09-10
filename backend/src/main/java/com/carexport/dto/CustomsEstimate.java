package com.carexport.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Estimated customs cost of importing a vehicle into Algeria, with the
 * applicable discount already applied.
 *
 * Model (indicated estimates — rates come from application.properties):
 * - customs duty = purchase price × base duty rate × (1 − discount%),
 *   where the discount tier (electric −80%, small essence/hybrid −50%,
 *   large essence/hybrid −20%) is the ImportEligibilityService logic.
 * - VAT = (price + discounted duty) × VAT rate.
 * - DZD amount = EUR amount × configured exchange rate, for display
 *   alongside the EUR figure on each vehicle card.
 */
@Data
public class CustomsEstimate {
    private BigDecimal dutyEur;
    private BigDecimal vatEur;
    private BigDecimal totalEur;
    private BigDecimal totalDzd;
    private BigDecimal dutyRatePercent;
}
package com.carexport.model;

/**
 * Machine-readable reason code for why a vehicle got its specific customs
 * discount tier. Kept separate from the percentage itself so the frontend
 * can render a fully translated explanation in any supported language,
 * instead of receiving a pre-built English sentence.
 */
public enum CustomsDiscountReasonCode {
    ELECTRIC,
    DIESEL_NOT_ELIGIBLE,
    SMALL_ENGINE,
    LARGE_ENGINE
}

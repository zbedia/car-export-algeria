package com.carexport.scraping;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.math.BigDecimal;

/**
 * Extracts the selling price of an Auto Export Marseille detail page.
 *
 * The site displays a flat « 8 990 € » price (hors taxes) inside
 * {@code .vehica-car-price}. As with every vehicle listed on the site, the
 * price is in EUR, so the amount is parsed—digits only, immune to the broken
 * charset that turns « € » into a U+FFFD replacement char—and tagged EUR.
 */
final class AutoExportMarseillePriceParser {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    /**
     * @param amount   parsed amount, {@link BigDecimal#ZERO} if not found
     * @param currency ISO code, always {@code "EUR"} for this source
     */
    record ParsedPrice(BigDecimal amount, String currency) {}

    ParsedPrice extract(Document doc) {
        Element priceEl = doc.selectFirst(AutoExportMarseilleSelectors.PRICE);
        if (priceEl != null) {
            BigDecimal amount = parseAmount(priceEl.text());
            if (amount.compareTo(ZERO) > 0) {
                return new ParsedPrice(amount, "EUR");
            }
        }
        return new ParsedPrice(ZERO, "EUR");
    }

    private BigDecimal parseAmount(String rawValue) {
        if (rawValue == null) {
            return ZERO;
        }
        // « 8 990 € » / « 8 990€ » / « 8.990 € » -> 8990. All non-digits go away.
        String clean = rawValue.replaceAll("[^0-9]", "");
        if (clean.isEmpty()) {
            return ZERO;
        }
        try {
            return new BigDecimal(clean);
        } catch (NumberFormatException e) {
            return ZERO;
        }
    }
}
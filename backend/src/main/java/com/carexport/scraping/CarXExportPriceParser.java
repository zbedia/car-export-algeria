package com.carexport.scraping;

import com.fasterxml.jackson.databind.JsonNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Extracts the selling price of a CarXport detail page.
 *
 * The site lists cars in SEK (« 231 920 SEK ») next to an indicative euro
 * conversion for export buyers (« ~ € 20,893 », Riksbank rate). Everything
 * downstream reasons in EUR (eligibility, shipping, comparison between
 * sources), so the CarXport conversion wins and the native currency only
 * serves as a fallback.
 */
final class CarXExportPriceParser {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    /**
     * @param amount   parsed amount, {@link BigDecimal#ZERO} if not found
     * @param currency ISO code (EUR, SEK, ...), {@code "EUR"} as a default
     */
    record ParsedPrice(BigDecimal amount, String currency) {}

    ParsedPrice extract(Document doc, JsonNode vehicleNode, Map<String, String> specs) {
        // 1. Indicative EUR conversion displayed by the site: « ~ € 20,893 »
        //    (thousands separated by a comma, Swedish format)
        String eurText = selectText(doc, CarXExportSelectors.PRICE_EUR_HINT);
        if (eurText.contains("€")) {
            BigDecimal p = parseWholesaleAmount(eurText);
            if (p.compareTo(ZERO) > 0) return new ParsedPrice(p, "EUR");
        }

        // 2. schema.org JSON-LD: exact price + native currency (SEK / EUR / ...)
        if (vehicleNode != null && vehicleNode.has("offers")) {
            JsonNode offers = vehicleNode.path("offers");
            String currency = offers.path("priceCurrency").asText("");
            if (!currency.isBlank()) {
                BigDecimal p = parsePrice(offers.path("price").asText(null));
                if (p.compareTo(ZERO) > 0) return new ParsedPrice(p, currency.toUpperCase());
            }
        }

        // 3. Visible price block in Swedish currency: « 231 920 SEK »
        for (Element priceEl : doc.select(CarXExportSelectors.PRICE_NATIVE_PARAGRAPHS)) {
            String text = priceEl.text().trim();
            if (text.matches("(?i)^[\\d.,\\s\\u00a0]+\\s*SEK$")) {
                BigDecimal p = parseWholesaleAmount(text);
                if (p.compareTo(ZERO) > 0) return new ParsedPrice(p, "SEK");
            }
        }

        // 4. Spec grid, if a total price is listed there
        for (Map.Entry<String, String> entry : specs.entrySet()) {
            String key = entry.getKey().toLowerCase();
            String val = entry.getValue();
            if ((key.contains("prix") || key.contains("price") || val.contains("€") || val.contains("SEK"))
                    && !val.isBlank()) {
                BigDecimal p = parseWholesaleAmount(val);
                if (p.compareTo(ZERO) > 0) {
                    return new ParsedPrice(p, val.contains("€") ? "EUR" : "SEK");
                }
            }
        }

        return new ParsedPrice(ZERO, "EUR");
    }

    private String selectText(Document doc, String selector) {
        Element el = doc.selectFirst(selector);
        return el != null ? el.text().trim() : "";
    }

    private BigDecimal parsePrice(String rawPrice) {
        if (rawPrice == null || rawPrice.isBlank()) {
            return ZERO;
        }
        String clean = rawPrice.replaceAll("[^0-9.,]", "").replace(",", ".");
        try {
            return new BigDecimal(clean);
        } catch (NumberFormatException e) {
            return ZERO;
        }
    }

    /**
     * Parses an integer amount written in Swedish thousands notation (non-breaking
     * spaces, comma as the thousands separator): all non-digit characters are
     * removed (« 231 920 SEK » -> 231920, « ~ € 20,893 » -> 20893).
     */
    private BigDecimal parseWholesaleAmount(String rawValue) {
        if (rawValue == null) {
            return ZERO;
        }
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
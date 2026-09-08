package com.carexport.scraping.configdriven;

import java.util.List;
import java.util.Map;

/**
 * Immutable configuration of one config-driven scraping source, as declared in
 * {@code scraping-sources.yml}.
 *
 * @param sitemapUrl            URL of the catalogue sitemap used to enumerate
 *                              every detail page URL (Rank Math / Yoast format)
 * @param offerUrlMarker        substring that isolates offer URLs from the rest
 *                              of the sitemap (e.g. « /annonce/ »)
 * @param currency              ISO 4217 code the site prices its cars in
 * @param garageCity            city stamped on every listing (single-garage sources)
 * @param priceSelector         CSS selector of the price element
 * @param titleSelector         CSS selector of the page headline (mileage fallback)
 * @param titleMileagePattern   regex with one capture group pulling a mileage
 *                              figure out of the headline, e.g. « … 25000kms »
 * @param dateFormat            format of the « first registration » spec value
 * @param politenessMs          sleep between two detail-page requests
 * @param requestTimeoutMs      per-request connect/read timeout
 * @param fetchAttempts         sitemap fetch retries before giving up
 * @param retryDelayMs          wait before a sitemap retry
 * @param userAgent             User-Agent header used on every request
 * @param specGrid              selectors, label keys and fallbacks of the
 *                              label/value attribute grid rendered by Vehicle
 *                              themes
 * @param skipWhenNotVehicle    drop listings carrying no vehicle data at all —
 *                              year 0 AND price 0 — typically accessories/parts
 *                              pages the sitemap also lists
 * @param fuelMapping           keyword lists per fuel type, matched against
 *                              the normalized (« Électrique » → « LECTRIQUE »)
 *                              raw spec value
 */
public record SourceConfig(
        String name,
        String sitemapUrl,
        String offerUrlMarker,
        String currency,
        String garageCity,
        String priceSelector,
        String titleSelector,
        String titleMileagePattern,
        String dateFormat,
        long politenessMs,
        long requestTimeoutMs,
        int fetchAttempts,
        long retryDelayMs,
        String userAgent,
        SpecGrid specGrid,
        boolean skipWhenNotVehicle,
        Map<String, List<String>> fuelMapping) {

    /**
     * @param nameSelector              selector of the grid cells holding the
     *                                  attribute label (normalized before matching)
     * @param valueSelector             selector of the grid cells holding the
     *                                  attribute value (zipped with {@code nameSelector}
     *                                  in document order)
     * @param labels                    field → ASCII-normalized label key, so the
     *                                  parser only relies on accent-free spellings
     * @param displacementFallbackKeys  extra label keys tried for displacement
     *                                  when the primary one is absent (e.g. « moteur »)
     */
    public record SpecGrid(
            String nameSelector,
            String valueSelector,
            Map<String, String> labels,
            List<String> displacementFallbackKeys) {

        public String labelFor(String field) {
            return labels.getOrDefault(field, "");
        }
    }
}
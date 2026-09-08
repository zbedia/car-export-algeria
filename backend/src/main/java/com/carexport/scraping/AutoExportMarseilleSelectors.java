package com.carexport.scraping;

/**
 * CSS selectors and spec label keys of the Auto Export Marseille marketplace
 * (autoexportmarseille.com, WordPress + Vehica/Elementor theme).
 *
 * Kept in one place so a site markup change is a single edit point and the
 * parsing code reads without a wall of inline selector literals.
 *
 * The site declares UTF-8 but actually serves windows-1252 bytes, so every
 * accented character arrives in the page as the U+FFFD replacement character.
 * Label keys are therefore matched in ASCII-normalized form (accents and any
 * non-ASCII byte stripped) — see {@link AutoExportMarseilleSpecGridParser}.
 */
final class AutoExportMarseilleSelectors {

    // --- Catalogue: vehicle URLs are enumerated via the Rank Math sitemap ---
    static final String SITEMAP_URL = "https://autoexportmarseille.com/vehica_car-sitemap.xml";
    static final String SITEMAP_LOC_SELECTOR = "loc";
    static final String OFFER_URL_MARKER = "/annonce/";

    // --- Detail page: label/value grid rendered by the Vehica theme ---
    static final String SPEC_CONTAINER = ".vehica-car-attributes";
    static final String SPEC_ATTRIBUTE_NAME = ".vehica-car-attributes__name";
    static final String SPEC_ATTRIBUTE_VALUE = ".vehica-car-attributes__values";

    // --- Detail page: price block (e.g. « 8 990 € ») ---
    static final String PRICE = ".vehica-car-price";

    // --- Detail page: headline, used as a mileage fallback (« … 25000kms ») ---
    static final String TITLE = "h1";

    // --- Spec label keys, ASCII-stripped as they survive the broken charset ---
    // Stripping removes the accented byte, so both the broken and the correct
    // encoding collapse to the same key: « Modèle » and « Mod�le » both -> "modle",
    // « Année » and « Ann�e » both -> "anne". Keys must stay this short because
    // accents are gone from the page either way.
    static final String LABEL_BRAND = "marque";
    static final String LABEL_MODEL = "modle";
    static final String LABEL_YEAR = "anne";
    static final String LABEL_FUEL = "carburant";
    static final String LABEL_MILEAGE = "kilometrage";
    static final String LABEL_DISPLACEMENT = "cylindree";
    static final String LABEL_REGISTRATION = "mise en circulation";

    // --- Single-garage site: used as the listing city when the page shows none ---
    static final String DEFAULT_CITY = "Marseille";

    private AutoExportMarseilleSelectors() {
    }
}
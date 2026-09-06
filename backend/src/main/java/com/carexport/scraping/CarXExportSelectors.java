package com.carexport.scraping;

/**
 * CSS selectors and spec label keys of the CarXport marketplace (carxexport.com).
 *
 * Kept in one place so a site markup change is a single edit point and the
 * parsing code reads without a wall of inline selector literals.
 */
final class CarXExportSelectors {

    // --- Listing page: links to individual offer pages ---
    static final String LISTING_CARD_LINKS =
            "a[href*='/offers/'], a[href*='/offer/'], a.vehicle-card, a.offer-card, " +
                    ".vehicle-item a, .car-card a";

    // --- Brand / model / meta fallbacks on a detail page ---
    static final String BRAND = ".vehicle-brand, .brand, [itemprop=brand]";
    static final String MODEL = ".vehicle-model, .model, h1.vehicle-title, h1.title, .page-title";
    static final String YEAR = ".vehicle-year, .year, .spec-year";
    static final String MILEAGE = ".vehicle-mileage, .mileage, .km";
    static final String FUEL = ".vehicle-fuel, .fuel, .carburant";
    static final String CITY = ".vehicle-location, .city, .location, .garage-city";

    // --- Structured data (schema.org Vehicle JSON-LD) ---
    static final String JSON_LD_SCRIPTS = "script[type=application/ld+json]";

    // --- Price block ---
    // Indicative EUR conversion shown to export buyers: « ~ € 20,893 »
    static final String PRICE_EUR_HINT = "p[dir=ltr]";
    // Any paragraph, used to spot « 231 920 SEK » style native prices.
    static final String PRICE_NATIVE_PARAGRAPHS = "p";

    // --- Spec grid extraction strategies (tried in order of preference) ---
    static final String SPEC_TABLE_ROWS =
            "table.specs-table tr, table.vehicle-specs tr, table.table tr, .specifications table tr";
    static final String SPEC_TABLE_LABEL_CELLS = "th, td.label, td.key, .title";
    static final String SPEC_TABLE_VALUE_CELLS = "td:not(.label):not(.key), td.value, .val";
    static final String SPEC_DL_DTS = ".spec-grid dt, .vehicle-specs dt, dl.specs dt";
    static final String SPEC_ITEMS = ".spec-grid .spec-item, .vehicle-specs .spec-item, " +
            ".car-details .detail-item, .specs-list .spec-row, .info-grid .info-item, " +
            ".technical-data .data-row";
    static final String SPEC_ITEM_LABEL = ".label, .title, .key, .spec-label, span:first-child";
    static final String SPEC_ITEM_VALUE = ".value, .data, .spec-value, span:last-child";
    // Tailwind markup: <p class="... text-fog">label</p> followed by the value.
    // "text-fog" is the only stable class (font size varies: text-xs, text-[10px]...).
    static final String SPEC_TAILWIND_LABEL = "p.text-fog";

    // --- Spec label keys, as spelled by the CarXport markup (FR + EN) ---
    static final String LABEL_BRAND_FR = "Marque";
    static final String LABEL_BRAND_EN = "Make";
    static final String LABEL_MODEL_FR = "Modèle";
    static final String LABEL_MODEL_EN = "Model";
    static final String LABEL_YEAR_FR = "Année";
    static final String LABEL_YEAR_EN = "Year";
    static final String LABEL_REGISTRATION_FR = "Mise en circulation";
    static final String LABEL_REGISTRATION_FR_2 = "Première immatriculation";
    static final String LABEL_REGISTRATION_FR_3 = "1ère immatriculation";
    static final String LABEL_MILEAGE_FR = "Kilométrage";
    static final String LABEL_MILEAGE_EN = "Mileage";
    static final String LABEL_FUEL_FR = "Carburant";
    static final String LABEL_FUEL_FR_2 = "Énergie";
    static final String LABEL_FUEL_EN = "Fuel";
    static final String LABEL_DISPLACEMENT_FR = "Cylindrée";
    static final String LABEL_DISPLACEMENT_EN = "Engine Size";
    static final String LABEL_CITY_FR = "Ville";
    static final String LABEL_CITY_FR_2 = "Localisation";
    static final String LABEL_CITY_EN = "Location";

    private CarXExportSelectors() {
    }
}
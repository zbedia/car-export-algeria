package com.carexport.scraping;

import com.carexport.model.FuelType;
import com.carexport.model.VehicleListing;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns a fetched Auto Export Marseille detail page into a {@link VehicleListing}.
 *
 * Pure parsing: network access, retries and politeness delays live in
 * {@link AutoExportMarseilleConnector}, page structure knowledge lives in
 * {@link AutoExportMarseilleSelectors} and {@link AutoExportMarseilleSpecGridParser}.
 *
 * The site never exposes engine displacement in its spec grid, so that field
 * stays {@code null} for this source (customs discount simply does not apply).
 */
final class AutoExportMarseilleVehicleParser {

    private static final DateTimeFormatter FRENCH_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final String sourceName;
    private final AutoExportMarseilleSpecGridParser specGridParser = new AutoExportMarseilleSpecGridParser();
    private final AutoExportMarseillePriceParser priceParser = new AutoExportMarseillePriceParser();

    AutoExportMarseilleVehicleParser(String sourceName) {
        this.sourceName = sourceName;
    }

    VehicleListing parse(Document detailDoc, String detailUrl) {
        Map<String, String> specs = specGridParser.parse(detailDoc);

        VehicleListing v = new VehicleListing();
        v.setSource(sourceName);
        v.setExternalUrl(detailUrl);

        // --- Brand & Model ---
        v.setBrand(firstNonBlank(specs.get(AutoExportMarseilleSelectors.LABEL_BRAND), ""));
        v.setModel(firstNonBlank(specs.get(AutoExportMarseilleSelectors.LABEL_MODEL), ""));

        // --- Year ---
        v.setYear(parseIntegerSafely(specs.get(AutoExportMarseilleSelectors.LABEL_YEAR)));

        // --- Mileage: attribute when present, headline fallback (« 25000kms ») ---
        int mileage = parseIntegerSafely(specs.get(AutoExportMarseilleSelectors.LABEL_MILEAGE));
        if (mileage == 0) {
            mileage = parseIntegerSafely(extractTitleMileage(detailDoc));
        }
        v.setMileageKm(mileage);

        // --- Fuel ---
        v.setFuelType(parseFuelType(specs.get(AutoExportMarseilleSelectors.LABEL_FUEL)));

        // --- Price: flat EUR « 8 990 € », hors taxes ---
        AutoExportMarseillePriceParser.ParsedPrice price = priceParser.extract(detailDoc);
        v.setPrice(price.amount());
        v.setCurrency(price.currency());

        // --- Engine displacement: never exposed by this site ---
        String displacementSpec = firstNonBlank(
                specs.get(AutoExportMarseilleSelectors.LABEL_DISPLACEMENT),
                specs.get("moteur"));
        int displacementCm3 = parseDisplacementCm3(displacementSpec);
        v.setEngineDisplacementCm3(displacementCm3 > 0 ? displacementCm3 : null);

        // --- First registration date, defaulting to the 1st of January of the
        //     advertised year when the site only ships « Année » ---
        v.setFirstRegistrationDate(parseFrenchDate(specs.get(AutoExportMarseilleSelectors.LABEL_REGISTRATION), v.getYear()));

        // --- Single-garage marketplace: fixed location ---
        v.setGarageCity(AutoExportMarseilleSelectors.DEFAULT_CITY);

        v.setImageUrl(ListingImageExtractor.extract(null, detailDoc));

        v.setScrapedAt(LocalDateTime.now());

        return v;
    }

    private String firstNonBlank(String primary, String fallback) {
        return primary != null && !primary.isBlank() ? primary.trim() : fallback;
    }

    private String extractTitleMileage(Document doc) {
        Element title = doc.selectFirst(AutoExportMarseilleSelectors.TITLE);
        if (title == null) {
            return "";
        }
        Matcher matcher = TITLE_MILEAGE.matcher(title.text());
        return matcher.find() ? matcher.group(1) : "";
    }

    private int parseIntegerSafely(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(rawValue.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static final Pattern TITLE_MILEAGE = Pattern.compile("(?i)(\\d{3,})\\s*kms?");
    private static final Pattern DISPLACEMENT_LITRES =
            Pattern.compile("(?<num>[0-9]+(?:[.,][0-9]+)?)\\s*[Ll]");
    private static final Pattern DISPLACEMENT_CC =
            Pattern.compile("(?<num>[0-9]+)\\s*(?:cm3|cc|cm³)", Pattern.CASE_INSENSITIVE);
    private static final Pattern BARE_COMMA_DECIMAL = Pattern.compile("(?<int>[0-9]+),(?<frac>[0-9]+)");
    private static final int CM3_PER_LITRE = 1000;
    private static final int MAX_BARE_DECIMAL_LITRES = 20;

    /**
     * Parses an engine displacement in cm³ (same notation as CarXport:
     * « 1,2 L », « 1981 cm3 », bare « 2,0 » as litres). Returns 0 when absent.
     */
    private int parseDisplacementCm3(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return 0;
        }
        String trimmed = rawValue.trim();

        Matcher litres = DISPLACEMENT_LITRES.matcher(trimmed);
        if (litres.find()) {
            return litresToCm3(litres.group("num"));
        }

        Matcher cc = DISPLACEMENT_CC.matcher(trimmed);
        if (cc.find()) {
            return parseIntegerSafely(cc.group("num"));
        }

        Matcher bareDecimal = BARE_COMMA_DECIMAL.matcher(trimmed);
        if (bareDecimal.matches() && Integer.parseInt(bareDecimal.group("int")) <= MAX_BARE_DECIMAL_LITRES) {
            return litresToCm3(trimmed);
        }

        return parseIntegerSafely(trimmed);
    }

    private int litresToCm3(String decimalValue) {
        BigDecimal litres = new BigDecimal(decimalValue.replace(",", "."));
        return litres.multiply(BigDecimal.valueOf(CM3_PER_LITRE)).setScale(0, RoundingMode.HALF_UP).intValue();
    }

    private LocalDate parseFrenchDate(String rawDate, int fallbackYear) {
        if (rawDate != null && !rawDate.isBlank()) {
            try {
                return LocalDate.parse(rawDate.trim(), FRENCH_DATE_FORMAT);
            } catch (DateTimeParseException ignored) {
                // fall through to the fallback year
            }
        }
        return fallbackYear > 0 ? LocalDate.of(fallbackYear, 1, 1) : LocalDate.now();
    }

    private FuelType parseFuelType(String rawValue) {
        if (rawValue == null) {
            return FuelType.ESSENCE;
        }
        // « Électrique » arrives as « �lectrique »: strip non-ASCII first.
        String normalized = rawValue.replaceAll("[^\\p{ASCII}]", "").toUpperCase();
        if (normalized.contains("HYBR")) return FuelType.HYBRIDE;
        if (normalized.contains("LECTR")) return FuelType.ELECTRIQUE;
        if (normalized.contains("DIES")) return FuelType.DIESEL;
        return FuelType.ESSENCE;
    }
}
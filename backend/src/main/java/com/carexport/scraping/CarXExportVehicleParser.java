package com.carexport.scraping;

import com.carexport.model.FuelType;
import com.carexport.model.VehicleListing;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * Turns a fetched CarXport detail page into a {@link VehicleListing}.
 *
 * Pure parsing: network access, retries and politeness delays live in
 * {@link CarXExportConnector}, page structure knowledge lives in
 * {@link CarXExportSelectors}, {@link CarXExportSpecGridParser} and
 * {@link CarXExportPriceParser}.
 */
final class CarXExportVehicleParser {

    private static final DateTimeFormatter FRENCH_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FRENCH_ABBREV_DATE_FORMAT =
            DateTimeFormatter.ofPattern("d MMM yyyy", java.util.Locale.FRENCH);

    private final String sourceName;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CarXExportSpecGridParser specGridParser = new CarXExportSpecGridParser();
    private final CarXExportPriceParser priceParser = new CarXExportPriceParser();

    CarXExportVehicleParser(String sourceName) {
        this.sourceName = sourceName;
    }

    VehicleListing parse(Document detailDoc, String detailUrl) {
        JsonNode vehicleNode = extractVehicleJsonLd(detailDoc);
        Map<String, String> specs = specGridParser.parse(detailDoc);

        VehicleListing v = new VehicleListing();
        v.setSource(sourceName);
        v.setExternalUrl(detailUrl);

        // --- Marque & Modèle ---
        String brand = extractFirstValid(
                vehicleNode != null ? vehicleNode.path("brand").path("name").asText(null) : null,
                specs.get(CarXExportSelectors.LABEL_BRAND_FR),
                specs.get(CarXExportSelectors.LABEL_BRAND_EN),
                selectText(detailDoc, CarXExportSelectors.BRAND)
        );

        String model = extractFirstValid(
                vehicleNode != null ? vehicleNode.path("model").asText(null) : null,
                specs.get(CarXExportSelectors.LABEL_MODEL_FR),
                specs.get(CarXExportSelectors.LABEL_MODEL_EN),
                selectText(detailDoc, CarXExportSelectors.MODEL)
        );

        v.setBrand(brand);
        v.setModel(model);

        // --- Année & Date ---
        int year = 0;
        if (vehicleNode != null) {
            year = vehicleNode.path("vehicleModelDate").asInt(0);
        }
        if (year == 0) {
            year = parseIntegerSafely(specs.getOrDefault(CarXExportSelectors.LABEL_YEAR_FR,
                    specs.get(CarXExportSelectors.LABEL_YEAR_EN)));
        }
        if (year == 0) {
            year = parseIntegerSafely(selectText(detailDoc, CarXExportSelectors.YEAR));
        }
        v.setYear(year);

        String regDateStr = specs.getOrDefault(CarXExportSelectors.LABEL_REGISTRATION_FR,
                specs.getOrDefault(CarXExportSelectors.LABEL_REGISTRATION_FR_2,
                        specs.get(CarXExportSelectors.LABEL_REGISTRATION_FR_3)));
        v.setFirstRegistrationDate(parseFrenchDate(regDateStr, year));

        // --- Kilométrage ---
        int mileage = 0;
        if (vehicleNode != null) {
            mileage = vehicleNode.path("mileageFromOdometer").path("value").asInt(0);
        }
        if (mileage == 0) {
            mileage = parseIntegerSafely(specs.getOrDefault(CarXExportSelectors.LABEL_MILEAGE_FR,
                    specs.get(CarXExportSelectors.LABEL_MILEAGE_EN)));
        }
        if (mileage == 0) {
            mileage = parseIntegerSafely(selectText(detailDoc, CarXExportSelectors.MILEAGE));
        }
        v.setMileageKm(mileage);

        // --- Carburant ---
        String rawFuel = extractFirstValid(
                vehicleNode != null ? vehicleNode.path("fuelType").asText(null) : null,
                specs.get(CarXExportSelectors.LABEL_FUEL_FR),
                specs.get(CarXExportSelectors.LABEL_FUEL_FR_2),
                specs.get(CarXExportSelectors.LABEL_FUEL_EN),
                selectText(detailDoc, CarXExportSelectors.FUEL)
        );
        v.setFuelType(parseFuelType(rawFuel));

        // --- Prix : EUR prioritaire (conversion CarXport pour l'export), sinon devise native ---
        CarXExportPriceParser.ParsedPrice price = priceParser.extract(detailDoc, vehicleNode, specs);
        v.setPrice(price.amount());
        v.setCurrency(price.currency());

        // --- Cylindrée & Ville ---
        v.setEngineDisplacementCm3(parseIntegerSafely(
                specs.getOrDefault(CarXExportSelectors.LABEL_DISPLACEMENT_FR,
                        specs.get(CarXExportSelectors.LABEL_DISPLACEMENT_EN))));

        String city = extractFirstValid(
                specs.get(CarXExportSelectors.LABEL_CITY_FR),
                specs.get(CarXExportSelectors.LABEL_CITY_FR_2),
                specs.get(CarXExportSelectors.LABEL_CITY_EN),
                selectText(detailDoc, CarXExportSelectors.CITY)
        );
        v.setGarageCity(city);

        v.setImageUrl(ListingImageExtractor.extract(vehicleNode, detailDoc));

        v.setScrapedAt(LocalDateTime.now());

        return v;
    }

    private JsonNode extractVehicleJsonLd(Document doc) {
        Elements scripts = doc.select(CarXExportSelectors.JSON_LD_SCRIPTS);
        for (Element script : scripts) {
            try {
                JsonNode root = objectMapper.readTree(script.data());
                if (root.isArray()) {
                    for (JsonNode node : root) {
                        if (isVehicleNode(node)) return node;
                    }
                } else {
                    if (isVehicleNode(root)) return root;
                    for (JsonNode node : root.path("@graph")) {
                        if (isVehicleNode(node)) return node;
                    }
                }
            } catch (Exception ignored) {
                // Malformed JSON block; try the next script tag.
            }
        }
        return null;
    }

    private boolean isVehicleNode(JsonNode node) {
        String type = node.path("@type").asText("");
        return "Vehicle".equalsIgnoreCase(type) || "Car".equalsIgnoreCase(type) || "Product".equalsIgnoreCase(type);
    }

    private String selectText(Document doc, String selector) {
        Element el = doc.selectFirst(selector);
        return el != null ? el.text().trim() : "";
    }

    private String extractFirstValid(String... candidates) {
        for (String c : candidates) {
            if (c != null && !c.isBlank()) {
                return c.trim();
            }
        }
        return "";
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

    private LocalDate parseFrenchDate(String rawDate, int fallbackYear) {
        if (rawDate != null && !rawDate.isBlank()) {
            try {
                return LocalDate.parse(rawDate.trim(), FRENCH_DATE_FORMAT);
            } catch (DateTimeParseException ignored) {
                // try the abbreviated format below
            }
            try {
                return LocalDate.parse(rawDate.trim(), FRENCH_ABBREV_DATE_FORMAT);
            } catch (DateTimeParseException ignored) {
                // fall through to the fallback year
            }
        }
        return fallbackYear > 0 ? LocalDate.of(fallbackYear, 1, 1) : LocalDate.now();
    }

    private FuelType parseFuelType(String rawValue) {
        if (rawValue == null) return FuelType.ESSENCE;
        String normalized = rawValue.trim().toUpperCase();
        if (normalized.contains("HYBR")) return FuelType.HYBRIDE;
        if (normalized.contains("ELEC") || normalized.contains("ÉLEC")) return FuelType.ELECTRIQUE;
        if (normalized.contains("DIES")) return FuelType.DIESEL;
        return FuelType.ESSENCE;
    }
}
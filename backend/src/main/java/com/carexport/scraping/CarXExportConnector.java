package com.carexport.scraping;

import com.carexport.exception.ScrapingException;
import com.carexport.model.FuelType;
import com.carexport.model.VehicleListing;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Connector for carxexport.com configured to extract EUR price tags directly.
 */
@Component
public class CarXExportConnector implements VehicleSourceConnector {

    private static final Logger log = LoggerFactory.getLogger(CarXExportConnector.class);

    private static final String BASE_URL = "https://carxexport.com/fr/offers";
    private static final String SOURCE_NAME = "CarXExport";
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36";
    private static final int REQUEST_TIMEOUT_MS = 30_000;
    private static final int LISTING_FETCH_ATTEMPTS = 3;
    private static final long LISTING_RETRY_DELAY_MS = 2_000;
    private static final long DETAIL_REQUEST_DELAY_MS = 300;
    private static final DateTimeFormatter FRENCH_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FRENCH_ABBREV_DATE_FORMAT =
            DateTimeFormatter.ofPattern("d MMM yyyy", java.util.Locale.FRENCH);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getSourceName() {
        return SOURCE_NAME;
    }

    @Override
    public List<VehicleListing> fetchListings(SearchCriteria criteria) {
        List<String> detailUrls = fetchDetailUrls();
        log.info("[{}] Found {} vehicle cards on the listing page", SOURCE_NAME, detailUrls.size());

        List<VehicleListing> results = new ArrayList<>();
        int failures = 0;
        for (String detailUrl : detailUrls) {
            try {
                results.add(fetchVehicleDetail(detailUrl));
            } catch (Exception e) {
                failures++;
                log.warn("[{}] Failed to parse detail page {}: {}", SOURCE_NAME, detailUrl, e.getMessage());
            }
            politeDelay();
        }
        log.info("[{}] Finished: {} vehicles parsed successfully, {} failed", SOURCE_NAME, results.size(), failures);
        return results;
    }

    private List<String> fetchDetailUrls() {
        IOException lastError = null;
        for (int attempt = 1; attempt <= LISTING_FETCH_ATTEMPTS; attempt++) {
            try {
                Document listingDoc = Jsoup.connect(BASE_URL)
                        .userAgent(USER_AGENT)
                        .timeout(REQUEST_TIMEOUT_MS)
                        .get();

                Elements cardLinks = listingDoc.select(
                        "a[href*='/offers/'], a[href*='/offer/'], " +
                                "a.vehicle-card, a.offer-card, .vehicle-item a, .car-card a"
                );

                List<String> urls = new ArrayList<>();
                for (Element link : cardLinks) {
                    String href = link.attr("abs:href");
                    if (!href.isBlank()
                            && !urls.contains(href)
                            && !href.equalsIgnoreCase(BASE_URL)
                            && !href.contains("page=")) {
                        urls.add(href);
                    }
                }
                return urls;
            } catch (IOException e) {
                lastError = e;
                log.warn("[{}] Listing page fetch failed (attempt {}/{}): {}", SOURCE_NAME, attempt,
                        LISTING_FETCH_ATTEMPTS, e.getMessage());
                if (attempt < LISTING_FETCH_ATTEMPTS) {
                    sleepQuietly(LISTING_RETRY_DELAY_MS);
                }
            }
        }
        throw new ScrapingException(getSourceName(), lastError);
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private VehicleListing fetchVehicleDetail(String detailUrl) throws IOException {
        Document detailDoc = Jsoup.connect(detailUrl)
                .userAgent(USER_AGENT)
                .timeout(REQUEST_TIMEOUT_MS)
                .get();

        JsonNode vehicleNode = extractVehicleJsonLd(detailDoc);
        Map<String, String> specs = parseSpecGrid(detailDoc);

        VehicleListing v = new VehicleListing();
        v.setSource(getSourceName());
        v.setExternalUrl(detailUrl);

        // --- Marque & Modèle ---
        String brand = extractFirstValid(
                vehicleNode != null ? vehicleNode.path("brand").path("name").asText(null) : null,
                specs.get("Marque"),
                specs.get("Make"),
                selectText(detailDoc, ".vehicle-brand, .brand, [itemprop=brand]")
        );

        String model = extractFirstValid(
                vehicleNode != null ? vehicleNode.path("model").asText(null) : null,
                specs.get("Modèle"),
                specs.get("Model"),
                selectText(detailDoc, ".vehicle-model, .model, h1.vehicle-title, h1.title, .page-title")
        );

        v.setBrand(brand);
        v.setModel(model);

        // --- Année & Date ---
        int year = 0;
        if (vehicleNode != null) {
            year = vehicleNode.path("vehicleModelDate").asInt(0);
        }
        if (year == 0) {
            year = parseIntegerSafely(specs.getOrDefault("Année", specs.get("Year")));
        }
        if (year == 0) {
            year = parseIntegerSafely(selectText(detailDoc, ".vehicle-year, .year, .spec-year"));
        }
        v.setYear(year);

        String regDateStr = specs.getOrDefault("Mise en circulation",
                specs.getOrDefault("Première immatriculation", specs.get("1ère immatriculation")));
        v.setFirstRegistrationDate(parseFrenchDate(regDateStr, year));

        // --- Kilométrage ---
        int mileage = 0;
        if (vehicleNode != null) {
            mileage = vehicleNode.path("mileageFromOdometer").path("value").asInt(0);
        }
        if (mileage == 0) {
            mileage = parseIntegerSafely(specs.getOrDefault("Kilométrage", specs.get("Mileage")));
        }
        if (mileage == 0) {
            mileage = parseIntegerSafely(selectText(detailDoc, ".vehicle-mileage, .mileage, .km"));
        }
        v.setMileageKm(mileage);

        // --- Carburant ---
        String rawFuel = extractFirstValid(
                vehicleNode != null ? vehicleNode.path("fuelType").asText(null) : null,
                specs.get("Carburant"),
                specs.get("Énergie"),
                specs.get("Fuel"),
                selectText(detailDoc, ".vehicle-fuel, .fuel, .carburant")
        );
        v.setFuelType(parseFuelType(rawFuel));

        // --- Prix : EUR prioritaire (conversion CarXport pour l'export), sinon devise native ---
        PriceInfo priceInfo = extractPrice(detailDoc, vehicleNode, specs);
        v.setPrice(priceInfo.amount());
        v.setCurrency(priceInfo.currency());

        // --- Cylindrée & Ville ---
        v.setEngineDisplacementCm3(parseIntegerSafely(specs.getOrDefault("Cylindrée", specs.get("Engine Size"))));

        String city = extractFirstValid(
                specs.get("Ville"),
                specs.get("Localisation"),
                specs.get("Location"),
                selectText(detailDoc, ".vehicle-location, .city, .location, .garage-city")
        );
        v.setGarageCity(city);

        v.setImageUrl(ListingImageExtractor.extract(vehicleNode, detailDoc));

        v.setScrapedAt(LocalDateTime.now());

        return v;
    }

    /**
     * Extrait le prix au format EUR. Le site annonce le prix de vente en SEK
     * (« 231 920 SEK ») et affiche à côté une conversion indicative en euros
     * (« ~ € 20,893 », taux Riksbank) destinée aux acheteurs à l'export.
     * L'application raisonnant en EUR (éligibilité, transport, comparaison
     * entre sources), la conversion CarXport est prioritaire et la devise
     * native ne sert que de repli si la conversion est absente.
     */
    private PriceInfo extractPrice(Document doc, JsonNode vehicleNode, Map<String, String> specs) {
        // 1. Conversion indicative en euros affichée par le site : « ~ € 20,893 »
        //    (groupes de milliers séparés par une virgule, format suédois)
        String eurText = selectText(doc, "p[dir=ltr]");
        if (eurText.contains("€")) {
            BigDecimal p = parseWholesaleAmount(eurText);
            if (p.compareTo(BigDecimal.ZERO) > 0) return new PriceInfo(p, "EUR");
        }

        // 2. JSON-LD schema.org : prix exact + devise native (SEK / EUR / ...)
        if (vehicleNode != null && vehicleNode.has("offers")) {
            JsonNode offers = vehicleNode.path("offers");
            String currency = offers.path("priceCurrency").asText("");
            if (!currency.isBlank()) {
                BigDecimal p = parsePrice(offers.path("price").asText(null));
                if (p.compareTo(BigDecimal.ZERO) > 0) return new PriceInfo(p, currency.toUpperCase());
            }
        }

        // 3. Bloc prix visible en devise suédoise : « 231 920 SEK »
        for (Element priceEl : doc.select("p")) {
            String text = priceEl.text().trim();
            if (text.matches("(?i)^[\\d.,\\s\\u00a0]+\\s*SEK$")) {
                BigDecimal p = parseWholesaleAmount(text);
                if (p.compareTo(BigDecimal.ZERO) > 0) return new PriceInfo(p, "SEK");
            }
        }

        // 4. Grille de spécifications si un prix total y figure
        for (Map.Entry<String, String> entry : specs.entrySet()) {
            String key = entry.getKey().toLowerCase();
            String val = entry.getValue();
            if ((key.contains("prix") || key.contains("price") || val.contains("€") || val.contains("SEK"))
                    && !val.isBlank()) {
                BigDecimal p = parseWholesaleAmount(val);
                if (p.compareTo(BigDecimal.ZERO) > 0) {
                    return new PriceInfo(p, val.contains("€") ? "EUR" : "SEK");
                }
            }
        }

        return new PriceInfo(BigDecimal.ZERO, "EUR");
    }

    private record PriceInfo(BigDecimal amount, String currency) {}

    private JsonNode extractVehicleJsonLd(Document doc) {
        Elements scripts = doc.select("script[type=application/ld+json]");
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
            } catch (Exception ignored) {}
        }
        return null;
    }

    private boolean isVehicleNode(JsonNode node) {
        String type = node.path("@type").asText("");
        return "Vehicle".equalsIgnoreCase(type) || "Car".equalsIgnoreCase(type) || "Product".equalsIgnoreCase(type);
    }

    private Map<String, String> parseSpecGrid(Document doc) {
        Map<String, String> specs = new LinkedHashMap<>();

        Elements tableRows = doc.select(
                "table.specs-table tr, table.vehicle-specs tr, table.table tr, .specifications table tr"
        );
        for (Element row : tableRows) {
            Elements th = row.select("th, td.label, td.key, .title");
            Elements td = row.select("td:not(.label):not(.key), td.value, .val");
            if (!th.isEmpty() && !td.isEmpty()) {
                specs.put(th.text().trim(), td.text().trim());
            }
        }
        if (!specs.isEmpty()) return specs;

        Elements dts = doc.select(".spec-grid dt, .vehicle-specs dt, dl.specs dt");
        for (Element dt : dts) {
            Element dd = dt.nextElementSibling();
            if (dd != null && "dd".equalsIgnoreCase(dd.tagName())) {
                specs.put(dt.text().trim(), dd.text().trim());
            }
        }
        if (!specs.isEmpty()) return specs;

        Elements items = doc.select(
                ".spec-grid .spec-item, .vehicle-specs .spec-item, .car-details .detail-item, " +
                        ".specs-list .spec-row, .info-grid .info-item, .technical-data .data-row"
        );
        for (Element item : items) {
            Element label = item.selectFirst(".label, .title, .key, .spec-label, span:first-child");
            Element value = item.selectFirst(".value, .data, .spec-value, span:last-child");
            if (label != null && value != null && label != value) {
                specs.put(label.text().trim(), value.text().trim());
            }
        }

        // Marques Tailwind du site : <p class="... text-fog">label</p>
        // immédiatement suivi de <p class="... text-snow ...">valeur</p>.
        // La classe "text-fog" est stable mais la taille de police du label
        // varie (text-xs sur certaines pages, text-[10px] arbitraire sur
        // d'autres) — on ne cible donc que "text-fog".
        for (Element label : doc.select("p.text-fog")) {
            Element value = label.nextElementSibling();
            if (value != null && value.hasText()) {
                specs.putIfAbsent(label.text().trim(), value.text().trim());
            }
        }

        return specs;
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

    private BigDecimal parsePrice(String rawPrice) {
        if (rawPrice == null || rawPrice.isBlank()) {
            return BigDecimal.ZERO;
        }
        String clean = rawPrice.replaceAll("[^0-9.,]", "").replace(",", ".");
        try {
            return new BigDecimal(clean);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Parse un montant entier basé sur la notation des milliers suédoise
     * (espaces insécables, virgule comme séparateur de milliers) : tous les
     * caractères non numériques sont supprimés (« 231 920 SEK » -> 231920,
     * « ~ € 20,893 » -> 20893).
     */
    private BigDecimal parseWholesaleAmount(String rawValue) {
        if (rawValue == null) {
            return BigDecimal.ZERO;
        }
        String clean = rawValue.replaceAll("[^0-9]", "");
        if (clean.isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(clean);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
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
            } catch (DateTimeParseException ignored) {}
            try {
                return LocalDate.parse(rawDate.trim(), FRENCH_ABBREV_DATE_FORMAT);
            } catch (DateTimeParseException ignored) {}
        }
        return fallbackYear > 0 ? LocalDate.of(fallbackYear, 1, 1) : LocalDate.now();
    }

    private FuelType parseFuelType(String rawValue) {
        if (rawValue == null) return FuelType.ESSENCE;
        String normalized = rawValue.trim().toUpperCase();
        if (normalized.contains("HYBR")) return FuelType.HYBRIDE;
        if (normalized.contains("ELEC") || normalized.contains("ÉLEC")) return FuelType.ELECTRIQUE;
        if (normalized.contains("DIES")) return FuelType.DIESEL;
      //  if (normalized.contains("GPL") || normalized.contains("LPG")) return FuelType.GPL;
        return FuelType.ESSENCE;
    }

    private void politeDelay() {
        try {
            Thread.sleep(DETAIL_REQUEST_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
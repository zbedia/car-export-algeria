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
 * Real connector for exportcar213.com.
 *
 * Compliance checked before writing this connector:
 * - robots.txt (verified 2026-08-21): "User-Agent: *  Allow: /  Disallow: /admin/  Disallow: /api/"
 *   — /inventaire and /vehicules/* are NOT disallowed. /api/ IS disallowed,
 *   so this connector only ever requests public HTML pages, never an
 *   internal API endpoint.
 *
 * Two-phase approach:
 * 1. Fetch /inventaire once, just to collect each vehicle's detail URL.
 * 2. Fetch each vehicle's detail page individually for its JSON-LD
 *    (schema.org "Vehicle") data plus the exact first-registration date
 *    from ".spec-grid" (the JSON-LD only has the year).
 *
 * IMPORTANT: detail pages carry MORE THAN ONE <script type="application/
 * ld+json"> tag (e.g. a site-wide Organization/WebSite schema in addition
 * to the page-specific Vehicle schema) — extractVehicleJsonLd() scans all
 * of them rather than assuming the first one is the Vehicle entry, which
 * was the bug in the first version of this connector (every single page
 * failed with "No Vehicle entry", including ones confirmed by hand to
 * contain a valid Vehicle block, because selectFirst() was grabbing an
 * unrelated script tag).
 *
 * A politeness delay is added between detail requests (see
 * DETAIL_REQUEST_DELAY_MS) so this connector doesn't hammer their server
 * with 200+ rapid-fire requests just because robots.txt technically
 * allows it.
 *
 * Still not available anywhere on the site: engine displacement (cm3) —
 * left null rather than guessed from horsepower. Garage city is likewise
 * not disclosed per vehicle.
 */
@Component
public class ExportCar213Connector implements VehicleSourceConnector {

    private static final Logger log = LoggerFactory.getLogger(ExportCar213Connector.class);

    private static final String BASE_URL = "https://exportcar213.com/inventaire";
    private static final String SOURCE_NAME = "ExportCar213";
    private static final String USER_AGENT =
            "Mozilla/5.0 (compatible; CarExportAlgeriaBot/1.0; +https://github.com/YOUR_USERNAME/car-export-algeria)";
    private static final int REQUEST_TIMEOUT_MS = 10_000;
    private static final long DETAIL_REQUEST_DELAY_MS = 300;
    private static final DateTimeFormatter FRENCH_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
                log.warn("[{}] Failed to parse detail page {}: {}", SOURCE_NAME, detailUrl, e.toString());
            }
            politeDelay();
        }
        log.info("[{}] Finished: {} vehicles parsed successfully, {} failed", SOURCE_NAME, results.size(), failures);
        return results;
    }

    private List<String> fetchDetailUrls() {
        try {
            Document listingDoc = Jsoup.connect(BASE_URL)
                    .userAgent(USER_AGENT)
                    .timeout(REQUEST_TIMEOUT_MS)
                    .get();

            Elements cards = listingDoc.select("a.vehicle-card");
            List<String> urls = new ArrayList<>();
            for (Element card : cards) {
                urls.add(card.attr("abs:href"));
            }
            return urls;
        } catch (IOException e) {
            throw new ScrapingException(getSourceName(), e);
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
        v.setExternalUrl(vehicleNode.path("offers").path("url").asText(detailUrl));
        v.setBrand(vehicleNode.path("brand").path("name").asText(""));
        v.setModel(vehicleNode.path("model").asText("").trim());

        int year = vehicleNode.path("vehicleModelDate").asInt(0);
        v.setYear(year);
        v.setFirstRegistrationDate(parseFrenchDate(specs.get("Mise en circulation"), year));

        v.setMileageKm(vehicleNode.path("mileageFromOdometer").path("value").asInt(0));
        v.setFuelType(parseFuelType(vehicleNode.path("fuelType").asText("")));

        JsonNode priceNode = vehicleNode.path("offers").path("price");
        v.setPrice(new BigDecimal(priceNode.asText("0")));
        v.setCurrency(vehicleNode.path("offers").path("priceCurrency").asText("EUR"));

        v.setEngineDisplacementCm3(null);
        v.setGarageCity(null);

        v.setImageUrl(ListingImageExtractor.extract(vehicleNode, detailDoc));

        v.setScrapedAt(LocalDateTime.now());
        return v;
    }

    /**
     * Scans EVERY JSON-LD script on the page (there can be more than one —
     * e.g. a site-wide Organization schema alongside the page-specific
     * Vehicle schema) and returns the first node whose "@type" is
     * "Vehicle", whether that node is the script's root object directly
     * or nested inside an "@graph" array.
     */
    private JsonNode extractVehicleJsonLd(Document doc) throws IOException {
        Elements scripts = doc.select("script[type=application/ld+json]");
        log.debug("[{}] {} JSON-LD script(s) found on {}", SOURCE_NAME, scripts.size(), doc.location());

        for (Element script : scripts) {
            JsonNode root;
            try {
                root = objectMapper.readTree(script.data());
            } catch (Exception e) {
                continue; // malformed block — skip it, try the next script tag
            }

            if ("Vehicle".equals(root.path("@type").asText())) {
                return root;
            }
            for (JsonNode node : root.path("@graph")) {
                if ("Vehicle".equals(node.path("@type").asText())) {
                    return node;
                }
            }
        }
        throw new IOException("No Vehicle entry in any of the " + scripts.size()
                + " JSON-LD script(s) on " + doc.location());
    }

    private Map<String, String> parseSpecGrid(Document doc) {
        Map<String, String> specs = new LinkedHashMap<>();
        Element grid = doc.selectFirst(".spec-grid");
        if (grid == null) {
            return specs;
        }

        Elements children = grid.children();
        for (int i = 0; i + 1 < children.size(); i += 2) {
            specs.put(children.get(i).text().trim(), children.get(i + 1).text().trim());
        }
        return specs;
    }

    private LocalDate parseFrenchDate(String rawDate, int fallbackYear) {
        if (rawDate != null && !rawDate.isBlank()) {
            try {
                return LocalDate.parse(rawDate.trim(), FRENCH_DATE_FORMAT);
            } catch (DateTimeParseException e) {
                // Fall through to the year-only approximation below.
            }
        }
        return fallbackYear > 0 ? LocalDate.of(fallbackYear, 1, 1) : LocalDate.now();
    }

    private FuelType parseFuelType(String rawValue) {
        String normalized = rawValue.trim().toUpperCase();
        return switch (normalized) {
            case "ÉLECTRIQUE", "ELECTRIQUE", "ELECTRIC" -> FuelType.ELECTRIQUE;
            case "HYBRIDE", "HYBRID" -> FuelType.HYBRIDE;
            case "DIESEL" -> FuelType.DIESEL;
            default -> FuelType.ESSENCE;
        };
    }

    private void politeDelay() {
        try {
            Thread.sleep(DETAIL_REQUEST_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
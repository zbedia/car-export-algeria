package com.carexport.scraping.configdriven;

import com.carexport.exception.ScrapingException;
import com.carexport.model.FuelType;
import com.carexport.model.VehicleListing;
import com.carexport.scraping.ListingImageExtractor;
import com.carexport.scraping.ScrapeRunner;
import com.carexport.scraping.SearchCriteria;
import com.carexport.scraping.VehicleSourceConnector;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The generic, configuration-driven connector of the {@link SourceConfig} family.
 *
 * It covers the most common marketplace shape: a catalogue enumerated through
 * a sitemap, followed by detail pages carrying a label/value spec grid, a flat
 * price element, and an optional mileage in the headline. Everything the parser
 * needs — selectors, accent-free label keys, fuel keywords, date format, site
 * behavior — comes from a {@code scraping-sources.yml} entry, so adding one of
 * those sources is exactly one YAML block. Sources outside this shape stay as
 * dedicated connectors.
 *
 * The single per-source knob that may still require code is the HTML itself
 * (JS-rendered catalogs, non-grid layouts); this class is the default that
 * covers the rest.
 */
public class ConfigDrivenConnector implements VehicleSourceConnector {

    private static final Logger log = LoggerFactory.getLogger(ConfigDrivenConnector.class);

    private static final String SITEMAP_LOC_SELECTOR = "loc";

    private final String sourceName;
    private final SourceConfig config;
    private final Pattern titleMileagePattern;

    public ConfigDrivenConnector(String sourceName, SourceConfig config) {
        this.sourceName = sourceName;
        this.config = config;
        this.titleMileagePattern = Pattern.compile(configOrDefaultTitlePattern());
    }

    @Override
    public String getSourceName() {
        return sourceName;
    }

    @Override
    public List<VehicleListing> fetchListings(SearchCriteria criteria) {
        List<String> detailUrls = fetchDetailUrls();
        log.info("[{}] Found {} vehicle URLs in the sitemap", sourceName, detailUrls.size());

        ScrapeRunner.ScrapeOutcome outcome = ScrapeRunner.run(
                sourceName, detailUrls, config.detailConcurrency(), config.politenessMs(), this::fetchAndParse);
        log.info("[{}] Finished: {} vehicles parsed successfully, {} skipped, {} failed",
                sourceName, outcome.results().size(), outcome.skipped(), outcome.failures());
        return outcome.results();
    }

    private VehicleListing fetchAndParse(String detailUrl) throws IOException {
        try {
            return parse(fetchDetail(detailUrl), detailUrl);
        } catch (NotAVehicleException e) {
            log.debug("[{}] Skipped non-vehicle listing {}: {}", sourceName, detailUrl, e.getMessage());
            return null;
        }
    }

    /**
     * Pure page parsing — {@link VehicleListing} from a fetched detail page.
     * Package-visible so tests can drive every parse path without network I/O.
     */
    VehicleListing parse(Document doc, String detailUrl) {
        Map<String, String> specs = parseSpecGrid(doc);

        VehicleListing v = new VehicleListing();
        v.setSource(sourceName);
        v.setExternalUrl(detailUrl);

        v.setBrand(firstNonBlank(specValue(specs, "brand"), ""));
        v.setModel(firstNonBlank(specValue(specs, "model"), ""));

        v.setYear(parseIntegerSafely(specValue(specs, "year")));

        int mileage = parseIntegerSafely(specValue(specs, "mileage"));
        if (mileage == 0) {
            mileage = parseIntegerSafely(extractTitleMileage(doc));
        }
        v.setMileageKm(mileage);

        v.setFuelType(parseFuelType(specValue(specs, "fuel")));

        v.setPrice(parseAmount(extractPriceText(doc)));
        v.setCurrency(config.currency());

        if (config.skipWhenNotVehicle() && v.getYear() == 0
                && (v.getPrice() == null || v.getPrice().signum() == 0)) {
            throw new NotAVehicleException(detailUrl);
        }

        String displacementSpec = firstNonBlank(
                specValue(specs, "displacement"),
                firstMatchingFallback(specs));
        int displacementCm3 = parseDisplacementCm3(displacementSpec);
        v.setEngineDisplacementCm3(displacementCm3 > 0 ? displacementCm3 : null);

        v.setFirstRegistrationDate(parseDate(specValue(specs, "registration"), v.getYear()));

        v.setGarageCity(config.garageCity());

        v.setImageUrl(ListingImageExtractor.extract(null, doc));

        v.setScrapedAt(LocalDateTime.now());

        return v;
    }

    private Map<String, String> parseSpecGrid(Document doc) {
        Map<String, String> specs = new LinkedHashMap<>();
        Elements names = doc.select(config.specGrid().nameSelector());
        Elements values = doc.select(config.specGrid().valueSelector());
        int pairs = Math.min(names.size(), values.size());
        for (int i = 0; i < pairs; i++) {
            String label = normalizeLabel(names.get(i).text());
            String value = values.get(i).text().trim();
            if (!label.isEmpty() && !value.isEmpty()) {
                specs.putIfAbsent(label, value);
            }
        }
        return specs;
    }

    private String normalizeLabel(String rawLabel) {
        if (rawLabel == null) {
            return "";
        }
        String label = rawLabel.replaceAll("[\\s:：]+$", "")
                .replaceAll("[^\\x00-\\x7F]", "")
                .trim()
                .toLowerCase(Locale.ROOT);
        return label.replaceAll("\\s+", " ");
    }

    private String specValue(Map<String, String> specs, String field) {
        return specs.get(config.specGrid().labelFor(field));
    }

    private String firstMatchingFallback(Map<String, String> specs) {
        List<String> fallbackKeys = config.specGrid().displacementFallbackKeys();
        for (String key : fallbackKeys) {
            String value = specs.get(key);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private String extractTitleMileage(Document doc) {
        Element title = doc.selectFirst(config.titleSelector());
        if (title == null) {
            return "";
        }
        Matcher matcher = titleMileagePattern.matcher(title.text());
        return matcher.find() ? matcher.group(1) : "";
    }

    private String extractPriceText(Document doc) {
        Element priceEl = doc.selectFirst(config.priceSelector());
        return priceEl == null ? "" : priceEl.text();
    }

    private FuelType parseFuelType(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return FuelType.ESSENCE;
        }
        String normalized = rawValue.replaceAll("[^\\p{ASCII}]", "").toUpperCase(Locale.ROOT);
        if (matchesAny(normalized, "electric")) return FuelType.ELECTRIQUE;
        if (matchesAny(normalized, "hybrid")) return FuelType.HYBRIDE;
        if (matchesAny(normalized, "diesel")) return FuelType.DIESEL;
        return FuelType.ESSENCE;
    }

    private boolean matchesAny(String normalized, String fuelKey) {
        List<String> keywords = config.fuelMapping().get(fuelKey);
        if (keywords != null) {
            for (String keyword : keywords) {
                if (normalized.contains(keyword.toUpperCase(Locale.ROOT))) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<String> fetchDetailUrls() {
        IOException lastError = null;
        for (int attempt = 1; attempt <= config.fetchAttempts(); attempt++) {
            try {
                Document sitemapDoc = Jsoup.connect(config.sitemapUrl())
                        .userAgent(config.userAgent())
                        .timeout((int) config.requestTimeoutMs())
                        .parser(Parser.xmlParser())
                        .get();

                Set<String> urls = new LinkedHashSet<>();
                Elements locs = sitemapDoc.select(SITEMAP_LOC_SELECTOR);
                for (Element loc : locs) {
                    String url = loc.text().trim();
                    if (url.contains(config.offerUrlMarker())) {
                        urls.add(url);
                    }
                }
                return new ArrayList<>(urls);
            } catch (IOException e) {
                lastError = e;
                log.warn("[{}] Sitemap fetch failed (attempt {}/{}): {}", sourceName, attempt,
                        config.fetchAttempts(), e.getMessage());
                if (attempt < config.fetchAttempts()) {
                    sleepQuietly(config.retryDelayMs());
                }
            }
        }
        throw new ScrapingException(sourceName, lastError);
    }

    private Document fetchDetail(String detailUrl) throws IOException {
        return Jsoup.connect(detailUrl)
                .userAgent(config.userAgent())
                .timeout((int) config.requestTimeoutMs())
                .get();
    }

    private void sleepQuietly(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // --- parsing helpers (label-key agnostic) ---

    private String firstNonBlank(String primary, String fallback) {
        return primary != null && !primary.isBlank() ? primary.trim() : fallback;
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

    private BigDecimal parseAmount(String rawValue) {
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

    private static final Pattern DISPLACEMENT_LITRES =
            Pattern.compile("(?<num>[0-9]+(?:[.,][0-9]+)?)\\s*[Ll]");
    private static final Pattern DISPLACEMENT_CC =
            Pattern.compile("(?<num>[0-9]+)\\s*(?:cm3|cc|cm³)", Pattern.CASE_INSENSITIVE);
    private static final Pattern BARE_COMMA_DECIMAL = Pattern.compile("(?<int>[0-9]+),(?<frac>[0-9]+)");
    private static final int CM3_PER_LITRE = 1000;
    private static final int MAX_BARE_DECIMAL_LITRES = 20;

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

    private LocalDate parseDate(String rawDate, int fallbackYear) {
        if (rawDate != null && !rawDate.isBlank()) {
            try {
                return LocalDate.parse(rawDate.trim(), DateTimeFormatter.ofPattern(config.dateFormat()));
            } catch (DateTimeParseException ignored) {
                // fall through to the fallback year
            }
        }
        return fallbackYear > 0 ? LocalDate.of(fallbackYear, 1, 1) : LocalDate.now();
    }

    private String configOrDefaultTitlePattern() {
        String pattern = config.titleMileagePattern();
        if (pattern == null || pattern.isBlank()) {
            return "(?i)(\\d{3,})\\s*kms?";
        }
        return pattern;
    }

    /** Thrown by {@link #parse} when a page is not a vehicle listing at all. */
    static class NotAVehicleException extends RuntimeException {
        NotAVehicleException(String detailUrl) {
            super("no vehicle data (year 0, price 0): " + detailUrl);
        }
    }
}
package com.carexport.scraping.configdriven;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toCollection;

/**
 * Loads the config-driven chunk of the scraping catalog from
 * {@code scraping-sources.yml}. The file is hand-written, so binding stays
 * explicit and typed (no reflection): unknown or mistyped fields fail fast
 * with a clear message instead of silently parsing as {@code null}.
 */
public class ScrapingSourcesLoader {

    private static final Logger log = LoggerFactory.getLogger(ScrapingSourcesLoader.class);

    private static final String RESOURCE = "scraping-sources.yml";

    public List<SourceConfig> load() {
        try (InputStream in = new ClassPathResource(RESOURCE).getInputStream()) {
            Object root = new Yaml().load(in);
            if (!(root instanceof Map<?, ?>)) {
                throw new IllegalStateException("scraping-sources.yml must be a mapping with a 'sources' list");
            }
            Object rawSources = ((Map<?, ?>) root).get("sources");
            if (!(rawSources instanceof List<?>)) {
                throw new IllegalStateException("scraping-sources.yml is missing the 'sources' list");
            }
            List<SourceConfig> sources = new ArrayList<>();
            for (Object rawSource : (List<?>) rawSources) {
                sources.add(bindSource(asMap(rawSource, "source entry")));
            }
            sources.forEach(s -> log.info("[{}] config-driven source registered (sitemap: {})",
                    s.name(), s.sitemapUrl()));
            return sources;
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + RESOURCE, e);
        }
    }

    private SourceConfig bindSource(Map<String, Object> m) {
        String name = requiredString(m, "name");
        Map<String, Object> grid = asMap(m.get("specGrid"), name + ".specGrid");
        Map<String, String> labels = bindLabels(asMap(grid.get("labels"), name + ".specGrid.labels"));
        List<String> displacementFallbacks = bindStringList(grid.get("displacementFallbackKeys"));
        return new SourceConfig(
                name,
                requiredString(m, "sitemapUrl"),
                string(m, "offerUrlMarker", "/annonce/"),
                string(m, "currency", "EUR"),
                string(m, "garageCity", ""),
                string(m, "priceSelector", ".vehica-car-price"),
                string(m, "titleSelector", "h1"),
                string(m, "titleMileagePattern", "(?i)(\\d{3,})\\s*kms?"),
                string(m, "dateFormat", "dd/MM/yyyy"),
                longOf(m.get("politenessMs"), 300L),
                longOf(m.get("requestTimeoutMs"), 30_000L),
                intOf(m.get("fetchAttempts"), 3),
                longOf(m.get("retryDelayMs"), 2_000L),
                string(m, "userAgent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                                + "(KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"),
                new SourceConfig.SpecGrid(
                        requiredString(grid, "nameSelector"),
                        requiredString(grid, "valueSelector"),
                        labels,
                        displacementFallbacks),
                booleanValue(m.get("skipWhenNotVehicle"), false),
                bindFuelMapping(m));
    }

    private Map<String, String> bindLabels(Map<String, Object> labels) {
        Map<String, String> bound = new LinkedHashMap<>();
        labels.forEach((key, value) -> bound.put(key, String.valueOf(value)));
        return bound;
    }

    private Map<String, List<String>> bindFuelMapping(Map<String, Object> m) {
        Object raw = m.get("fuelMapping");
        if (!(raw instanceof Map<?, ?>)) {
            return Map.of();
        }
        Map<String, List<String>> mapping = new LinkedHashMap<>();
        ((Map<?, ?>) raw).forEach((fuel, keywords) -> {
            if (keywords instanceof List<?>) {
                mapping.put(String.valueOf(fuel), bindStringList(keywords));
            }
        });
        return mapping;
    }

    private List<String> bindStringList(Object raw) {
        if (!(raw instanceof List<?>)) {
            return new ArrayList<>();
        }
        return ((List<?>) raw).stream()
                .map(String::valueOf)
                .collect(toCollection(ArrayList::new));
    }

    private Map<String, Object> asMap(Object value, String path) {
        if (!(value instanceof Map<?, ?>)) {
            throw new IllegalStateException("scraping-sources.yml: expected a mapping for " + path);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        ((Map<?, ?>) value).forEach((k, v) -> out.put(String.valueOf(k), v));
        return out;
    }

    private String requiredString(Map<String, Object> m, String key) {
        String value = string(m, key, null);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("scraping-sources.yml: entry '" + m.get("name")
                    + "' is missing a non-blank '" + key + "'");
        }
        return value;
    }

    private String string(Map<String, Object> m, String key, String fallback) {
        Object value = m.get(key);
        return value == null ? fallback : String.valueOf(value);
    }

    private long longOf(Object value, long fallback) {
        return value instanceof Number ? ((Number) value).longValue() : fallback;
    }

    private boolean booleanValue(Object value, boolean fallback) {
        return value instanceof Boolean ? (Boolean) value : fallback;
    }

    private int intOf(Object value, int fallback) {
        return value instanceof Number ? ((Number) value).intValue() : fallback;
    }
}
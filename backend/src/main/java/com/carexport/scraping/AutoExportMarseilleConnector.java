package com.carexport.scraping;

import com.carexport.exception.ScrapingException;
import com.carexport.model.VehicleListing;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Connector for autoexportmarseille.com, a WordPress marketplace exporting
 * cars from Marseille to Algeria.
 *
 * Responsibilities kept to network I/O: fetching the vehicle sitemap (with
 * retries), fetching each detail page, and waiting out the politeness delay.
 * All page parsing is delegated to {@link AutoExportMarseilleVehicleParser}.
 *
 * robots.txt only forbids {@code /wp-admin/}, and every vehicle URL is listed
 * in the Rank Math sitemap {@code /vehica_car-sitemap.xml}, so the sitemap is
 * used to enumerate the full catalogue instead of crawling an index page.
 */
@Component
public class AutoExportMarseilleConnector implements VehicleSourceConnector {

    private static final Logger log = LoggerFactory.getLogger(AutoExportMarseilleConnector.class);

    private static final String SOURCE_NAME = "AutoExportMarseille";
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36";
    private static final int REQUEST_TIMEOUT_MS = 30_000;
    private static final int SITEMAP_FETCH_ATTEMPTS = 3;
    private static final long SITEMAP_RETRY_DELAY_MS = 2_000;
    private static final long DETAIL_REQUEST_DELAY_MS = 300;

    private final AutoExportMarseilleVehicleParser parser = new AutoExportMarseilleVehicleParser(SOURCE_NAME);

    @Override
    public String getSourceName() {
        return SOURCE_NAME;
    }

    @Override
    public List<VehicleListing> fetchListings(SearchCriteria criteria) {
        List<String> detailUrls = fetchDetailUrls();
        log.info("[{}] Found {} vehicle URLs in the sitemap", SOURCE_NAME, detailUrls.size());

        List<VehicleListing> results = new ArrayList<>();
        int failures = 0;
        for (String detailUrl : detailUrls) {
            try {
                results.add(parser.parse(fetchDetail(detailUrl), detailUrl));
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
        for (int attempt = 1; attempt <= SITEMAP_FETCH_ATTEMPTS; attempt++) {
            try {
                Document sitemapDoc = Jsoup.connect(AutoExportMarseilleSelectors.SITEMAP_URL)
                        .userAgent(USER_AGENT)
                        .timeout(REQUEST_TIMEOUT_MS)
                        .parser(Parser.xmlParser())
                        .get();

                Set<String> urls = new LinkedHashSet<>();
                Elements locs = sitemapDoc.select(AutoExportMarseilleSelectors.SITEMAP_LOC_SELECTOR);
                for (Element loc : locs) {
                    String url = loc.text().trim();
                    if (url.contains(AutoExportMarseilleSelectors.OFFER_URL_MARKER)) {
                        urls.add(url);
                    }
                }
                return new ArrayList<>(urls);
            } catch (IOException e) {
                lastError = e;
                log.warn("[{}] Sitemap fetch failed (attempt {}/{}): {}", SOURCE_NAME, attempt,
                        SITEMAP_FETCH_ATTEMPTS, e.getMessage());
                if (attempt < SITEMAP_FETCH_ATTEMPTS) {
                    sleepQuietly(SITEMAP_RETRY_DELAY_MS);
                }
            }
        }
        throw new ScrapingException(getSourceName(), lastError);
    }

    private Document fetchDetail(String detailUrl) throws IOException {
        return Jsoup.connect(detailUrl)
                .userAgent(USER_AGENT)
                .timeout(REQUEST_TIMEOUT_MS)
                .get();
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void politeDelay() {
        try {
            Thread.sleep(DETAIL_REQUEST_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
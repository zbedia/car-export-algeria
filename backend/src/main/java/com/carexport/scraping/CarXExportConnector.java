package com.carexport.scraping;

import com.carexport.exception.ScrapingException;
import com.carexport.model.VehicleListing;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Connector for carxexport.com configured to extract EUR price tags directly.
 *
 * Responsibilities kept to network I/O: fetching the listing page (with
 * retries), fetching each detail page, and waiting out the politeness delay.
 * All page parsing is delegated to {@link CarXExportVehicleParser}.
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

    private final CarXExportVehicleParser parser = new CarXExportVehicleParser(SOURCE_NAME);

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
        for (int attempt = 1; attempt <= LISTING_FETCH_ATTEMPTS; attempt++) {
            try {
                Document listingDoc = Jsoup.connect(BASE_URL)
                        .userAgent(USER_AGENT)
                        .timeout(REQUEST_TIMEOUT_MS)
                        .get();

                Elements cardLinks = listingDoc.select(CarXExportSelectors.LISTING_CARD_LINKS);

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
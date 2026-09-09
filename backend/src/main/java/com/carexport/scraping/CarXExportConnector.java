package com.carexport.scraping;

import com.carexport.exception.ScrapingException;
import com.carexport.model.VehicleListing;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Connector for carxexport.com configured to extract EUR price tags directly.
 *
 * Responsibilities kept to network I/O: fetching the listing page (with
 * retries), fetching each detail page, and waiting out the politeness delay.
 * All page parsing is delegated to {@link CarXExportVehicleParser}.
 *
 * The listing page is a server-rendered Next.js app: the full catalogue is
 * serialized into the React Flight payload of the {@code self.__next_f.push}
 * scripts while only the first 24 offers are actually rendered as cards.
 * Enumerating the payload therefore recovers every offer, not a 24-car slice.
 */
@Component
@ConditionalOnProperty(name = "scraping.carxport.enabled", havingValue = "true", matchIfMissing = true)
public class CarXExportConnector implements VehicleSourceConnector {

    private static final Logger log = LoggerFactory.getLogger(CarXExportConnector.class);

    private static final String BASE_URL = "https://carxexport.com/fr/offers";

    /**
     * Offer id as serialized by the React Flight payload: {@code "id":"<uuid>"}.
     * Inside the stream the quotes are JS-escaped ({@code \\"id\\":\\"<uuid>\\"}),
     * so the payload is unescaped to raw quotes before matching.
     */
    private static final Pattern FLIGHT_OFFER_ID = Pattern.compile(
            "\"id\":\"([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})\"");
    private static final String OFFER_URL_TEMPLATE = "https://carxexport.com/fr/offers/%s";
    private static final String SOURCE_NAME = "CarXExport";
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36";
    private static final int REQUEST_TIMEOUT_MS = 30_000;
    private static final int LISTING_FETCH_ATTEMPTS = 3;
    private static final long LISTING_RETRY_DELAY_MS = 2_000;
    private static final long DETAIL_REQUEST_DELAY_MS = 300;

    private final CarXExportVehicleParser parser = new CarXExportVehicleParser(SOURCE_NAME);

    @Value("${scraping.carxport.detail-concurrency:4}")
    private int detailConcurrency;

    @Override
    public String getSourceName() {
        return SOURCE_NAME;
    }

    @Override
    public List<VehicleListing> fetchListings(SearchCriteria criteria) {
        List<String> detailUrls = fetchDetailUrls();
        log.info("[{}] Found {} vehicle cards on the listing page", SOURCE_NAME, detailUrls.size());

        ScrapeRunner.ScrapeOutcome outcome = ScrapeRunner.run(
                SOURCE_NAME, detailUrls, detailConcurrency, DETAIL_REQUEST_DELAY_MS,
                url -> parser.parse(fetchDetail(url), url));
        log.info("[{}] Finished: {} vehicles parsed successfully, {} failed",
                SOURCE_NAME, outcome.results().size(), outcome.failures());
        return outcome.results();
    }

    private List<String> fetchDetailUrls() {
        IOException lastError = null;
        for (int attempt = 1; attempt <= LISTING_FETCH_ATTEMPTS; attempt++) {
            try {
                Document listingDoc = Jsoup.connect(BASE_URL)
                        .userAgent(USER_AGENT)
                        .timeout(REQUEST_TIMEOUT_MS)
                        .get();

                return extractDetailUrls(listingDoc);
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

    private List<String> extractDetailUrls(Document listingDoc) {
        List<String> urls = extractOfferUrlsFromFlightPayload(listingDoc);
        if (!urls.isEmpty()) {
            return urls;
        }
        log.warn("[{}] React Flight payload not found; falling back to the rendered offer cards", SOURCE_NAME);
        return extractOfferUrlsFromRenderedCards(listingDoc);
    }

    /**
     * Reads the server-rendered React Flight stream ({@code self.__next_f.push})
     * and rebuilds one detail URL per offer id. This is the only source that
     * carries the whole catalogue: the card fallback below never sees more than
     * the first page of offers.
     */
    private List<String> extractOfferUrlsFromFlightPayload(Document listingDoc) {
        StringBuilder flightStream = new StringBuilder();
        for (Element script : listingDoc.select("script")) {
            String data = script.data();
            if (data.contains("self.__next_f.push")) {
                flightStream.append(data);
            }
        }
        if (flightStream.isEmpty()) {
            return List.of();
        }

        // Inline JSON inside the stream quotes its keys/values as \"…\"; unescape
        // just enough for the regex to see "id":"<uuid>".
        String payload = flightStream.toString().replace("\\\"", "\"");

        Set<String> offerIds = new LinkedHashSet<>();
        Matcher matcher = FLIGHT_OFFER_ID.matcher(payload);
        while (matcher.find()) {
            offerIds.add(matcher.group(1));
        }

        List<String> urls = new ArrayList<>(offerIds.size());
        for (String id : offerIds) {
            urls.add(OFFER_URL_TEMPLATE.formatted(id));
        }
        return urls;
    }

    /**
     * Pre-Next.js fallback: the anchors of the cards actually rendered on the
     * listing page. Only recovers the handful of offers visible server-side.
     */
    private List<String> extractOfferUrlsFromRenderedCards(Document listingDoc) {
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
}
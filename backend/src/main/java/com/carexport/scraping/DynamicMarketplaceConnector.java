package com.carexport.scraping;

import com.carexport.exception.ScrapingException;
import com.carexport.model.FuelType;
import com.carexport.model.VehicleListing;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Example connector for a fictional "DynamicMarketplace" site whose
 * listings are rendered client-side (Single Page App style). Jsoup
 * cannot see this content, since it only fetches the initial HTML
 * before JavaScript runs — Playwright drives an actual headless
 * browser instead, so it sees the page as a real user would.
 *
 * Use this technique (rather than Jsoup) when the target site's
 * listing cards only appear after JS execution, infinite scroll,
 * or an interaction (e.g. clicking "load more").
 *
 * Should be adapted with the real CSS selectors of the actual target site.
 * Always check the site's terms of service / robots.txt before scraping
 * in production.
 *
 * Disabled unless {@code scraping.example-connectors.enabled=true} is set:
 * it targets a fictional domain and is only kept as a reference.
 */
@Component
@ConditionalOnProperty(name = "scraping.example-connectors.enabled", havingValue = "true")
public class DynamicMarketplaceConnector implements VehicleSourceConnector {

    private static final String BASE_URL = "https://dynamic-marketplace.example.com/search";
    private static final String SOURCE_NAME = "DynamicMarketplace";
    private static final int SELECTOR_TIMEOUT_MS = 10_000;

    private final PlaywrightBrowserManager browserManager;

    public DynamicMarketplaceConnector(PlaywrightBrowserManager browserManager) {
        this.browserManager = browserManager;
    }

    @Override
    public String getSourceName() {
        return SOURCE_NAME;
    }

    @Override
    public List<VehicleListing> fetchListings(SearchCriteria criteria) {
        List<VehicleListing> results = new ArrayList<>();
        Browser browser = browserManager.getBrowser();

        try (BrowserContext context = browser.newContext()) {
            Page page = context.newPage();
            String url = BASE_URL + "?brand=" + (criteria.getBrand() != null ? criteria.getBrand() : "");
            page.navigate(url);

            // Wait for the JS-rendered listing cards to actually appear
            // before reading them — this is the key difference from Jsoup,
            // which has no concept of "waiting for JavaScript to run".
            page.waitForSelector(".vehicle-card",
                new Page.WaitForSelectorOptions().setTimeout(SELECTOR_TIMEOUT_MS));

            for (ElementHandle card : page.querySelectorAll(".vehicle-card")) {
                results.add(parseCard(card));
            }
        } catch (Exception e) {
            throw new ScrapingException(getSourceName(), e);
        }

        return results;
    }

    private VehicleListing parseCard(ElementHandle card) {
        VehicleListing v = new VehicleListing();
        v.setSource(getSourceName());
        v.setExternalUrl(attr(card, "a.link", "href"));
        v.setBrand(text(card, ".brand"));
        v.setModel(text(card, ".model"));
        v.setYear(Integer.parseInt(text(card, ".year")));
        v.setPrice(new BigDecimal(text(card, ".price").replaceAll("[^0-9]", "")));
        v.setCurrency("EUR");
        v.setScrapedAt(LocalDateTime.now());

        // TODO: adapt these selectors to the real site's markup.
        v.setFuelType(parseFuelType(text(card, ".fuel-type")));
        String displacementText = text(card, ".engine-displacement").replaceAll("[^0-9]", "");
        v.setEngineDisplacementCm3(displacementText.isEmpty() ? null : Integer.parseInt(displacementText));
        v.setFirstRegistrationDate(LocalDate.parse(attr(card, ".first-registration-date", "datetime")));

        return v;
    }

    private String text(ElementHandle card, String selector) {
        ElementHandle element = card.querySelector(selector);
        return element != null ? element.textContent().trim() : "";
    }

    private String attr(ElementHandle card, String selector, String attribute) {
        ElementHandle element = card.querySelector(selector);
        return element != null ? element.getAttribute(attribute) : "";
    }

    private FuelType parseFuelType(String rawValue) {
        String normalized = rawValue.trim().toUpperCase();
        return switch (normalized) {
            case "ELECTRIQUE", "ELECTRIC", "EV" -> FuelType.ELECTRIQUE;
            case "HYBRIDE", "HYBRID" -> FuelType.HYBRIDE;
            case "DIESEL" -> FuelType.DIESEL;
            default -> FuelType.ESSENCE;
        };
    }
}

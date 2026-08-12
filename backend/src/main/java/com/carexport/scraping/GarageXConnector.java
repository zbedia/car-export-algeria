package com.carexport.scraping;

import com.carexport.exception.ScrapingException;
import com.carexport.model.VehicleListing;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Example connector for a fictional "GarageX" dealership.
 * Should be adapted with the real CSS selectors of the actual target site.
 * Always check the site's terms of service / robots.txt before scraping in production.
 */
@Component
public class GarageXConnector implements VehicleSourceConnector {

    private static final String BASE_URL = "https://garagex.example.com/search";

    @Override
    public String getSourceName() {
        return "GarageX";
    }

    @Override
    public List<VehicleListing> fetchListings(SearchCriteria criteria) {
        List<VehicleListing> results = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(BASE_URL)
                .userAgent("Mozilla/5.0 (compatible; VehicleExportBot/1.0)")
                .timeout(10_000)
                .data("brand", criteria.getBrand() != null ? criteria.getBrand() : "")
                .data("min_year", String.valueOf(criteria.getMinYear()))
                .get();

            Elements cards = doc.select(".vehicle-card");
            for (Element card : cards) {
                results.add(parseCard(card));
            }
        } catch (IOException e) {
            throw new ScrapingException(getSourceName(), e);
        }
        return results;
    }

    private VehicleListing parseCard(Element card) {
        VehicleListing v = new VehicleListing();
        v.setSource(getSourceName());
        v.setExternalUrl(card.select("a.link").attr("abs:href"));
        v.setBrand(card.select(".brand").text());
        v.setModel(card.select(".model").text());
        v.setYear(Integer.parseInt(card.select(".year").text()));
        v.setPrice(new BigDecimal(card.select(".price").text().replaceAll("[^0-9]", "")));
        v.setCurrency("EUR");
        v.setScrapedAt(LocalDateTime.now());
        return v;
    }
}

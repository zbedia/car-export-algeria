package com.carexport.scraping;

import com.carexport.model.VehicleListing;
import com.carexport.service.ListingUpdateService;
import com.carexport.service.ScrapingHealthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScrapingOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(ScrapingOrchestrator.class);

    private final List<VehicleSourceConnector> connectors;
    private final ScrapingHealthService healthService;
    private final ListingUpdateService listingUpdateService;

    public ScrapingOrchestrator(List<VehicleSourceConnector> connectors,
                                ScrapingHealthService healthService,
                                ListingUpdateService listingUpdateService) {
        this.connectors = connectors;
        this.healthService = healthService;
        this.listingUpdateService = listingUpdateService;
    }

    /**
     * Fetches from every connector in parallel and persists each source as soon
     * as it finishes. A source that fails to scrape — or whose rows are rejected
     * by the database — is isolated: it is recorded in health and the other
     * sources keep going, so fresh listings never wait for the slowest connector.
     */
    public void refreshAll(SearchCriteria criteria) {
        connectors.parallelStream().forEach(c -> refreshSource(c, criteria));
    }

    private void refreshSource(VehicleSourceConnector c, SearchCriteria criteria) {
        try {
            List<VehicleListing> result = c.fetchListings(criteria);
            listingUpdateService.persist(result);
            healthService.recordSuccess(c.getSourceName(), result.size());
        } catch (Exception e) {
            healthService.recordFailure(c.getSourceName(), e.getMessage());
            log.warn("Source failed {}: {}", c.getSourceName(), e.getMessage());
        }
    }
}
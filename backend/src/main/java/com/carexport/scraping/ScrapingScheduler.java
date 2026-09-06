package com.carexport.scraping;

import com.carexport.model.VehicleListing;
import com.carexport.service.ListingUpdateService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScrapingScheduler {

    private final ScrapingOrchestrator orchestrator;
    private final ListingUpdateService listingUpdateService;

    public ScrapingScheduler(ScrapingOrchestrator orchestrator, ListingUpdateService listingUpdateService) {
        this.orchestrator = orchestrator;
        this.listingUpdateService = listingUpdateService;
    }

    @Scheduled(fixedRateString = "${scraping.refresh-interval-ms}")
    public void refreshListings() {
        SearchCriteria criteria = SearchCriteria.defaultExportCriteria();
        List<VehicleListing> listings = orchestrator.collectAll(criteria);

        listingUpdateService.persist(listings);
    }
}
package com.carexport.scraping;

import com.carexport.model.VehicleListing;
import com.carexport.service.ListingUpdateService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class ScrapingScheduler {

    private final ScrapingOrchestrator orchestrator;
    private final ListingUpdateService listingUpdateService;

    public ScrapingScheduler(ScrapingOrchestrator orchestrator, ListingUpdateService listingUpdateService) {
        this.orchestrator = orchestrator;
        this.listingUpdateService = listingUpdateService;
    }

    @Scheduled(fixedRate = 6, timeUnit = TimeUnit.HOURS)
    public void refreshListings() {
        SearchCriteria criteria = SearchCriteria.defaultExportCriteria();
        List<VehicleListing> listings = orchestrator.collectAll(criteria);

        listingUpdateService.persist(listings);
    }
}
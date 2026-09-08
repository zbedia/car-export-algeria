package com.carexport.scraping;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScrapingScheduler {

    private final ScrapingOrchestrator orchestrator;

    public ScrapingScheduler(ScrapingOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Scheduled(fixedRateString = "${scraping.refresh-interval-ms}")
    public void refreshListings() {
        SearchCriteria criteria = SearchCriteria.defaultExportCriteria();
        orchestrator.refreshAll(criteria);
    }
}
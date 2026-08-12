package com.carexport.scraping;

import com.carexport.model.VehicleListing;
import com.carexport.repository.VehicleListingRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class ScrapingScheduler {

    private final ScrapingOrchestrator orchestrator;
    private final VehicleListingRepository repository;

    public ScrapingScheduler(ScrapingOrchestrator orchestrator, VehicleListingRepository repository) {
        this.orchestrator = orchestrator;
        this.repository = repository;
    }

    @Scheduled(fixedRate = 6, timeUnit = TimeUnit.HOURS)
    public void refreshListings() {
        SearchCriteria criteria = SearchCriteria.defaultExportCriteria();
        List<VehicleListing> listings = orchestrator.collectAll(criteria);

        for (VehicleListing v : listings) {
            repository.findByExternalUrl(v.getExternalUrl())
                .ifPresentOrElse(
                    existing -> updateIfChanged(existing, v),
                    () -> repository.save(v)
                );
        }
    }

    private void updateIfChanged(VehicleListing existing, VehicleListing fresh) {
        if (!existing.getPrice().equals(fresh.getPrice())) {
            existing.setPrice(fresh.getPrice());
            existing.setScrapedAt(fresh.getScrapedAt());
            repository.save(existing);
        }
    }
}

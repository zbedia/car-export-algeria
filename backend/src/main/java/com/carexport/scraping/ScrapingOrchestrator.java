package com.carexport.scraping;

import com.carexport.model.VehicleListing;
import com.carexport.service.ScrapingHealthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScrapingOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(ScrapingOrchestrator.class);

    private final List<VehicleSourceConnector> connectors;
    private final ScrapingHealthService healthService;

    public ScrapingOrchestrator(List<VehicleSourceConnector> connectors, ScrapingHealthService healthService) {
        this.connectors = connectors;
        this.healthService = healthService;
    }

    public List<VehicleListing> collectAll(SearchCriteria criteria) {
        return connectors.parallelStream()
            .flatMap(c -> safeFetch(c, criteria).stream())
            .collect(Collectors.toList());
    }

    private List<VehicleListing> safeFetch(VehicleSourceConnector c, SearchCriteria criteria) {
        try {
            List<VehicleListing> result = c.fetchListings(criteria);
            healthService.recordSuccess(c.getSourceName(), result.size());
            return result;
        } catch (Exception e) {
            healthService.recordFailure(c.getSourceName(), e.getMessage());
            log.warn("Source failed {}: {}", c.getSourceName(), e.getMessage());
            return List.of();
        }
    }
}

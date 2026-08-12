package com.carexport.scraping;

import com.carexport.model.VehicleListing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScrapingOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(ScrapingOrchestrator.class);

    private final List<VehicleSourceConnector> connectors;

    public ScrapingOrchestrator(List<VehicleSourceConnector> connectors) {
        this.connectors = connectors;
    }

    public List<VehicleListing> collectAll(SearchCriteria criteria) {
        return connectors.parallelStream()
            .flatMap(c -> safeFetch(c, criteria).stream())
            .collect(Collectors.toList());
    }

    private List<VehicleListing> safeFetch(VehicleSourceConnector c, SearchCriteria criteria) {
        try {
            return c.fetchListings(criteria);
        } catch (Exception e) {
            log.warn("Source failed {}: {}", c.getSourceName(), e.getMessage());
            return List.of();
        }
    }
}

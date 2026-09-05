package com.carexport.controller;

import com.carexport.dto.HealthResponse;
import com.carexport.dto.SourceHealthDto;
import com.carexport.scraping.ScrapingScheduler;
import com.carexport.service.ScrapingHealthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/health")
public class ScrapingHealthController {

    private final ScrapingHealthService healthService;
    private final ScrapingScheduler scrapingScheduler;

    public ScrapingHealthController(ScrapingHealthService healthService, ScrapingScheduler scrapingScheduler) {
        this.healthService = healthService;
        this.scrapingScheduler = scrapingScheduler;
    }

    /**
     * Current snapshot from the last scrape round (scheduled or manual).
     */
    @GetMapping
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(buildResponse());
    }

    /**
     * Runs a full refresh round NOW (same code path as the 6-hourly
     * scheduler: fetch, persist, evict the search cache) and returns the
     * fresh health snapshot. Blocking — a round can take a couple of
     * minutes when a marketplace is back online.
     */
    @PostMapping("/refresh")
    public ResponseEntity<HealthResponse> refresh() {
        scrapingScheduler.refreshListings();
        return ResponseEntity.ok(buildResponse());
    }

    private HealthResponse buildResponse() {
        List<SourceHealthDto> sources = healthService.snapshot();
        return new HealthResponse(overall(sources), sources);
    }

    private static String overall(List<SourceHealthDto> sources) {
        if (sources.isEmpty()) {
            return "UNKNOWN";
        }
        SourceHealthDto.Status status = sources.stream()
                .map(SourceHealthDto::status)
                .reduce(SourceHealthDto.Status.UP, ScrapingHealthController::merge);
        return status.name();
    }

    private static SourceHealthDto.Status merge(SourceHealthDto.Status a, SourceHealthDto.Status b) {
        if (a == SourceHealthDto.Status.DOWN || b == SourceHealthDto.Status.DOWN) {
            return SourceHealthDto.Status.DOWN;
        }
        if (a == SourceHealthDto.Status.EMPTY || b == SourceHealthDto.Status.EMPTY) {
            return SourceHealthDto.Status.EMPTY;
        }
        return SourceHealthDto.Status.UP;
    }
}
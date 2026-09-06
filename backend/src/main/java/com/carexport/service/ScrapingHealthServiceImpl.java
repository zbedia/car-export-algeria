package com.carexport.service;

import com.carexport.dto.SourceHealthDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ScrapingHealthServiceImpl implements ScrapingHealthService {

    private static final Logger log = LoggerFactory.getLogger(ScrapingHealthServiceImpl.class);

    private final Map<String, SourceHealth> sources = new ConcurrentHashMap<>();

    @Override
    public void recordSuccess(String source, int vehicleCount) {
        sources.computeIfAbsent(source, SourceHealth::new).markSuccess(vehicleCount);
        if (vehicleCount == 0) {
            log.warn("[ScrapeHealth] {} succeeded but returned 0 vehicles — site may be down", source);
        }
    }

    @Override
    public void recordFailure(String source, String message) {
        sources.computeIfAbsent(source, SourceHealth::new).markFailure(message);
        log.warn("[ScrapeHealth] {} failed this round: {}", source, message);
    }

    @Override
    public List<SourceHealthDto> snapshot() {
        return sources.values().stream()
                .map(SourceHealth::toDto)
                .sorted(Comparator.comparing(SourceHealthDto::source))
                .toList();
    }

    @Override
    public boolean isEmpty() {
        return sources.isEmpty();
    }

    private static final class SourceHealth {

        private final String source;
        private boolean lastSucceeded;
        private long successCount;
        private long failureCount;
        private long consecutiveFailures;
        private Instant lastSuccessAt;
        private Instant lastFailureAt;
        private String lastError;
        private int lastVehicleCount;

        private SourceHealth(String source) {
            this.source = source;
        }

        private synchronized void markSuccess(int vehicleCount) {
            lastSucceeded = true;
            successCount++;
            consecutiveFailures = 0;
            lastSuccessAt = Instant.now();
            lastVehicleCount = vehicleCount;
            lastError = null;
        }

        private synchronized void markFailure(String error) {
            lastSucceeded = false;
            failureCount++;
            consecutiveFailures++;
            lastFailureAt = Instant.now();
            lastError = error;
        }

        private synchronized SourceHealthDto toDto() {
            SourceHealthDto.Status status;
            if (!lastSucceeded) {
                status = SourceHealthDto.Status.DOWN;
            } else if (lastVehicleCount == 0) {
                status = SourceHealthDto.Status.EMPTY;
            } else {
                status = SourceHealthDto.Status.UP;
            }
            return new SourceHealthDto(source, status, successCount, failureCount, consecutiveFailures,
                    lastSuccessAt, lastFailureAt, lastVehicleCount, lastError);
        }
    }
}
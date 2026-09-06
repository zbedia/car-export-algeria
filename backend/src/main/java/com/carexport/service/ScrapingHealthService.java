package com.carexport.service;

import com.carexport.dto.SourceHealthDto;

import java.util.List;

/**
 * In-memory, thread-safe health tracker for the scraping sources.
 * The picture is intentionally "last round" based — a paused marketplace
 * shows up as {@code EMPTY} instead of silently looking healthy.
 */
public interface ScrapingHealthService {

    void recordSuccess(String source, int vehicleCount);

    void recordFailure(String source, String message);

    List<SourceHealthDto> snapshot();

    boolean isEmpty();
}
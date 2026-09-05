package com.carexport.dto;

import java.time.Instant;

/**
 * Health snapshot for one scraping source (e.g. CarXport, ExportCar213).
 *
 * {@code status} reflects only the LAST scrape round:
 * <ul>
 *   <li>UP   — last round succeeded and produced vehicles,</li>
 *   <li>EMPTY — last round succeeded but produced ZERO vehicles (e.g. the
 *              marketplace page is up but shows a "deployment paused" banner
 *              instead of the listing grid),</li>
 *   <li>DOWN — last round threw (network error, HTTP failure, parse error).</li>
 * </ul>
 */
public record SourceHealthDto(
        String source,
        Status status,
        long successCount,
        long failureCount,
        long consecutiveFailures,
        Instant lastSuccessAt,
        Instant lastFailureAt,
        Integer lastVehicleCount,
        String lastError
) {
    public enum Status { UP, EMPTY, DOWN }
}
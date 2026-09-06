package com.carexport.service;

import com.carexport.dto.SourceHealthDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScrapingHealthServiceTest {

    private final ScrapingHealthService healthService = new ScrapingHealthServiceImpl();

    @Test
    void recordsSuccess_andFailure_alongsideCountsAndTimeStamps() {
        healthService.recordSuccess("CarXport", 24);
        healthService.recordFailure("ExportCar213", "connect timed out");

        List<SourceHealthDto> snapshot = healthService.snapshot();

        assertThat(snapshot).hasSize(2);

        SourceHealthDto carxport = find(snapshot, "CarXport");
        assertThat(carxport.status()).isEqualTo(SourceHealthDto.Status.UP);
        assertThat(carxport.successCount()).isEqualTo(1);
        assertThat(carxport.failureCount()).isZero();
        assertThat(carxport.lastVehicleCount()).isEqualTo(24);
        assertThat(carxport.lastSuccessAt()).isNotNull();

        SourceHealthDto exporter = find(snapshot, "ExportCar213");
        assertThat(exporter.status()).isEqualTo(SourceHealthDto.Status.DOWN);
        assertThat(exporter.failureCount()).isEqualTo(1);
        assertThat(exporter.consecutiveFailures()).isEqualTo(1);
        assertThat(exporter.lastError()).isEqualTo("connect timed out");
    }

    @Test
    void marksEmpty_whenLastRoundSucceeded_withZeroVehicles() {
        healthService.recordSuccess("ExportCar213", 0);

        SourceHealthDto dto = find(healthService.snapshot(), "ExportCar213");
        assertThat(dto.status()).isEqualTo(SourceHealthDto.Status.EMPTY);
    }

    @Test
    void resetsConsecutiveFailures_onRecovery_butKeepsFailureHistory() {
        healthService.recordFailure("CarXport", "boom");
        healthService.recordFailure("CarXport", "boom again");
        healthService.recordSuccess("CarXport", 5);

        SourceHealthDto dto = find(healthService.snapshot(), "CarXport");
        assertThat(dto.failureCount()).isEqualTo(2);
        assertThat(dto.consecutiveFailures()).isZero();
        assertThat(dto.status()).isEqualTo(SourceHealthDto.Status.UP);
    }

    @Test
    void snapshotIsEmpty_whenNothingRecordedYet() {
        assertThat(healthService.snapshot()).isEmpty();
    }

    private static SourceHealthDto find(List<SourceHealthDto> snapshot, String source) {
        return snapshot.stream()
                .filter(d -> d.source().equals(source))
                .findFirst()
                .orElseThrow();
    }
}
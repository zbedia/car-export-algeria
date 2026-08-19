package com.carexport.repository;

import com.carexport.model.FuelType;
import com.carexport.model.VehicleListing;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Uses a real (in-memory H2) database via @DataJpaTest, unlike
 * VehicleSearchServiceTest which mocks the repository — this is what
 * actually verifies the JPQL query behaves as written, including the
 * partial city match (LIKE) added here.
 */
@DataJpaTest
class VehicleListingRepositoryTest {

    @Autowired
    private VehicleListingRepository repository;

    @Test
    void search_matchesCityPartially_caseInsensitive() {
        // "Bordeaux" doesn't appear in data.sql's seed cities (Lyon, Marseille,
        // Paris, Toulouse, Nice), so searching "bord" can only match this row —
        // regardless of whether the seed data is also loaded in this test.
        repository.save(buildListing("Bordeaux"));

        List<VehicleListing> results = repository.search(
            null, null, new BigDecimal("999999999"), null, "bord", null,
            LocalDate.now().minusYears(2).minusMonths(10)
        );

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getGarageCity()).isEqualTo("Bordeaux");
    }

    @Test
    void search_returnsNothing_whenCityDoesNotMatch() {
        List<VehicleListing> results = repository.search(
            null, null, new BigDecimal("999999999"), null, "bord", null,
            LocalDate.now().minusYears(2).minusMonths(10)
        );

        assertThat(results).isEmpty();
    }

    private VehicleListing buildListing(String city) {
        VehicleListing v = new VehicleListing();
        v.setSource("TestSource");
        v.setExternalUrl(UUID.randomUUID().toString());
        v.setBrand("Peugeot");
        v.setModel("308");
        v.setYear(2025);
        v.setMileageKm(15000);
        v.setPrice(new BigDecimal("18000"));
        v.setCurrency("EUR");
        v.setGarageCity(city);
        v.setScrapedAt(LocalDateTime.now());
        v.setFuelType(FuelType.ESSENCE);
        v.setEngineDisplacementCm3(1600);
        v.setFirstRegistrationDate(LocalDate.now().minusMonths(10));
        return v;
    }
}

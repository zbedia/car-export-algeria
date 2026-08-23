package com.carexport.repository;

import com.carexport.model.FuelType;
import com.carexport.model.VehicleListing;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Uses a real (in-memory H2) database via @DataJpaTest to verify the
 * actual filtering behavior of VehicleSpecifications — this is what
 * VehicleSearchServiceTest can't cover, since it mocks the repository.
 */
@DataJpaTest
class VehicleSpecificationsTest {

    @Autowired
    private VehicleListingRepository repository;

    @Test
    void cityContains_matchesPartially_caseInsensitive() {
        // "Bordeaux" doesn't appear in data.sql's seed cities (Lyon, Marseille,
        // Paris, Toulouse, Nice), so searching "bord" can only match this row —
        // regardless of whether the seed data is also loaded in this test.
        repository.save(buildListing("Bordeaux", FuelType.ESSENCE, 1600, 15000));

        List<VehicleListing> results = repository.findAll(VehicleSpecifications.cityContains("bord"));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getGarageCity()).isEqualTo("Bordeaux");
    }

    @Test
    void cityContains_returnsNothing_whenNoMatch() {
        List<VehicleListing> results = repository.findAll(VehicleSpecifications.cityContains("bord"));

        assertThat(results).isEmpty();
    }

    @Test
    void mileageAtMost_excludesHigherMileage() {
        repository.save(buildListing("Strasbourg", FuelType.ESSENCE, 1600, 10000));
        repository.save(buildListing("Strasbourg", FuelType.ESSENCE, 1600, 90000));

        List<VehicleListing> results = repository.findAll(
            Specification.where(VehicleSpecifications.cityContains("strasbourg"))
                .and(VehicleSpecifications.mileageAtMost(50000))
        );

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getMileageKm()).isEqualTo(10000);
    }

    @Test
    void fuelTypeNot_excludesDiesel() {
        repository.save(buildListing("LeHavre", FuelType.DIESEL, 1600, 15000));
        repository.save(buildListing("LeHavre", FuelType.ESSENCE, 1600, 15000));

        List<VehicleListing> results = repository.findAll(
            Specification.where(VehicleSpecifications.cityContains("lehavre"))
                .and(VehicleSpecifications.fuelTypeNot(FuelType.DIESEL))
        );

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFuelType()).isEqualTo(FuelType.ESSENCE);
    }

    @Test
    void combinedSpecifications_applyAllFiltersTogether() {
        repository.save(buildListing("Rennes", FuelType.ESSENCE, 1600, 15000)); // matches everything
        repository.save(buildListing("Rennes", FuelType.ESSENCE, 1600, 80000)); // mileage too high
        repository.save(buildListing("Rennes", FuelType.DIESEL, 1600, 15000));  // wrong fuel type

        Specification<VehicleListing> spec = Specification
            .where(VehicleSpecifications.cityContains("rennes"))
            .and(VehicleSpecifications.mileageAtMost(50000))
            .and(VehicleSpecifications.fuelTypeEquals(FuelType.ESSENCE));

        List<VehicleListing> results = repository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getMileageKm()).isEqualTo(15000);
    }

    private VehicleListing buildListing(String city, FuelType fuelType, Integer displacementCm3, int mileageKm) {
        VehicleListing v = new VehicleListing();
        v.setSource("TestSource");
        v.setExternalUrl(UUID.randomUUID().toString());
        v.setBrand("Peugeot");
        v.setModel("308");
        v.setYear(2025);
        v.setMileageKm(mileageKm);
        v.setPrice(new BigDecimal("18000"));
        v.setCurrency("EUR");
        v.setGarageCity(city);
        v.setScrapedAt(LocalDateTime.now());
        v.setFuelType(fuelType);
        v.setEngineDisplacementCm3(displacementCm3);
        v.setFirstRegistrationDate(LocalDate.now().minusMonths(10));
        return v;
    }
}

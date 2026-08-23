package com.carexport.service;

import com.carexport.dto.SearchRequest;
import com.carexport.dto.VehicleSearchResult;
import com.carexport.model.FuelType;
import com.carexport.model.VehicleListing;
import com.carexport.repository.VehicleListingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests with a mocked repository — these check VehicleSearchService's
 * own logic (best-price scoring, discount exposure), not the actual
 * filtering behavior of the Specifications themselves. Since a
 * Specification is a lambda, its content can't be meaningfully asserted
 * on via Mockito matchers; the real filtering behavior is covered by
 * VehicleSpecificationsTest instead, against a real (H2) database.
 */
@ExtendWith(MockitoExtension.class)
class VehicleSearchServiceTest {

    @Mock
    private VehicleListingRepository repository;

    private VehicleSearchService service;

    @BeforeEach
    void setUp() {
        service = new VehicleSearchService(repository, new ImportEligibilityService());
    }

    @Test
    void search_returnsEmptyList_whenNoResults() {
        when(repository.findAll(any(Specification.class), any(Sort.class)))
            .thenReturn(List.of());

        SearchRequest request = new SearchRequest();
        request.setBrand("Bugatti");

        List<VehicleSearchResult> results = service.search(request);

        assertThat(results).isEmpty();
    }

    @Test
    void search_marksLowestPriceAsBestPrice_perModel() {
        VehicleListing cheap = buildListing("Peugeot", "308", new BigDecimal("15000"));
        VehicleListing expensive = buildListing("Peugeot", "308", new BigDecimal("18000"));
        VehicleListing otherModel = buildListing("Renault", "Clio", new BigDecimal("14000"));

        when(repository.findAll(any(Specification.class), any(Sort.class)))
            .thenReturn(List.of(cheap, expensive, otherModel));

        List<VehicleSearchResult> results = service.search(new SearchRequest());

        assertThat(results).filteredOn(VehicleSearchResult::isBestPrice)
            .hasSize(2) // cheapest vehicle of each model group
            .extracting(VehicleSearchResult::getPrice)
            .containsExactlyInAnyOrder(new BigDecimal("15000"), new BigDecimal("14000"));
    }

    @Test
    void search_exposesCustomsDiscountPercentage() {
        VehicleListing electric = buildListing("Renault", "Zoe", new BigDecimal("20000"));
        electric.setFuelType(FuelType.ELECTRIQUE);
        electric.setEngineDisplacementCm3(null);

        when(repository.findAll(any(Specification.class), any(Sort.class)))
            .thenReturn(List.of(electric));

        List<VehicleSearchResult> results = service.search(new SearchRequest());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getCustomsDiscountPercentage()).isEqualByComparingTo(new BigDecimal("80"));
    }

    private VehicleListing buildListing(String brand, String model, BigDecimal price) {
        VehicleListing v = new VehicleListing();
        v.setBrand(brand);
        v.setModel(model);
        v.setPrice(price);
        v.setYear(2025);
        v.setCurrency("EUR");
        v.setExternalUrl(UUID.randomUUID().toString());
        v.setFuelType(FuelType.ESSENCE);
        v.setEngineDisplacementCm3(1600);
        v.setFirstRegistrationDate(LocalDate.now().minusMonths(10));
        return v;
    }
}

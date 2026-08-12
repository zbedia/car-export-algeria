package com.carexport.service;

import com.carexport.dto.SearchRequest;
import com.carexport.dto.VehicleSearchResult;
import com.carexport.model.VehicleListing;
import com.carexport.repository.VehicleListingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleSearchServiceTest {

    @Mock
    private VehicleListingRepository repository;

    @InjectMocks
    private VehicleSearchService service;

    @Test
    void search_returnsEmptyList_whenNoResults() {
        when(repository.search(any(), any(), anyInt(), any()))
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

        when(repository.search(any(), any(), anyInt(), any()))
            .thenReturn(List.of(cheap, expensive, otherModel));

        List<VehicleSearchResult> results = service.search(new SearchRequest());

        assertThat(results).filteredOn(VehicleSearchResult::isBestPrice)
            .hasSize(2) // cheapest vehicle of each model group
            .extracting(VehicleSearchResult::getPrice)
            .containsExactlyInAnyOrder(new BigDecimal("15000"), new BigDecimal("14000"));
    }

    private VehicleListing buildListing(String brand, String model, BigDecimal price) {
        VehicleListing v = new VehicleListing();
        v.setBrand(brand);
        v.setModel(model);
        v.setPrice(price);
        v.setYear(2024);
        v.setCurrency("EUR");
        v.setExternalUrl(UUID.randomUUID().toString());
        return v;
    }
}

package com.carexport.service;

import com.carexport.dto.SearchRequest;
import com.carexport.dto.VehicleSearchResult;
import com.carexport.model.VehicleListing;
import com.carexport.repository.VehicleListingRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleSearchService {

    private final VehicleListingRepository repository;

    public VehicleSearchService(VehicleListingRepository repository) {
        this.repository = repository;
    }

    public List<VehicleSearchResult> search(SearchRequest request) {
        BigDecimal maxPrice = request.getMaxPrice() != null
            ? request.getMaxPrice()
            : new BigDecimal("999999999");

        List<VehicleListing> listings = repository.search(
            request.getBrand(),
            request.getModel(),
            LocalDate.now().getYear() - 3,
            maxPrice
        );

        // Best price is computed per brand+model group, not globally
        return listings.stream()
            .map(v -> toResult(v, isBestPriceForModel(v, listings)))
            .collect(Collectors.toList());
    }

    private boolean isBestPriceForModel(VehicleListing v, List<VehicleListing> allListings) {
        BigDecimal minForModel = allListings.stream()
            .filter(l -> l.getBrand().equalsIgnoreCase(v.getBrand())
                && l.getModel().equalsIgnoreCase(v.getModel()))
            .map(VehicleListing::getPrice)
            .min(Comparator.naturalOrder())
            .orElse(null);

        return v.getPrice().equals(minForModel);
    }

    private VehicleSearchResult toResult(VehicleListing v, boolean isBestPrice) {
        VehicleSearchResult r = new VehicleSearchResult();
        r.setId(v.getId());
        r.setSource(v.getSource());
        r.setExternalUrl(v.getExternalUrl());
        r.setBrand(v.getBrand());
        r.setModel(v.getModel());
        r.setYear(v.getYear());
        r.setMileageKm(v.getMileageKm());
        r.setPrice(v.getPrice());
        r.setCurrency(v.getCurrency());
        r.setGarageCity(v.getGarageCity());
        r.setBestPrice(isBestPrice);
        return r;
    }
}

package com.carexport.service;

import com.carexport.dto.SearchRequest;
import com.carexport.dto.VehicleSearchResult;
import com.carexport.model.FuelType;
import com.carexport.model.VehicleListing;
import com.carexport.repository.VehicleListingRepository;
import com.carexport.repository.VehicleSpecifications;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleSearchService {

    private final VehicleListingRepository repository;
    private final ImportEligibilityService eligibilityService;

    public VehicleSearchService(VehicleListingRepository repository,
                                 ImportEligibilityService eligibilityService) {
        this.repository = repository;
        this.eligibilityService = eligibilityService;
    }

    /**
     * Runs inside a read-only transaction so every request sees one consistent
     * snapshot of the listings (no torn reads while a refresh is writing), and
     * is cached in the "vehicleSearch" Caffeine cache keyed by the query
     * filters — concurrent users asking the same question hit the cache
     * instead of piling identical queries onto the database. The cache is
     * evicted by ListingUpdateService whenever scraped data changes.
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "vehicleSearch", key = "{#request.brand, #request.model, #request.maxPrice, #request.maxMileageKm, #request.garageCity, #request.fuelType}")
    public List<VehicleSearchResult> search(SearchRequest request) {
        BigDecimal maxPrice = request.getMaxPrice() != null
            ? request.getMaxPrice()
            : new BigDecimal("999999999");

        // Only the filters the user actually provided are added to the
        // query — see VehicleSpecifications for why this is preferable to
        // a single static query with "(:param IS NULL OR ...)" branches.
        // The last two specifications enforce the Algerian import
        // eligibility rules: fuel type must not be DIESEL, and the vehicle
        // must not be older than the eligibility cutoff (2 years 10 months).
        Specification<VehicleListing> spec = Specification
            .where(VehicleSpecifications.brandEquals(request.getBrand()))
            .and(VehicleSpecifications.modelEquals(request.getModel()))
            .and(VehicleSpecifications.priceAtMost(maxPrice))
            .and(VehicleSpecifications.mileageAtMost(request.getMaxMileageKm()))
            .and(VehicleSpecifications.cityContains(request.getGarageCity()))
            .and(VehicleSpecifications.fuelTypeEquals(request.getFuelType()))
            .and(VehicleSpecifications.fuelTypeNot(FuelType.DIESEL))
            .and(VehicleSpecifications.registeredOnOrAfter(eligibilityService.getOldestEligibleRegistrationDate()));

        List<VehicleListing> listings = repository.findAll(spec, Sort.by(Sort.Direction.ASC, "price"));

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
        r.setFuelType(v.getFuelType().name());
        r.setEngineDisplacementCm3(v.getEngineDisplacementCm3());
        if (v.getFuelType() == FuelType.ESSENCE || v.getFuelType() == FuelType.HYBRIDE) {
            r.setEngineDisplacementThresholdCm3(eligibilityService.getEngineDisplacementThresholdCm3());
        }
        r.setCustomsDiscountPercentage(eligibilityService.getCustomsDiscountPercentage(v));
        r.setCustomsDiscountReasonCode(eligibilityService.getCustomsDiscountReasonCode(v).name());
        return r;
    }
}

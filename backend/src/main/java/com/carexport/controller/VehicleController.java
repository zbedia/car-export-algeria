package com.carexport.controller;

import com.carexport.dto.SearchRequest;
import com.carexport.dto.VehicleSearchResult;
import com.carexport.model.FuelType;
import com.carexport.service.VehicleSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleSearchService searchService;

    public VehicleController(VehicleSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<VehicleSearchResult>> search(
        @RequestParam(required = false) String brand,
        @RequestParam(required = false) String model,
        @RequestParam(required = false) BigDecimal maxPrice,
        @RequestParam(required = false) Integer maxMileageKm,
        @RequestParam(required = false) String garageCity,
        @RequestParam(required = false) FuelType fuelType
    ) {
        SearchRequest request = new SearchRequest();
        request.setBrand(brand);
        request.setModel(model);
        request.setMaxPrice(maxPrice);
        request.setMaxMileageKm(maxMileageKm);
        request.setGarageCity(garageCity);
        request.setFuelType(fuelType);

        List<VehicleSearchResult> results = searchService.search(request);
        return ResponseEntity.ok(results);
    }
}

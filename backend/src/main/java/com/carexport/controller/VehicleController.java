package com.carexport.controller;

import com.carexport.dto.SearchRequest;
import com.carexport.dto.VehicleSearchResult;
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
        @RequestParam(required = false) BigDecimal maxPrice
    ) {
        SearchRequest request = new SearchRequest();
        request.setBrand(brand);
        request.setModel(model);
        request.setMaxPrice(maxPrice);

        List<VehicleSearchResult> results = searchService.search(request);
        return ResponseEntity.ok(results);
    }
}

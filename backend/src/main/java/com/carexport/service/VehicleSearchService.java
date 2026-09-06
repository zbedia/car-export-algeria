package com.carexport.service;

import com.carexport.dto.SearchRequest;
import com.carexport.dto.VehicleSearchResult;

import java.util.List;

/**
 * Read path for vehicle comparison: filters the scraped listings against the
 * Algerian import eligibility rules and sorts them by ascending price.
 */
public interface VehicleSearchService {

    List<VehicleSearchResult> search(SearchRequest request);
}
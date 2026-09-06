package com.carexport.service;

import com.carexport.model.VehicleListing;

import java.util.List;

/**
 * Owns the write path to the {@code vehicle_listing} table: the atomic upsert
 * of a scraped batch, and eviction of the search cache when data changes.
 */
public interface ListingUpdateService {

    void persist(List<VehicleListing> listings);
}
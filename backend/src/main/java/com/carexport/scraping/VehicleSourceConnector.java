package com.carexport.scraping;

import com.carexport.model.VehicleListing;

import java.util.List;

public interface VehicleSourceConnector {
    String getSourceName();
    List<VehicleListing> fetchListings(SearchCriteria criteria);
}

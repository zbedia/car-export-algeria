package com.carexport.repository;

import com.carexport.model.VehicleListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JpaSpecificationExecutor enables dynamic query building via
 * VehicleSpecifications, used by VehicleSearchService.search() — see
 * that class for the reasoning behind moving away from a single static
 * JPQL query with optional "(:param IS NULL OR ...)" branches.
 */
@Repository
public interface VehicleListingRepository extends JpaRepository<VehicleListing, Long>,
                                                    JpaSpecificationExecutor<VehicleListing> {

    Optional<VehicleListing> findByExternalUrl(String externalUrl);

    @Query("""
        SELECT v FROM VehicleListing v
        WHERE v.brand = :brand AND v.model = :model
        ORDER BY v.price ASC
        LIMIT 1
        """)
    Optional<VehicleListing> findBestPrice(@Param("brand") String brand, @Param("model") String model);
}

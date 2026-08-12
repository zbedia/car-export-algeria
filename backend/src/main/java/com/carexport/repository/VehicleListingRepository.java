package com.carexport.repository;

import com.carexport.model.VehicleListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleListingRepository extends JpaRepository<VehicleListing, Long> {

    Optional<VehicleListing> findByExternalUrl(String externalUrl);

    List<VehicleListing> findByBrandIgnoreCaseAndModelIgnoreCase(String brand, String model);

    @Query("""
        SELECT v FROM VehicleListing v
        WHERE (:brand IS NULL OR LOWER(v.brand) = LOWER(:brand))
        AND (:model IS NULL OR LOWER(v.model) = LOWER(:model))
        AND v.year >= :minYear
        AND v.price <= :maxPrice
        ORDER BY v.price ASC
        """)
    List<VehicleListing> search(
        @Param("brand") String brand,
        @Param("model") String model,
        @Param("minYear") int minYear,
        @Param("maxPrice") BigDecimal maxPrice
    );

    @Query("""
        SELECT v FROM VehicleListing v
        WHERE v.brand = :brand AND v.model = :model
        ORDER BY v.price ASC
        LIMIT 1
        """)
    Optional<VehicleListing> findBestPrice(@Param("brand") String brand, @Param("model") String model);
}

package com.carexport.repository;

import com.carexport.model.FuelType;
import com.carexport.model.VehicleListing;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Reusable, composable search predicates for VehicleListing, used with
 * JpaSpecificationExecutor instead of a single static JPQL query with
 * "(:param IS NULL OR condition)" branches.
 *
 * Two concrete benefits over the previous approach:
 * - PostgreSQL only sees the predicates that actually apply to a given
 *   search, so it can pick a query plan (and use indexes) suited to that
 *   specific combination of filters, instead of one generic plan that has
 *   to stay valid for every possible combination of null/non-null filters.
 * - Each predicate's parameter type comes directly from the entity
 *   metamodel (via CriteriaBuilder), which sidesteps the PostgreSQL JDBC
 *   driver's parameter type inference entirely — the "function lower(bytea)
 *   does not exist" class of bugs we hit with the JPQL LIKE clause simply
 *   doesn't occur here.
 *
 * Each method returns null when the filter isn't provided; Specification's
 * and()/or() combinators treat a null Specification as "no restriction",
 * so callers can chain all of these unconditionally.
 */
public class VehicleSpecifications {

    private VehicleSpecifications() {
    }

    public static Specification<VehicleListing> brandEquals(String brand) {
        if (brand == null || brand.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(cb.lower(root.get("brand")), brand.toLowerCase());
    }

    public static Specification<VehicleListing> modelEquals(String model) {
        if (model == null || model.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(cb.lower(root.get("model")), model.toLowerCase());
    }

    public static Specification<VehicleListing> priceAtMost(BigDecimal maxPrice) {
        if (maxPrice == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<VehicleListing> mileageAtMost(Integer maxMileageKm) {
        if (maxMileageKm == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("mileageKm"), maxMileageKm);
    }

    /** Partial, case-insensitive city match (e.g. "lyo" matches "Lyon"). */
    public static Specification<VehicleListing> cityContains(String garageCity) {
        if (garageCity == null || garageCity.isBlank()) {
            return null;
        }
        String pattern = "%" + garageCity.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("garageCity")), pattern);
    }

    public static Specification<VehicleListing> fuelTypeEquals(FuelType fuelType) {
        if (fuelType == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("fuelType"), fuelType);
    }

    public static Specification<VehicleListing> fuelTypeNot(FuelType fuelType) {
        return (root, query, cb) -> cb.notEqual(root.get("fuelType"), fuelType);
    }

    public static Specification<VehicleListing> registeredOnOrAfter(LocalDate cutoffDate) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("firstRegistrationDate"), cutoffDate);
    }
}

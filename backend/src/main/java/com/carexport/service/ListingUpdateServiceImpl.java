package com.carexport.service;

import com.carexport.model.VehicleListing;
import com.carexport.repository.VehicleListingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * The scheduler scrapes over the network (slow) and must never hold a DB
 * transaction open while it does so. It therefore hands the already-built
 * in-memory listings to this service, whose {@link #persist(List)} method
 * wraps the whole upsert loop in a single transaction:
 *   - the find-then-update (read-modify-write) is atomic, so two concurrent
 *     refreshes can no longer both read the old price and both write it back,
 *   - concurrent inserts of the same {@code externalUrl} are caught via the
 *     unique constraint and re-routed to an update, instead of crashing,
 *   - the optimistic {@code version} column guards against lost updates on
 *     the rare truly-overlapping writes.
 *
 * The search cache is evicted when listings change, so concurrent readers always
 * observe fresh results instead of a stale aggregate.
 */
@Service
public class ListingUpdateServiceImpl implements ListingUpdateService {

    private static final Logger log = LoggerFactory.getLogger(ListingUpdateServiceImpl.class);

    private final VehicleListingRepository repository;

    public ListingUpdateServiceImpl(VehicleListingRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "vehicleSearch", allEntries = true)
    public void persist(List<VehicleListing> listings) {
        for (VehicleListing v : listings) {
            v.setPrice(v.getPrice().setScale(0, RoundingMode.HALF_UP));
            repository.findByExternalUrl(v.getExternalUrl())
                .ifPresentOrElse(
                    existing -> updateIfChanged(existing, v),
                    () -> insertIfAbsent(v)
                );
        }
    }

    private void insertIfAbsent(VehicleListing v) {
        try {
            repository.saveAndFlush(v);
        } catch (DataIntegrityViolationException e) {
            // A concurrent refresh created this listing in the meantime:
            // the unique constraint on external_url fired, so rebuild the
            // existing row from disk and apply the fresh data on top of it.
            log.info("Listing {} was inserted concurrently; updating it instead", v.getExternalUrl());
            repository.findByExternalUrl(v.getExternalUrl())
                .ifPresent(existing -> updateIfChanged(existing, v));
        }
    }

    private void updateIfChanged(VehicleListing existing, VehicleListing fresh) {
        if (hasChanges(existing, fresh)) {
            existing.setBrand(fresh.getBrand());
            existing.setModel(fresh.getModel());
            existing.setYear(fresh.getYear());
            existing.setMileageKm(fresh.getMileageKm());
            existing.setPrice(fresh.getPrice());
            existing.setCurrency(fresh.getCurrency());
            existing.setGarageCity(fresh.getGarageCity());
            existing.setFuelType(fresh.getFuelType());
            existing.setEngineDisplacementCm3(fresh.getEngineDisplacementCm3());
            existing.setFirstRegistrationDate(fresh.getFirstRegistrationDate());
            existing.setImageUrl(fresh.getImageUrl());
            existing.setScrapedAt(fresh.getScrapedAt());
            repository.saveAndFlush(existing);
        }
    }

    private boolean hasChanges(VehicleListing existing, VehicleListing fresh) {
        return !Objects.equals(existing.getBrand(), fresh.getBrand())
                || !Objects.equals(existing.getModel(), fresh.getModel())
                || existing.getYear() != fresh.getYear()
                || existing.getMileageKm() != fresh.getMileageKm()
                || !Objects.equals(existing.getPrice(), fresh.getPrice())
                || !Objects.equals(existing.getCurrency(), fresh.getCurrency())
                || !Objects.equals(existing.getGarageCity(), fresh.getGarageCity())
                || existing.getFuelType() != fresh.getFuelType()
                || !Objects.equals(existing.getEngineDisplacementCm3(), fresh.getEngineDisplacementCm3())
                || !Objects.equals(existing.getFirstRegistrationDate(), fresh.getFirstRegistrationDate())
                || !Objects.equals(existing.getImageUrl(), fresh.getImageUrl());
    }
}
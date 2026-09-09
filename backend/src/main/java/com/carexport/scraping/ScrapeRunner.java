package com.carexport.scraping;

import com.carexport.model.VehicleListing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Runs the per-source "fetch each detail page and parse it" loop concurrently.
 *
 * All three connectors share the same shape — enumerate N URLs, then turn each
 * into a {@link VehicleListing} — so the concurrency behaviour (one fixed pool
 * per round, one failure never aborting the batch, a politeness sleep per
 * worker) lives here instead of being copy-pasted three times.
 *
 * Result ordering is not preserved; upserts by {@code externalUrl} make that
 * irrelevant to {@code ListingUpdateService}. A task returning {@code null}
 * counts as a skip (e.g. a non-vehicle listing), a thrown exception as a
 * failure.
 */
public final class ScrapeRunner {

    private static final Logger log = LoggerFactory.getLogger(ScrapeRunner.class);

    private ScrapeRunner() {
    }

    /** Per-item job: fetch + parse one detail URL. */
    public interface Task {
        VehicleListing run(String url) throws Exception;
    }

    public record ScrapeOutcome(List<VehicleListing> results, int failures, int skipped) {}

    /**
     * @param sourceName  connector name, used in log lines
     * @param urls        detail page URLs to process
     * @param concurrency maximum number of worker threads (bounded by
     *                    {@code urls.size()})
     * @param politenessMs sleep each worker waits after its own request
     * @param task        per-URL fetch+parse job
     */
    public static ScrapeOutcome run(String sourceName, List<String> urls, int concurrency,
                                    long politenessMs, Task task) {
        if (concurrency < 1) {
            throw new IllegalArgumentException("concurrency must be >= 1, was " + concurrency);
        }
        if (urls.isEmpty()) {
            return new ScrapeOutcome(List.of(), 0, 0);
        }

        ExecutorService pool = Executors.newFixedThreadPool(Math.min(concurrency, urls.size()));
        List<VehicleListing> results = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger failures = new AtomicInteger();
        AtomicInteger skipped = new AtomicInteger();

        for (String url : urls) {
            pool.submit(() -> {
                try {
                    VehicleListing listing = task.run(url);
                    if (listing == null) {
                        skipped.incrementAndGet();
                    } else {
                        results.add(listing);
                    }
                } catch (Exception e) {
                    failures.incrementAndGet();
                    log.warn("[{}] Failed to parse detail page {}: {}", sourceName, url, e.getMessage());
                } finally {
                    sleepQuietly(politenessMs);
                }
            });
        }

        pool.shutdown();
        try {
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return new ScrapeOutcome(List.copyOf(results), failures.get(), skipped.get());
    }

    private static void sleepQuietly(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
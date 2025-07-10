package com.guardianes.shared.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Collects and reports cache performance metrics. Provides insights into cache hit rates, miss
 * rates, and performance impact.
 */
@Component
public class CacheMetricsCollector {

    private static final Logger logger = LoggerFactory.getLogger(CacheMetricsCollector.class);

    private final MeterRegistry meterRegistry;
    private final CacheManager cacheManager;

    // Cache statistics
    private final AtomicLong totalCacheHits = new AtomicLong(0);
    private final AtomicLong totalCacheMisses = new AtomicLong(0);
    private final AtomicLong totalCacheEvictions = new AtomicLong(0);

    public CacheMetricsCollector(MeterRegistry meterRegistry, CacheManager cacheManager) {
        this.meterRegistry = meterRegistry;
        this.cacheManager = cacheManager;

        // Register gauges for cache statistics
        Gauge.builder("cache.hit.ratio", this, CacheMetricsCollector::calculateHitRatio)
                .description("Cache hit ratio (hits / (hits + misses))")
                .register(meterRegistry);

        Gauge.builder("cache.size.total", this, CacheMetricsCollector::getTotalCacheSize)
                .description("Total number of cached entries across all caches")
                .register(meterRegistry);
    }

    /** Records a cache hit event */
    public void recordCacheHit(String cacheName, String key) {
        Counter.builder("cache.hits")
                .tag("cache", cacheName)
                .tag("operation", "hit")
                .register(meterRegistry)
                .increment();
        totalCacheHits.incrementAndGet();

        logger.debug("Cache hit recorded - Cache: {}, Key: {}", cacheName, key);
    }

    /** Records a cache miss event */
    public void recordCacheMiss(String cacheName, String key) {
        Counter.builder("cache.misses")
                .tag("cache", cacheName)
                .tag("operation", "miss")
                .register(meterRegistry)
                .increment();
        totalCacheMisses.incrementAndGet();

        logger.debug("Cache miss recorded - Cache: {}, Key: {}", cacheName, key);
    }

    /** Records a cache eviction event */
    public void recordCacheEviction(String cacheName, String key, String reason) {
        Counter.builder("cache.evictions")
                .tag("cache", cacheName)
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
        totalCacheEvictions.incrementAndGet();

        logger.debug(
                "Cache eviction recorded - Cache: {}, Key: {}, Reason: {}", cacheName, key, reason);
    }

    /** Times a cache operation */
    public Timer.Sample startCacheOperationTimer(String cacheName, String operation) {
        return Timer.start(meterRegistry);
    }

    /** Stops the cache operation timer */
    public void stopCacheOperationTimer(Timer.Sample sample, String cacheName, String operation) {
        sample.stop(
                Timer.builder("cache.operation.time")
                        .tag("cache", cacheName)
                        .tag("operation", operation)
                        .register(meterRegistry));
    }

    /** Calculates cache hit ratio */
    private double calculateHitRatio() {
        long hits = totalCacheHits.get();
        long misses = totalCacheMisses.get();
        long total = hits + misses;

        if (total == 0) {
            return 0.0;
        }

        return (double) hits / total;
    }

    /** Gets total cache size across all caches */
    private long getTotalCacheSize() {
        return cacheManager.getCacheNames().stream().mapToLong(this::getCacheSize).sum();
    }

    /** Gets size of a specific cache */
    private long getCacheSize(String cacheName) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null
                    && cache.getNativeCache()
                            instanceof org.springframework.data.redis.cache.RedisCache) {
                // For Redis cache, we'll estimate based on key patterns
                // In a real implementation, you might want to use Redis commands to get actual size
                return 0; // Placeholder - Redis doesn't easily provide size without scanning
            }
            return 0;
        } catch (Exception e) {
            logger.warn("Failed to get cache size for cache: {}", cacheName, e);
            return 0;
        }
    }

    /** Scheduled task to collect and report cache statistics */
    @Scheduled(fixedRate = 60000) // Every minute
    public void collectCacheStatistics() {
        try {
            double hitRatio = calculateHitRatio();
            long totalSize = getTotalCacheSize();

            logger.debug(
                    "Cache Statistics - Hit Ratio: {:.2f}%, Total Size: {}, "
                            + "Total Hits: {}, Total Misses: {}, Total Evictions: {}",
                    hitRatio * 100,
                    totalSize,
                    totalCacheHits.get(),
                    totalCacheMisses.get(),
                    totalCacheEvictions.get());

            // Record per-cache metrics
            for (String cacheName : cacheManager.getCacheNames()) {
                recordCacheSpecificMetrics(cacheName);
            }

        } catch (Exception e) {
            logger.error("Error collecting cache statistics", e);
        }
    }

    /** Records metrics for a specific cache */
    private void recordCacheSpecificMetrics(String cacheName) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                // Register cache-specific gauges
                Gauge.builder("cache.size", cache, c -> getCacheSize(cacheName))
                        .description("Number of entries in cache")
                        .tag("cache", cacheName)
                        .register(meterRegistry);
            }
        } catch (Exception e) {
            logger.warn("Failed to record metrics for cache: {}", cacheName, e);
        }
    }

    /** Gets cache performance summary for monitoring dashboards */
    public CachePerformanceSummary getCachePerformanceSummary() {
        return new CachePerformanceSummary(
                totalCacheHits.get(),
                totalCacheMisses.get(),
                totalCacheEvictions.get(),
                calculateHitRatio(),
                getTotalCacheSize());
    }

    /** Cache performance summary data class */
    public static class CachePerformanceSummary {
        private final long totalHits;
        private final long totalMisses;
        private final long totalEvictions;
        private final double hitRatio;
        private final long totalSize;

        public CachePerformanceSummary(
                long totalHits,
                long totalMisses,
                long totalEvictions,
                double hitRatio,
                long totalSize) {
            this.totalHits = totalHits;
            this.totalMisses = totalMisses;
            this.totalEvictions = totalEvictions;
            this.hitRatio = hitRatio;
            this.totalSize = totalSize;
        }

        // Getters
        public long getTotalHits() {
            return totalHits;
        }

        public long getTotalMisses() {
            return totalMisses;
        }

        public long getTotalEvictions() {
            return totalEvictions;
        }

        public double getHitRatio() {
            return hitRatio;
        }

        public long getTotalSize() {
            return totalSize;
        }

        public double getHitRatioPercentage() {
            return hitRatio * 100;
        }

        public long getTotalOperations() {
            return totalHits + totalMisses;
        }
    }
}

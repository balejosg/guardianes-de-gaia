package com.guardianes.shared.infrastructure.cache;

import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.walking.application.service.CachedEnergyService;
import com.guardianes.walking.application.service.CachedStepAggregationService;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service for warming up caches during off-peak hours. Preloads frequently accessed data to improve
 * response times.
 */
@Service
public class CacheWarmupService {

    private static final Logger logger = LoggerFactory.getLogger(CacheWarmupService.class);

    private final CachedStepAggregationService cachedStepAggregationService;
    private final CachedEnergyService cachedEnergyService;

    public CacheWarmupService(
            CachedStepAggregationService cachedStepAggregationService,
            CachedEnergyService cachedEnergyService) {
        this.cachedStepAggregationService = cachedStepAggregationService;
        this.cachedEnergyService = cachedEnergyService;
    }

    /**
     * Scheduled cache warmup - runs every day at 5 AM During off-peak hours to minimize impact on
     * user requests
     */
    @Scheduled(cron = "0 0 5 * * *") // 5:00 AM daily
    public void scheduledCacheWarmup() {
        logger.info("Starting scheduled cache warmup");

        try {
            // Warm up caches for active guardians
            warmupActiveGuardianCaches();

            logger.info("Scheduled cache warmup completed successfully");
        } catch (Exception e) {
            logger.error("Error during scheduled cache warmup", e);
        }
    }

    /** Warms up caches for recently active guardians */
    @Async("taskExecutor")
    public CompletableFuture<Void> warmupActiveGuardianCaches() {
        try {
            // In a real implementation, you would query for active guardians
            // For now, we'll simulate with some example guardian IDs
            List<GuardianId> activeGuardians = getActiveGuardianIds();

            logger.info("Warming up caches for {} active guardians", activeGuardians.size());

            for (GuardianId guardianId : activeGuardians) {
                warmupGuardianCache(guardianId);
            }

            logger.info("Cache warmup completed for {} guardians", activeGuardians.size());

        } catch (Exception e) {
            logger.error("Error warming up active guardian caches", e);
        }

        return CompletableFuture.completedFuture(null);
    }

    /** Warms up cache for a specific guardian */
    @Async("taskExecutor")
    public CompletableFuture<Void> warmupGuardianCache(GuardianId guardianId) {
        try {
            logger.debug("Warming up cache for guardian: {}", guardianId);

            // Warm up step-related caches
            warmupStepCaches(guardianId);

            // Warm up energy-related caches
            warmupEnergyCaches(guardianId);

            logger.debug("Cache warmup completed for guardian: {}", guardianId);

        } catch (Exception e) {
            logger.warn("Error warming up cache for guardian: {}", guardianId, e);
        }

        return CompletableFuture.completedFuture(null);
    }

    /** Warms up step-related caches for a guardian */
    private void warmupStepCaches(GuardianId guardianId) {
        try {
            // Preload today's aggregate
            cachedStepAggregationService.preloadTodaysAggregate(guardianId);

            // Preload current week's aggregates
            cachedStepAggregationService.preloadCurrentWeekAggregates(guardianId);

            // Preload last 7 days of history (common query)
            LocalDate today = LocalDate.now();
            LocalDate weekAgo = today.minusDays(7);
            cachedStepAggregationService.getStepHistory(guardianId, weekAgo, today);

            // Preload last 30 days of history (monthly view)
            LocalDate monthAgo = today.minusDays(30);
            cachedStepAggregationService.getStepHistory(guardianId, monthAgo, today);

        } catch (Exception e) {
            logger.warn("Error warming up step caches for guardian: {}", guardianId, e);
        }
    }

    /** Warms up energy-related caches for a guardian */
    private void warmupEnergyCaches(GuardianId guardianId) {
        try {
            // Preload energy balance
            cachedEnergyService.preloadEnergyBalance(guardianId);

            // Preload recent transactions
            cachedEnergyService.preloadRecentTransactions(guardianId);

        } catch (Exception e) {
            logger.warn("Error warming up energy caches for guardian: {}", guardianId, e);
        }
    }

    /** Warms up cache for a new guardian (called when guardian first logs in) */
    public void warmupNewGuardianCache(GuardianId guardianId) {
        logger.info("Warming up cache for new guardian: {}", guardianId);

        // Async warmup to not block the login process
        warmupGuardianCache(guardianId);
    }

    /** Warms up caches for the most popular data Called during application startup */
    public void warmupPopularCaches() {
        logger.info("Warming up popular caches during application startup");

        try {
            // Get list of most active guardians
            List<GuardianId> popularGuardians =
                    getMostActiveGuardianIds(50); // Top 50 active guardians

            for (GuardianId guardianId : popularGuardians) {
                // Warm up only today's data for popular guardians
                cachedStepAggregationService.preloadTodaysAggregate(guardianId);
                cachedEnergyService.getCurrentBalance(guardianId);
            }

            logger.info("Popular caches warmed up for {} guardians", popularGuardians.size());

        } catch (Exception e) {
            logger.error("Error warming up popular caches", e);
        }
    }

    /**
     * Invalidates and rewarms cache for a guardian Useful when data inconsistencies are detected
     */
    public void refreshGuardianCache(GuardianId guardianId) {
        logger.info("Refreshing cache for guardian: {}", guardianId);

        try {
            // Invalidate all caches for the guardian
            cachedStepAggregationService.invalidateAllCachesForGuardian(guardianId);
            cachedEnergyService.invalidateAllEnergyCachesForGuardian(guardianId);

            // Warm up the cache again
            warmupGuardianCache(guardianId);

        } catch (Exception e) {
            logger.error("Error refreshing cache for guardian: {}", guardianId, e);
        }
    }

    /** Gets list of active guardian IDs In a real implementation, this would query the database */
    private List<GuardianId> getActiveGuardianIds() {
        // Placeholder implementation
        // In reality, you would query for guardians who have been active in the last 7 days
        return List.of(GuardianId.of(1L), GuardianId.of(2L), GuardianId.of(3L));
    }

    /**
     * Gets list of most active guardian IDs In a real implementation, this would query the database
     * for top active users
     */
    private List<GuardianId> getMostActiveGuardianIds(int limit) {
        // Placeholder implementation
        // In reality, you would query for guardians with most activity in the last 30 days
        return List.of(
                GuardianId.of(1L),
                GuardianId.of(2L),
                GuardianId.of(3L),
                GuardianId.of(4L),
                GuardianId.of(5L));
    }
}

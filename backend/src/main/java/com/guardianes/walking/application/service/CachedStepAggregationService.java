package com.guardianes.walking.application.service;

import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.walking.domain.model.DailyStepAggregate;
import com.guardianes.walking.domain.repository.StepRepository;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

/**
 * Cached service for step aggregation operations. Provides high-performance access to frequently
 * requested step data.
 */
@Service
public class CachedStepAggregationService {

    private static final Logger logger =
            LoggerFactory.getLogger(CachedStepAggregationService.class);

    private final StepRepository stepRepository;

    public CachedStepAggregationService(StepRepository stepRepository) {
        this.stepRepository = stepRepository;
    }

    /**
     * Gets daily step aggregate with caching. Cache key: guardianId:date TTL: 2 hours (configured
     * in CacheConfig)
     */
    @Cacheable(value = "daily-aggregates", key = "#guardianId.value() + ':' + #date.toString()")
    public DailyStepAggregate getDailyAggregate(GuardianId guardianId, LocalDate date) {
        logger.debug("Cache miss for daily aggregate - Guardian: {}, Date: {}", guardianId, date);

        try {
            return stepRepository
                    .findDailyAggregatesByGuardianIdAndDateRange(guardianId, date, date)
                    .stream()
                    .findFirst()
                    .orElse(DailyStepAggregate.empty(guardianId, date));
        } catch (Exception e) {
            logger.warn(
                    "Failed to retrieve daily aggregate from repository for guardian {} on {}, returning empty",
                    guardianId,
                    date,
                    e);
            return DailyStepAggregate.empty(guardianId, date);
        }
    }

    /**
     * Gets step history for a date range with caching. Cache key: guardianId:fromDate:toDate TTL: 1
     * hour (configured in CacheConfig)
     */
    @Cacheable(
            value = "step-history",
            key = "#guardianId.value() + ':' + #fromDate.toString() + ':' + #toDate.toString()")
    public List<DailyStepAggregate> getStepHistory(
            GuardianId guardianId, LocalDate fromDate, LocalDate toDate) {
        logger.debug(
                "Cache miss for step history - Guardian: {}, From: {}, To: {}",
                guardianId,
                fromDate,
                toDate);

        return stepRepository.findDailyAggregatesByGuardianIdAndDateRange(
                guardianId, fromDate, toDate);
    }

    /**
     * Gets current week's step aggregates (Sunday to Saturday). Useful for weekly progress
     * tracking.
     */
    @Cacheable(
            value = "step-history",
            key = "#guardianId.value() + ':week:' + #weekStartDate.toString()")
    public List<DailyStepAggregate> getCurrentWeekAggregates(
            GuardianId guardianId, LocalDate weekStartDate) {
        logger.debug(
                "Cache miss for current week aggregates - Guardian: {}, Week start: {}",
                guardianId,
                weekStartDate);

        LocalDate weekEndDate = weekStartDate.plusDays(6);
        return stepRepository.findDailyAggregatesByGuardianIdAndDateRange(
                guardianId, weekStartDate, weekEndDate);
    }

    /**
     * Invalidates daily aggregate cache for a specific guardian and date. Called when new steps are
     * submitted.
     */
    @CacheEvict(value = "daily-aggregates", key = "#guardianId.value() + ':' + #date.toString()")
    public void invalidateDailyAggregate(GuardianId guardianId, LocalDate date) {
        logger.debug(
                "Invalidating daily aggregate cache - Guardian: {}, Date: {}", guardianId, date);
    }

    /** Invalidates step history cache for a guardian. Called when step data is modified. */
    @CacheEvict(value = "step-history", allEntries = true, condition = "#guardianId != null")
    public void invalidateStepHistory(GuardianId guardianId) {
        logger.debug("Invalidating step history cache for guardian: {}", guardianId);
    }

    /**
     * Invalidates all cache entries for a specific guardian. Used when guardian data needs to be
     * completely refreshed.
     */
    @Caching(
            evict = {
                @CacheEvict(value = "daily-aggregates", allEntries = true),
                @CacheEvict(value = "step-history", allEntries = true)
            })
    public void invalidateAllCachesForGuardian(GuardianId guardianId) {
        logger.info("Invalidating all step-related caches for guardian: {}", guardianId);
    }

    /**
     * Preloads cache with current day's aggregate. Useful for warming up cache during off-peak
     * hours.
     */
    public void preloadTodaysAggregate(GuardianId guardianId) {
        LocalDate today = LocalDate.now();
        logger.debug("Preloading today's aggregate for guardian: {}", guardianId);
        getDailyAggregate(guardianId, today);
    }

    /**
     * Preloads cache with current week's aggregates. Useful for warming up cache for weekly
     * reports.
     */
    public void preloadCurrentWeekAggregates(GuardianId guardianId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart =
                today.minusDays(today.getDayOfWeek().getValue() % 7); // Start of week (Sunday)
        logger.debug("Preloading current week aggregates for guardian: {}", guardianId);
        getCurrentWeekAggregates(guardianId, weekStart);
    }
}

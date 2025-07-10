package com.guardianes.walking.application.service;

import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.walking.domain.model.Energy;
import com.guardianes.walking.domain.model.EnergyTransaction;
import com.guardianes.walking.domain.repository.EnergyRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

/**
 * Cached service for energy management operations. Provides high-performance access to energy
 * balances and transaction history.
 */
@Service
public class CachedEnergyService {

    private static final Logger logger = LoggerFactory.getLogger(CachedEnergyService.class);

    private final EnergyRepository energyRepository;

    public CachedEnergyService(EnergyRepository energyRepository) {
        this.energyRepository = energyRepository;
    }

    /**
     * Gets current energy balance with caching. Cache key: guardianId TTL: 15 minutes (configured
     * in CacheConfig)
     */
    @Cacheable(value = "energy-balance", key = "#guardianId.value()")
    public Energy getCurrentBalance(GuardianId guardianId) {
        logger.debug("Cache miss for energy balance - Guardian: {}", guardianId);

        try {
            // Use existing repository method - adapt to current interface
            return energyRepository.getEnergyBalance(guardianId);
        } catch (Exception e) {
            logger.warn(
                    "Failed to retrieve energy balance for guardian {}, returning zero",
                    guardianId,
                    e);
            return Energy.zero();
        }
    }

    /**
     * Gets recent energy transactions with caching. Cache key: guardianId:limit TTL: 45 minutes
     * (configured in CacheConfig)
     */
    @Cacheable(value = "energy-transactions", key = "#guardianId.value() + ':' + #limit")
    public List<EnergyTransaction> getRecentTransactions(GuardianId guardianId, int limit) {
        logger.debug(
                "Cache miss for recent transactions - Guardian: {}, Limit: {}", guardianId, limit);

        // Use existing repository method - adapt to current interface
        return energyRepository.getRecentTransactions(guardianId, limit);
    }

    /** Gets energy transactions by type with caching. Useful for analytics and reporting. */
    @Cacheable(
            value = "energy-transactions",
            key = "#guardianId.value() + ':type:' + #transactionType + ':' + #limit")
    public List<EnergyTransaction> getTransactionsByType(
            GuardianId guardianId, String transactionType, int limit) {
        logger.debug(
                "Cache miss for transactions by type - Guardian: {}, Type: {}, Limit: {}",
                guardianId,
                transactionType,
                limit);

        // Use existing repository method - adapt to current interface
        return energyRepository.findTransactionsByGuardianId(guardianId).stream()
                .filter(transaction -> transactionType.equals(transaction.getType().toString()))
                .limit(limit)
                .toList();
    }

    /** Gets total energy earned today with caching. Useful for daily progress tracking. */
    @Cacheable(value = "energy-balance", key = "#guardianId.value() + ':daily-earned'")
    public Energy getTotalEarnedToday(GuardianId guardianId) {
        logger.debug("Cache miss for daily earned energy - Guardian: {}", guardianId);

        try {
            // Calculate from transactions - adapt to current interface
            return energyRepository.findTransactionsByGuardianId(guardianId).stream()
                    .filter(
                            transaction ->
                                    transaction
                                            .getOccurredAt()
                                            .value()
                                            .toLocalDate()
                                            .equals(java.time.LocalDate.now()))
                    .filter(transaction -> "EARNED".equals(transaction.getType().toString()))
                    .map(transaction -> transaction.getAmount())
                    .reduce(Energy.zero(), Energy::add);
        } catch (Exception e) {
            logger.warn(
                    "Failed to retrieve daily earned energy for guardian {}, returning zero",
                    guardianId,
                    e);
            return Energy.zero();
        }
    }

    /** Gets total energy spent today with caching. Useful for daily spending tracking. */
    @Cacheable(value = "energy-balance", key = "#guardianId.value() + ':daily-spent'")
    public Energy getTotalSpentToday(GuardianId guardianId) {
        logger.debug("Cache miss for daily spent energy - Guardian: {}", guardianId);

        try {
            // Calculate from transactions - adapt to current interface
            return energyRepository.findTransactionsByGuardianId(guardianId).stream()
                    .filter(
                            transaction ->
                                    transaction
                                            .getOccurredAt()
                                            .value()
                                            .toLocalDate()
                                            .equals(java.time.LocalDate.now()))
                    .filter(transaction -> "SPENT".equals(transaction.getType().toString()))
                    .map(transaction -> transaction.getAmount())
                    .reduce(Energy.zero(), Energy::add);
        } catch (Exception e) {
            logger.warn(
                    "Failed to retrieve daily spent energy for guardian {}, returning zero",
                    guardianId,
                    e);
            return Energy.zero();
        }
    }

    /**
     * Invalidates energy balance cache for a specific guardian. Called when energy transactions
     * occur.
     */
    @Caching(
            evict = {
                @CacheEvict(value = "energy-balance", key = "#guardianId.value()"),
                @CacheEvict(
                        value = "energy-balance",
                        key = "#guardianId.value() + ':daily-earned'"),
                @CacheEvict(value = "energy-balance", key = "#guardianId.value() + ':daily-spent'")
            })
    public void invalidateEnergyBalance(GuardianId guardianId) {
        logger.debug("Invalidating energy balance cache for guardian: {}", guardianId);
    }

    /**
     * Invalidates energy transaction cache for a specific guardian. Called when new transactions
     * are added.
     */
    @CacheEvict(value = "energy-transactions", allEntries = true, condition = "#guardianId != null")
    public void invalidateTransactionHistory(GuardianId guardianId) {
        logger.debug("Invalidating transaction history cache for guardian: {}", guardianId);
    }

    /**
     * Invalidates all energy-related cache entries for a specific guardian. Used when guardian
     * energy data needs to be completely refreshed.
     */
    @Caching(
            evict = {
                @CacheEvict(value = "energy-balance", allEntries = true),
                @CacheEvict(value = "energy-transactions", allEntries = true)
            })
    public void invalidateAllEnergyCachesForGuardian(GuardianId guardianId) {
        logger.info("Invalidating all energy-related caches for guardian: {}", guardianId);
    }

    /**
     * Preloads cache with current energy balance. Useful for warming up cache during off-peak
     * hours.
     */
    public void preloadEnergyBalance(GuardianId guardianId) {
        logger.debug("Preloading energy balance for guardian: {}", guardianId);
        getCurrentBalance(guardianId);
        getTotalEarnedToday(guardianId);
        getTotalSpentToday(guardianId);
    }

    /**
     * Preloads cache with recent transaction history. Useful for warming up cache for transaction
     * views.
     */
    public void preloadRecentTransactions(GuardianId guardianId) {
        logger.debug("Preloading recent transactions for guardian: {}", guardianId);
        getRecentTransactions(guardianId, 10); // Load last 10 transactions
        getRecentTransactions(guardianId, 50); // Load last 50 transactions for detailed view
    }

    /**
     * Checks if guardian has sufficient energy for a transaction. Uses cached balance for fast
     * validation.
     */
    public boolean hasSufficientEnergy(GuardianId guardianId, Energy requiredEnergy) {
        Energy currentBalance = getCurrentBalance(guardianId);
        boolean sufficient = currentBalance.amount() >= requiredEnergy.amount();

        logger.debug(
                "Energy sufficiency check - Guardian: {}, Required: {}, Current: {}, Sufficient: {}",
                guardianId,
                requiredEnergy,
                currentBalance,
                sufficient);

        return sufficient;
    }
}

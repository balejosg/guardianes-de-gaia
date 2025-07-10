package com.guardianes.walking.domain.service;

import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.walking.domain.model.Energy;
import com.guardianes.walking.domain.model.EnergySource;
import com.guardianes.walking.domain.model.EnergyTransaction;
import com.guardianes.walking.domain.model.InsufficientEnergyException;
import com.guardianes.walking.domain.repository.EnergyRepository;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Domain service that encapsulates complex energy management business logic. Handles energy balance
 * calculations and spending validation.
 */
@Service
public class EnergyManagementDomainService {
    private static final Logger logger =
            LoggerFactory.getLogger(EnergyManagementDomainService.class);

    private final EnergyRepository energyRepository;

    public EnergyManagementDomainService(EnergyRepository energyRepository) {
        this.energyRepository = energyRepository;
    }

    /**
     * Calculates the current energy balance for a guardian. Business rule: Balance = Sum of all
     * earned energy - Sum of all spent energy, minimum 0.
     */
    public Energy calculateCurrentBalance(GuardianId guardianId) {
        Objects.requireNonNull(guardianId, "Guardian ID cannot be null");

        logger.debug("Calculating energy balance for guardian {}", guardianId);

        List<EnergyTransaction> allTransactions =
                energyRepository.findTransactionsByGuardianId(guardianId);

        int totalEarned =
                allTransactions.stream()
                        .filter(EnergyTransaction::isEarning)
                        .mapToInt(transaction -> transaction.getAmount().amount())
                        .sum();

        int totalSpent =
                allTransactions.stream()
                        .filter(EnergyTransaction::isSpending)
                        .mapToInt(transaction -> transaction.getAmount().amount())
                        .sum();

        int totalBalance = totalEarned - totalSpent;
        Energy balance = Energy.of(Math.max(0, totalBalance));

        logger.debug("Calculated energy balance for guardian {}: {}", guardianId, balance);
        return balance;
    }

    /**
     * Processes an energy spending operation with full validation. Ensures sufficient balance and
     * creates the spending transaction.
     */
    public EnergyTransaction spendEnergy(
            GuardianId guardianId, Energy amount, EnergySource source) {
        Objects.requireNonNull(guardianId, "Guardian ID cannot be null");
        Objects.requireNonNull(amount, "Energy amount cannot be null");
        Objects.requireNonNull(source, "Energy source cannot be null");

        logger.debug(
                "Processing energy spending for guardian {}: {} energy for {}",
                guardianId,
                amount,
                source);

        // 1. Calculate current balance
        Energy currentBalance = calculateCurrentBalance(guardianId);

        // 2. Validate sufficient energy
        if (!currentBalance.isSufficientFor(amount)) {
            logger.warn(
                    "Insufficient energy for guardian {}: requested {}, available {}",
                    guardianId,
                    amount,
                    currentBalance);
            throw new InsufficientEnergyException(
                    String.format(
                            "Insufficient energy: requested %d, available %d",
                            amount.amount(), currentBalance.amount()));
        }

        // 3. Create and save spending transaction
        EnergyTransaction spendingTransaction = EnergyTransaction.spent(guardianId, amount, source);
        EnergyTransaction savedTransaction = energyRepository.saveTransaction(spendingTransaction);

        logger.info(
                "Successfully processed energy spending for guardian {}: {} energy spent for {}, {} remaining",
                guardianId,
                amount,
                source,
                currentBalance.subtract(amount));

        return savedTransaction;
    }

    /** Checks if a guardian has sufficient energy for a specific operation. */
    public boolean hasSufficientEnergy(GuardianId guardianId, Energy requiredAmount) {
        Energy currentBalance = calculateCurrentBalance(guardianId);
        return currentBalance.isSufficientFor(requiredAmount);
    }

    /**
     * Gets recent energy transactions for a guardian. Useful for displaying transaction history.
     */
    public List<EnergyTransaction> getRecentTransactions(GuardianId guardianId, int limit) {
        Objects.requireNonNull(guardianId, "Guardian ID cannot be null");

        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }

        return energyRepository.getRecentTransactions(guardianId, limit);
    }

    /** Calculates energy statistics for a guardian. */
    public EnergyStatistics calculateEnergyStatistics(GuardianId guardianId) {
        List<EnergyTransaction> allTransactions =
                energyRepository.findTransactionsByGuardianId(guardianId);

        Energy totalEarned =
                Energy.of(
                        allTransactions.stream()
                                .filter(EnergyTransaction::isEarning)
                                .mapToInt(t -> t.getAmount().amount())
                                .sum());

        Energy totalSpent =
                Energy.of(
                        allTransactions.stream()
                                .filter(EnergyTransaction::isSpending)
                                .mapToInt(t -> t.getAmount().amount())
                                .sum());

        Energy currentBalance = calculateCurrentBalance(guardianId);

        long totalTransactions = allTransactions.size();
        long stepTransactions =
                allTransactions.stream().filter(EnergyTransaction::isFromSteps).count();

        return new EnergyStatistics(
                totalEarned, totalSpent, currentBalance, totalTransactions, stepTransactions);
    }

    /** Value object representing energy statistics for a guardian. */
    public record EnergyStatistics(
            Energy totalEarned,
            Energy totalSpent,
            Energy currentBalance,
            long totalTransactions,
            long stepBasedTransactions) {
        public double getStepTransactionPercentage() {
            return totalTransactions > 0
                    ? (double) stepBasedTransactions / totalTransactions * 100
                    : 0;
        }
    }
}

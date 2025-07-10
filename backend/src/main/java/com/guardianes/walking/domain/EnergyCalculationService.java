package com.guardianes.walking.domain;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class EnergyCalculationService {
    private final EnergyRepository energyRepository;
    private final StepAggregationService stepAggregationService;

    public EnergyCalculationService(EnergyRepository energyRepository, StepAggregationService stepAggregationService) {
        this.energyRepository = energyRepository;
        this.stepAggregationService = stepAggregationService;
    }

    public int calculateEnergyFromSteps(int steps) {
        return steps / 10;
    }

    @Transactional
    public EnergyTransaction convertDailyStepsToEnergy(Long guardianId, LocalDate date) {
        DailyStepAggregate stepAggregate = stepAggregationService.aggregateDailySteps(guardianId, date);
        int energy = calculateEnergyFromSteps(stepAggregate.getTotalSteps());
        EnergyTransaction transaction = new EnergyTransaction(
            guardianId,
            EnergyTransactionType.EARNED,
            energy,
            "DAILY_STEPS",
            LocalDateTime.now()
        );
        return energyRepository.saveTransaction(transaction);
    }

    @Transactional(readOnly = true)
    public int getCurrentEnergyBalance(Long guardianId) {
        List<EnergyTransaction> transactions = energyRepository.findTransactionsByGuardianId(guardianId);
        int balance = transactions.stream()
            .mapToInt(transaction -> {
                if (transaction.getType() == EnergyTransactionType.EARNED) {
                    return transaction.getAmount();
                } else {
                    return -transaction.getAmount();
                }
            })
            .sum();
        return Math.max(0, balance);
    }

    @Transactional
    public EnergyTransaction spendEnergy(Long guardianId, int energyToSpend, String source) {
        int currentBalance = getCurrentEnergyBalance(guardianId);
        if (currentBalance < energyToSpend) {
            throw new InsufficientEnergyException("Not enough energy available");
        }
        EnergyTransaction transaction = new EnergyTransaction(
            guardianId,
            EnergyTransactionType.SPENT,
            energyToSpend,
            source,
            LocalDateTime.now()
        );
        return energyRepository.saveTransaction(transaction);
    }

    @Transactional(readOnly = true)
    public List<EnergyTransaction> getEnergyHistory(Long guardianId, LocalDate fromDate, LocalDate toDate) {
        return energyRepository.findTransactionsByGuardianIdAndDateRange(guardianId, fromDate, toDate);
    }
}
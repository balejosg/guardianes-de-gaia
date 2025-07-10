package com.guardianes.walking.domain.service;

import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.walking.domain.model.DailyStepAggregate;
import com.guardianes.walking.domain.model.Energy;
import com.guardianes.walking.domain.model.EnergySource;
import com.guardianes.walking.domain.model.EnergySpendingSource;
import com.guardianes.walking.domain.model.EnergyTransaction;
import com.guardianes.walking.domain.model.EnergyTransactionType;
import com.guardianes.walking.domain.model.InsufficientEnergyException;
import com.guardianes.walking.domain.model.StepCount;
import com.guardianes.walking.domain.repository.EnergyRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EnergyCalculationService {
    private final EnergyRepository energyRepository;
    private final StepAggregationService stepAggregationService;

    public EnergyCalculationService(
            EnergyRepository energyRepository, StepAggregationService stepAggregationService) {
        this.energyRepository = energyRepository;
        this.stepAggregationService = stepAggregationService;
    }

    public Energy calculateEnergyFromSteps(StepCount steps) {
        return Energy.of(steps.value() / 10);
    }

    @Transactional
    public EnergyTransaction convertDailyStepsToEnergy(GuardianId guardianId, LocalDate date) {
        DailyStepAggregate stepAggregate =
                stepAggregationService.aggregateDailySteps(guardianId, date);
        Energy energy = calculateEnergyFromSteps(stepAggregate.getTotalSteps());
        EnergyTransaction transaction =
                EnergyTransaction.earned(guardianId, energy, EnergySource.steps());
        return energyRepository.saveTransaction(transaction);
    }

    @Transactional(readOnly = true)
    public Energy getCurrentEnergyBalance(GuardianId guardianId) {
        List<EnergyTransaction> transactions =
                energyRepository.findTransactionsByGuardianId(guardianId);
        Energy balance =
                transactions.stream()
                        .map(
                                transaction -> {
                                    if (transaction.getType() == EnergyTransactionType.EARNED) {
                                        return transaction.getAmount();
                                    } else {
                                        return Energy.of(transaction.getAmount().negateValue());
                                    }
                                })
                        .reduce(Energy.zero(), Energy::add);
        return Energy.of(Math.max(0, balance.amount()));
    }

    @Transactional
    public EnergyTransaction spendEnergy(
            GuardianId guardianId, Energy energyToSpend, EnergySpendingSource source) {
        Energy currentBalance = getCurrentEnergyBalance(guardianId);
        if (currentBalance.amount() < energyToSpend.amount()) {
            throw new InsufficientEnergyException("Not enough energy available");
        }
        EnergyTransaction transaction =
                EnergyTransaction.spent(guardianId, energyToSpend, EnergySource.of(source.name()));
        return energyRepository.saveTransaction(transaction);
    }

    @Transactional(readOnly = true)
    public List<EnergyTransaction> getEnergyHistory(
            GuardianId guardianId, LocalDate fromDate, LocalDate toDate) {
        return energyRepository.findTransactionsByGuardianIdAndDateRange(
                guardianId, fromDate, toDate);
    }
}

package com.guardianes.walking.infrastructure.repository;

import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.walking.domain.model.Energy;
import com.guardianes.walking.domain.model.EnergyTransaction;
import com.guardianes.walking.domain.model.EnergyTransactionType;
import com.guardianes.walking.domain.repository.EnergyRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryEnergyRepository implements EnergyRepository {

    private final Map<String, EnergyTransaction> transactions = new ConcurrentHashMap<>();
    private long transactionIdCounter = 1;

    @Override
    public EnergyTransaction saveTransaction(EnergyTransaction transaction) {
        String key = String.valueOf(transactionIdCounter++);
        transactions.put(key, transaction);
        return transaction;
    }

    @Override
    public List<EnergyTransaction> findTransactionsByGuardianId(GuardianId guardianId) {
        return transactions.values().stream()
                .filter(transaction -> transaction.getGuardianId().equals(guardianId))
                .collect(Collectors.toList());
    }

    @Override
    public List<EnergyTransaction> findTransactionsByGuardianIdAndDateRange(
            GuardianId guardianId, LocalDate fromDate, LocalDate toDate) {
        return transactions.values().stream()
                .filter(transaction -> transaction.getGuardianId().equals(guardianId))
                .filter(
                        transaction -> {
                            LocalDate transactionDate =
                                    transaction.getOccurredAt().value().toLocalDate();
                            return !transactionDate.isBefore(fromDate)
                                    && !transactionDate.isAfter(toDate);
                        })
                .collect(Collectors.toList());
    }

    @Override
    public Energy getEnergyBalance(GuardianId guardianId) {
        return Energy.of(
                findTransactionsByGuardianId(guardianId).stream()
                        .mapToInt(
                                transaction -> {
                                    if (transaction.getType() == EnergyTransactionType.EARNED) {
                                        return transaction.getAmount().amount();
                                    } else {
                                        return -transaction.getAmount().amount();
                                    }
                                })
                        .sum());
    }

    @Override
    public List<EnergyTransaction> getRecentTransactions(GuardianId guardianId, int limit) {
        return findTransactionsByGuardianId(guardianId).stream()
                .sorted(
                        (t1, t2) ->
                                t2.getOccurredAt().value().compareTo(t1.getOccurredAt().value()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}

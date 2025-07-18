package com.guardianes.walking.infrastructure.repository;

import com.guardianes.walking.domain.EnergyRepository;
import com.guardianes.walking.domain.EnergyTransaction;
import com.guardianes.walking.domain.EnergyTransactionType;
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
  public List<EnergyTransaction> findTransactionsByGuardianId(Long guardianId) {
    return transactions.values().stream()
        .filter(transaction -> transaction.getGuardianId().equals(guardianId))
        .collect(Collectors.toList());
  }

  @Override
  public List<EnergyTransaction> findTransactionsByGuardianIdAndDateRange(
      Long guardianId, LocalDate fromDate, LocalDate toDate) {
    return transactions.values().stream()
        .filter(transaction -> transaction.getGuardianId().equals(guardianId))
        .filter(
            transaction -> {
              LocalDate transactionDate = transaction.getTimestamp().toLocalDate();
              return !transactionDate.isBefore(fromDate) && !transactionDate.isAfter(toDate);
            })
        .collect(Collectors.toList());
  }

  @Override
  public int getEnergyBalance(Long guardianId) {
    return findTransactionsByGuardianId(guardianId).stream()
        .mapToInt(
            transaction -> {
              if (transaction.getType() == EnergyTransactionType.EARNED) {
                return transaction.getAmount();
              } else {
                return -transaction.getAmount();
              }
            })
        .sum();
  }

  @Override
  public List<EnergyTransaction> getRecentTransactions(Long guardianId, int limit) {
    return findTransactionsByGuardianId(guardianId).stream()
        .sorted((t1, t2) -> t2.getTimestamp().compareTo(t1.getTimestamp()))
        .limit(limit)
        .collect(Collectors.toList());
  }
}

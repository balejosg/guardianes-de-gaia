package com.guardianes.walking.domain;

import java.time.LocalDate;
import java.util.List;

public interface EnergyRepository {
  EnergyTransaction saveTransaction(EnergyTransaction transaction);

  List<EnergyTransaction> findTransactionsByGuardianId(Long guardianId);

  List<EnergyTransaction> findTransactionsByGuardianIdAndDateRange(
      Long guardianId, LocalDate fromDate, LocalDate toDate);

  int getEnergyBalance(Long guardianId);

  List<EnergyTransaction> getRecentTransactions(Long guardianId, int limit);
}

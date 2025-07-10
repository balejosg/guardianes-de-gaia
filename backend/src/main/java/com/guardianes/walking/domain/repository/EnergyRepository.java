package com.guardianes.walking.domain.repository;

import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.walking.domain.model.Energy;
import com.guardianes.walking.domain.model.EnergyTransaction;
import java.time.LocalDate;
import java.util.List;

public interface EnergyRepository {
    EnergyTransaction saveTransaction(EnergyTransaction transaction);

    List<EnergyTransaction> findTransactionsByGuardianId(GuardianId guardianId);

    List<EnergyTransaction> findTransactionsByGuardianIdAndDateRange(
            GuardianId guardianId, LocalDate fromDate, LocalDate toDate);

    Energy getEnergyBalance(GuardianId guardianId);

    List<EnergyTransaction> getRecentTransactions(GuardianId guardianId, int limit);
}

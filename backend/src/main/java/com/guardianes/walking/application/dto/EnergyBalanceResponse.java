package com.guardianes.walking.application.dto;

import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.walking.domain.model.Energy;
import com.guardianes.walking.domain.model.EnergyTransaction;
import java.util.List;

public record EnergyBalanceResponse(
        GuardianId guardianId, Energy currentBalance, List<EnergyTransaction> transactionSummary) {}

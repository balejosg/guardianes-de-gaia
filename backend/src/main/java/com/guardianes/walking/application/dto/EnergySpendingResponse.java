package com.guardianes.walking.application.dto;

import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.walking.domain.model.Energy;
import com.guardianes.walking.domain.model.EnergySpendingSource;

public record EnergySpendingResponse(
        GuardianId guardianId,
        Energy newBalance,
        Energy amountSpent,
        EnergySpendingSource source,
        String message) {}

package com.guardianes.walking.application.dto;

import com.guardianes.walking.domain.EnergySpendingSource;

public record EnergySpendingResponse(
    Long guardianId,
    Integer newBalance,
    Integer amountSpent,
    EnergySpendingSource source,
    String message
) {}
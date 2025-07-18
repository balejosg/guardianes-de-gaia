package com.guardianes.walking.application.dto;

import com.guardianes.walking.domain.EnergyTransaction;
import java.util.List;

public record EnergyBalanceResponse(
    Long guardianId, Integer currentBalance, List<EnergyTransaction> transactionSummary) {}

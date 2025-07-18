package com.guardianes.walking.application.dto;

import com.guardianes.walking.domain.EnergySpendingSource;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to spend energy for battles, challenges, or shop purchases")
public record EnergySpendingRequest(
    @Schema(description = "Amount of energy to spend", example = "100", minimum = "1")
        @NotNull(message = "Amount is required")
        @Min(value = 1, message = "Amount must be positive")
        Integer amount,
    @Schema(
            description = "Source of energy spending",
            example = "BATTLE",
            allowableValues = {"BATTLE", "CHALLENGE", "SHOP"})
        @NotNull(message = "Source is required")
        EnergySpendingSource source) {}

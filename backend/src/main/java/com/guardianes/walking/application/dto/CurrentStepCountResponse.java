package com.guardianes.walking.application.dto;

import java.time.LocalDate;

public record CurrentStepCountResponse(
    Long guardianId,
    Integer currentSteps,
    Integer availableEnergy,
    LocalDate date
) {}
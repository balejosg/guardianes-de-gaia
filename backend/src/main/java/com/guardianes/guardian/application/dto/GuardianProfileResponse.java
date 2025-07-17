package com.guardianes.guardian.application.dto;

import com.guardianes.guardian.domain.model.GuardianLevel;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record GuardianProfileResponse(
    Long id,
    String username,
    String email,
    String name,
    LocalDate birthDate,
    int age,
    GuardianLevel level,
    int experiencePoints,
    int experienceToNextLevel,
    int totalSteps,
    int totalEnergyGenerated,
    LocalDateTime createdAt,
    LocalDateTime lastActiveAt,
    boolean isChild
) {}
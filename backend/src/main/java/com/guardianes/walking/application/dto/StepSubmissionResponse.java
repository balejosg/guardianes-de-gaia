package com.guardianes.walking.application.dto;

public record StepSubmissionResponse(
    Long guardianId, Integer totalDailySteps, Integer energyEarned, String message) {}

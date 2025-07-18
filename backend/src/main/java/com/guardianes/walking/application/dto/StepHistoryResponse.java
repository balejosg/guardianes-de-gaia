package com.guardianes.walking.application.dto;

import com.guardianes.walking.domain.DailyStepAggregate;
import java.util.List;

public record StepHistoryResponse(Long guardianId, List<DailyStepAggregate> dailySteps) {}

package com.guardianes.walking.domain.service;

import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.shared.infrastructure.metrics.BusinessMetricsService;
import com.guardianes.walking.domain.model.StepCount;
import com.guardianes.walking.domain.model.StepRecord;
import com.guardianes.walking.domain.model.StepValidationResult;
import com.guardianes.walking.domain.repository.StepRepository;
import io.micrometer.core.instrument.Timer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StepValidationService {
    private static final Logger logger = LoggerFactory.getLogger(StepValidationService.class);

    private final StepRepository stepRepository;
    private final AnomalyDetectionService anomalyDetectionService;
    private final BusinessMetricsService metricsService;
    private static final int MAX_DAILY_STEPS = 50000;
    private static final int MAX_SUBMISSIONS_PER_HOUR = 100;

    public StepValidationService(
            StepRepository stepRepository,
            AnomalyDetectionService anomalyDetectionService,
            BusinessMetricsService metricsService) {
        this.stepRepository = stepRepository;
        this.anomalyDetectionService = anomalyDetectionService;
        this.metricsService = metricsService;
    }

    public StepValidationResult validateStepCount(
            GuardianId guardianId, StepCount stepCount, LocalDateTime timestamp) {
        Timer.Sample validationTimer = metricsService.startStepValidation();
        logger.debug(
                "Validating step count for guardian {}: {} steps at {}",
                guardianId,
                stepCount,
                timestamp);

        try {
            if (stepCount.value() < 0) {
                logger.warn(
                        "Invalid step count for guardian {}: negative value {}",
                        guardianId,
                        stepCount);
                metricsService.recordStepValidationFailure(guardianId.value(), "negative_value");
                return StepValidationResult.invalid("Step count cannot be negative");
            }

            if (stepCount.value() > MAX_DAILY_STEPS) {
                logger.warn(
                        "Step count exceeds daily maximum for guardian {}: {} > {}",
                        guardianId,
                        stepCount,
                        MAX_DAILY_STEPS);
                metricsService.recordStepValidationFailure(
                        guardianId.value(), "exceeds_daily_maximum");
                return StepValidationResult.invalid("Step count exceeds daily maximum (50000)");
            }

            List<StepRecord> todaySteps =
                    stepRepository.findByGuardianIdAndDate(guardianId, timestamp.toLocalDate());
            int currentDailyTotal =
                    todaySteps.stream()
                            .mapToInt(stepRecord -> stepRecord.getStepCount().value())
                            .sum();
            logger.debug(
                    "Current daily total for guardian {}: {} steps", guardianId, currentDailyTotal);

            if (currentDailyTotal + stepCount.value() > MAX_DAILY_STEPS) {
                logger.warn(
                        "Daily step count would exceed maximum for guardian {}: {} + {} > {}",
                        guardianId,
                        currentDailyTotal,
                        stepCount,
                        MAX_DAILY_STEPS);
                metricsService.recordStepValidationFailure(
                        guardianId.value(), "daily_total_exceeded");
                return StepValidationResult.invalid(
                        "Daily step count would exceed maximum allowed (50000)");
            }

            if (anomalyDetectionService.isAnomalous(guardianId, stepCount, timestamp)) {
                logger.warn(
                        "Anomalous step count detected for guardian {}: {} steps",
                        guardianId,
                        stepCount);
                metricsService.recordAnomalousSteps(
                        guardianId.value(), stepCount.value(), "anomaly_detection_triggered");
                return StepValidationResult.invalid(
                        "Step count appears anomalous and requires verification");
            }

            logger.debug(
                    "Step count validation successful for guardian {}: {} steps",
                    guardianId,
                    stepCount);
            metricsService.recordStepSubmission(guardianId.value(), stepCount.value());
            return StepValidationResult.valid();
        } finally {
            metricsService.endStepValidation(validationTimer);
        }
    }

    public boolean isReasonableIncrement(
            GuardianId guardianId,
            StepCount previousSteps,
            StepCount currentSteps,
            LocalDateTime previousTimestamp,
            LocalDateTime currentTimestamp) {
        int stepIncrement = currentSteps.value() - previousSteps.value();
        long minutesElapsed = ChronoUnit.MINUTES.between(previousTimestamp, currentTimestamp);

        if (minutesElapsed <= 0) return false;

        // Allow up to 200 steps per minute as reasonable
        int maxReasonableSteps = (int) (minutesElapsed * 200);
        return stepIncrement <= maxReasonableSteps;
    }

    public boolean hasStepCountSpike(GuardianId guardianId, LocalDate date) {
        List<StepRecord> recentSteps =
                stepRepository.findByGuardianIdAndDateRange(guardianId, date.minusDays(7), date);

        if (recentSteps.size() < 2) return false;

        // Calculate average of previous days
        double averageSteps =
                recentSteps.stream()
                        .filter(step -> !step.getRecordedAt().value().toLocalDate().equals(date))
                        .mapToInt(step -> step.getStepCount().value())
                        .average()
                        .orElse(0.0);

        // Check if today's steps are more than 10x the average
        int todaySteps =
                recentSteps.stream()
                        .filter(step -> step.getRecordedAt().value().toLocalDate().equals(date))
                        .mapToInt(step -> step.getStepCount().value())
                        .sum();

        return todaySteps > averageSteps * 10;
    }

    public boolean isWithinSubmissionRateLimit(GuardianId guardianId, LocalDateTime timestamp) {
        int submissionsInLastHour =
                stepRepository.countSubmissionsInLastHour(guardianId, timestamp);
        boolean withinLimit = submissionsInLastHour < MAX_SUBMISSIONS_PER_HOUR;

        if (!withinLimit) {
            logger.warn(
                    "Rate limit exceeded for guardian {}: {} submissions in last hour (limit: {})",
                    guardianId,
                    submissionsInLastHour,
                    MAX_SUBMISSIONS_PER_HOUR);
            metricsService.recordRateLimitExceeded(guardianId.value(), "step_submission");
        } else {
            logger.debug(
                    "Guardian {} within rate limit: {} submissions in last hour",
                    guardianId,
                    submissionsInLastHour);
        }

        return withinLimit;
    }

    public boolean hasValidDataIntegrity(StepRecord stepRecord) {
        return stepRecord.getGuardianId() != null
                && stepRecord.getStepCount().value() >= 0
                && stepRecord.getRecordedAt() != null;
    }
}

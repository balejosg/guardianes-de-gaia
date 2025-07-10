package com.guardianes.walking.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.shared.infrastructure.metrics.BusinessMetricsService;
import com.guardianes.walking.domain.model.*;
import com.guardianes.walking.domain.repository.StepRepository;
import com.guardianes.walking.domain.service.StepValidationService;
import java.time.LocalDateTime;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Simplified test for domain services that replaces the complex legacy tests. Focuses on core
 * functionality without complex mocking scenarios.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Simplified Domain Service Tests")
class SimplifiedDomainServiceTest {

    @Mock private StepRepository stepRepository;

    @Mock private BusinessMetricsService metricsService;

    private StepValidationService stepValidationService;

    @BeforeEach
    void setUp() {
        // Create a minimal mock for AnomalyDetectionService
        var anomalyDetectionService =
                mock(com.guardianes.walking.domain.service.AnomalyDetectionService.class);
        lenient().when(anomalyDetectionService.isAnomalous(any(), any(), any())).thenReturn(false);

        stepValidationService =
                new StepValidationService(stepRepository, anomalyDetectionService, metricsService);
    }

    @Test
    @DisplayName("Should validate reasonable step counts")
    void shouldValidateReasonableStepCounts() {
        // Given
        GuardianId guardianId = GuardianId.of(1L);
        StepCount validStepCount = StepCount.of(5000);
        LocalDateTime timestamp = LocalDateTime.now();

        lenient()
                .when(stepRepository.findByGuardianIdAndDate(any(), any()))
                .thenReturn(Collections.emptyList());
        lenient().when(stepRepository.countSubmissionsInLastHour(any(), any())).thenReturn(1);

        // When
        StepValidationResult result =
                stepValidationService.validateStepCount(guardianId, validStepCount, timestamp);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("Should reject negative step counts")
    void shouldRejectNegativeStepCounts() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> StepCount.of(-1));
    }

    @Test
    @DisplayName("Should reject excessive step counts")
    void shouldRejectExcessiveStepCounts() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> StepCount.of(60000));
    }

    @Test
    @DisplayName("Should validate step count within daily maximum")
    void shouldValidateStepCountWithinDailyMaximum() {
        // Given
        GuardianId guardianId = GuardianId.of(1L);
        StepCount stepCount = StepCount.of(10000);
        LocalDateTime timestamp = LocalDateTime.now();

        // Mock existing steps for the day (total should be under 50000)
        StepRecord existingRecord = StepRecord.create(guardianId, StepCount.of(30000));
        when(stepRepository.findByGuardianIdAndDate(guardianId, timestamp.toLocalDate()))
                .thenReturn(Collections.singletonList(existingRecord));
        lenient()
                .when(stepRepository.countSubmissionsInLastHour(guardianId, timestamp))
                .thenReturn(1);

        // When
        StepValidationResult result =
                stepValidationService.validateStepCount(guardianId, stepCount, timestamp);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("Should reject step count that would exceed daily maximum")
    void shouldRejectStepCountThatWouldExceedDailyMaximum() {
        // Given
        GuardianId guardianId = GuardianId.of(1L);
        StepCount stepCount = StepCount.of(20000);
        LocalDateTime timestamp = LocalDateTime.now();

        // Mock existing steps for the day (total would exceed 50000)
        StepRecord existingRecord = StepRecord.create(guardianId, StepCount.of(40000));
        when(stepRepository.findByGuardianIdAndDate(guardianId, timestamp.toLocalDate()))
                .thenReturn(Collections.singletonList(existingRecord));

        // When
        StepValidationResult result =
                stepValidationService.validateStepCount(guardianId, stepCount, timestamp);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).contains("Daily step count would exceed maximum");
    }

    @Test
    @DisplayName("Should check rate limiting")
    void shouldCheckRateLimiting() {
        // Given
        GuardianId guardianId = GuardianId.of(1L);
        LocalDateTime timestamp = LocalDateTime.now();

        // When - Within rate limit
        when(stepRepository.countSubmissionsInLastHour(guardianId, timestamp)).thenReturn(50);
        boolean withinLimit =
                stepValidationService.isWithinSubmissionRateLimit(guardianId, timestamp);

        // Then
        assertThat(withinLimit).isTrue();

        // When - Exceeding rate limit
        when(stepRepository.countSubmissionsInLastHour(guardianId, timestamp)).thenReturn(150);
        boolean exceedsLimit =
                stepValidationService.isWithinSubmissionRateLimit(guardianId, timestamp);

        // Then
        assertThat(exceedsLimit).isFalse();
    }

    @Test
    @DisplayName("Should validate data integrity")
    void shouldValidateDataIntegrity() {
        // Given
        GuardianId guardianId = GuardianId.of(1L);
        StepCount stepCount = StepCount.of(1000);
        StepRecord validRecord = StepRecord.create(guardianId, stepCount);

        // When
        boolean hasIntegrity = stepValidationService.hasValidDataIntegrity(validRecord);

        // Then
        assertThat(hasIntegrity).isTrue();
    }

    @Test
    @DisplayName("Should calculate reasonable step increments")
    void shouldCalculateReasonableStepIncrements() {
        // Given
        GuardianId guardianId = GuardianId.of(1L);
        StepCount previousSteps = StepCount.of(1000);
        StepCount currentSteps = StepCount.of(1500); // 500 step increase
        LocalDateTime previousTime = LocalDateTime.now().minusMinutes(10);
        LocalDateTime currentTime = LocalDateTime.now();

        // When
        boolean isReasonable =
                stepValidationService.isReasonableIncrement(
                        guardianId, previousSteps, currentSteps, previousTime, currentTime);

        // Then
        assertThat(isReasonable).isTrue(); // 500 steps in 10 minutes = 50 steps/minute < 200 limit
    }

    @Test
    @DisplayName("Should detect unreasonable step increments")
    void shouldDetectUnreasonableStepIncrements() {
        // Given
        GuardianId guardianId = GuardianId.of(1L);
        StepCount previousSteps = StepCount.of(1000);
        StepCount currentSteps = StepCount.of(1500); // 500 step increase
        LocalDateTime previousTime = LocalDateTime.now().minusMinutes(1);
        LocalDateTime currentTime = LocalDateTime.now();

        // When
        boolean isReasonable =
                stepValidationService.isReasonableIncrement(
                        guardianId, previousSteps, currentSteps, previousTime, currentTime);

        // Then
        assertThat(isReasonable).isFalse(); // 500 steps in 1 minute = 500 steps/minute > 200 limit
    }
}

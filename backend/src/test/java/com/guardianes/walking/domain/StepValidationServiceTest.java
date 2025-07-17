package com.guardianes.walking.domain;

import com.guardianes.shared.infrastructure.metrics.BusinessMetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Step Validation Service Tests")
class StepValidationServiceTest {

    @Mock
    private StepRepository stepRepository;

    @Mock
    private AnomalyDetectionService anomalyDetectionService;

    @Mock
    private BusinessMetricsService metricsService;

    private StepValidationService stepValidationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        stepValidationService = new StepValidationService(stepRepository, anomalyDetectionService, metricsService);
    }

    @Test
    @DisplayName("Should validate normal step count")
    void shouldValidateNormalStepCount() {
        // Given
        Long guardianId = 1L;
        int stepCount = 5000;
        LocalDateTime timestamp = LocalDateTime.now();
        
        when(stepRepository.findByGuardianIdAndDate(guardianId, timestamp.toLocalDate()))
            .thenReturn(Arrays.asList(
                new StepRecord(guardianId, 2000, timestamp.minusHours(2))
            ));
        when(anomalyDetectionService.isAnomalous(guardianId, stepCount, timestamp))
            .thenReturn(false);

        // When
        StepValidationResult result = stepValidationService.validateStepCount(guardianId, stepCount, timestamp);

        // Then
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
        verify(stepRepository, times(1)).findByGuardianIdAndDate(guardianId, timestamp.toLocalDate());
        verify(anomalyDetectionService, times(1)).isAnomalous(guardianId, stepCount, timestamp);
    }

    @Test
    @DisplayName("Should reject negative step count")
    void shouldRejectNegativeStepCount() {
        // Given
        Long guardianId = 1L;
        int stepCount = -100;
        LocalDateTime timestamp = LocalDateTime.now();

        // When
        StepValidationResult result = stepValidationService.validateStepCount(guardianId, stepCount, timestamp);

        // Then
        assertFalse(result.isValid());
        assertEquals("Step count cannot be negative", result.getErrorMessage());
        verify(stepRepository, never()).findByGuardianIdAndDate(anyLong(), any(LocalDate.class));
        verify(anomalyDetectionService, never()).isAnomalous(anyLong(), anyInt(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should reject step count exceeding daily maximum")
    void shouldRejectStepCountExceedingDailyMaximum() {
        // Given
        Long guardianId = 1L;
        int stepCount = 10000;
        LocalDateTime timestamp = LocalDateTime.now();
        
        when(stepRepository.findByGuardianIdAndDate(guardianId, timestamp.toLocalDate()))
            .thenReturn(Arrays.asList(
                new StepRecord(guardianId, 45000, timestamp.minusHours(1))
            ));

        // When
        StepValidationResult result = stepValidationService.validateStepCount(guardianId, stepCount, timestamp);

        // Then
        assertFalse(result.isValid());
        assertEquals("Daily step count would exceed maximum allowed (50000)", result.getErrorMessage());
        verify(stepRepository, times(1)).findByGuardianIdAndDate(guardianId, timestamp.toLocalDate());
        verify(anomalyDetectionService, never()).isAnomalous(anyLong(), anyInt(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should reject step count that would exceed daily maximum")
    void shouldRejectStepCountThatWouldExceedDailyMaximum() {
        // Given
        Long guardianId = 1L;
        int stepCount = 60000;
        LocalDateTime timestamp = LocalDateTime.now();

        // When
        StepValidationResult result = stepValidationService.validateStepCount(guardianId, stepCount, timestamp);

        // Then
        assertFalse(result.isValid());
        assertEquals("Step count exceeds daily maximum (50000)", result.getErrorMessage());
        verify(stepRepository, never()).findByGuardianIdAndDate(anyLong(), any(LocalDate.class));
        verify(anomalyDetectionService, never()).isAnomalous(anyLong(), anyInt(), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should reject anomalous step count")
    void shouldRejectAnomalousStepCount() {
        // Given
        Long guardianId = 1L;
        int stepCount = 15000;
        LocalDateTime timestamp = LocalDateTime.now();
        
        when(stepRepository.findByGuardianIdAndDate(guardianId, timestamp.toLocalDate()))
            .thenReturn(Arrays.asList(
                new StepRecord(guardianId, 2000, timestamp.minusHours(2))
            ));
        when(anomalyDetectionService.isAnomalous(guardianId, stepCount, timestamp))
            .thenReturn(true);

        // When
        StepValidationResult result = stepValidationService.validateStepCount(guardianId, stepCount, timestamp);

        // Then
        assertFalse(result.isValid());
        assertEquals("Step count appears anomalous and requires verification", result.getErrorMessage());
        verify(stepRepository, times(1)).findByGuardianIdAndDate(guardianId, timestamp.toLocalDate());
        verify(anomalyDetectionService, times(1)).isAnomalous(guardianId, stepCount, timestamp);
    }

    @Test
    @DisplayName("Should validate step increment reasonableness")
    void shouldValidateStepIncrementReasonableness() {
        // Given
        Long guardianId = 1L;
        int previousSteps = 1000;
        int currentSteps = 2000;
        LocalDateTime previousTimestamp = LocalDateTime.now().minusHours(1);
        LocalDateTime currentTimestamp = LocalDateTime.now();

        // When
        boolean result = stepValidationService.isReasonableIncrement(
            guardianId, previousSteps, currentSteps, previousTimestamp, currentTimestamp
        );

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Should reject unreasonable step increment")
    void shouldRejectUnreasonableStepIncrement() {
        // Given
        Long guardianId = 1L;
        int previousSteps = 1000;
        int currentSteps = 20000;
        LocalDateTime previousTimestamp = LocalDateTime.now().minusMinutes(10);
        LocalDateTime currentTimestamp = LocalDateTime.now();

        // When
        boolean result = stepValidationService.isReasonableIncrement(
            guardianId, previousSteps, currentSteps, previousTimestamp, currentTimestamp
        );

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should detect step count spikes")
    void shouldDetectStepCountSpikes() {
        // Given
        Long guardianId = 1L;
        LocalDate date = LocalDate.now();
        List<StepRecord> recentSteps = Arrays.asList(
            new StepRecord(guardianId, 1000, date.minusDays(2).atTime(10, 0)),
            new StepRecord(guardianId, 1200, date.minusDays(1).atTime(10, 0)),
            new StepRecord(guardianId, 15000, date.atTime(10, 0)) // Spike
        );
        
        when(stepRepository.findByGuardianIdAndDateRange(guardianId, date.minusDays(7), date))
            .thenReturn(recentSteps);

        // When
        boolean result = stepValidationService.hasStepCountSpike(guardianId, date);

        // Then
        assertTrue(result);
        verify(stepRepository, times(1)).findByGuardianIdAndDateRange(guardianId, date.minusDays(7), date);
    }

    @Test
    @DisplayName("Should not detect spike in normal step progression")
    void shouldNotDetectSpikeInNormalStepProgression() {
        // Given
        Long guardianId = 1L;
        LocalDate date = LocalDate.now();
        List<StepRecord> recentSteps = Arrays.asList(
            new StepRecord(guardianId, 1000, date.minusDays(2).atTime(10, 0)),
            new StepRecord(guardianId, 1200, date.minusDays(1).atTime(10, 0)),
            new StepRecord(guardianId, 1500, date.atTime(10, 0)) // Normal progression
        );
        
        when(stepRepository.findByGuardianIdAndDateRange(guardianId, date.minusDays(7), date))
            .thenReturn(recentSteps);

        // When
        boolean result = stepValidationService.hasStepCountSpike(guardianId, date);

        // Then
        assertFalse(result);
        verify(stepRepository, times(1)).findByGuardianIdAndDateRange(guardianId, date.minusDays(7), date);
    }

    @Test
    @DisplayName("Should validate step submission rate")
    void shouldValidateStepSubmissionRate() {
        // Given
        Long guardianId = 1L;
        LocalDateTime timestamp = LocalDateTime.now();
        
        when(stepRepository.countSubmissionsInLastHour(guardianId, timestamp))
            .thenReturn(10);

        // When
        boolean result = stepValidationService.isWithinSubmissionRateLimit(guardianId, timestamp);

        // Then
        assertTrue(result);
        verify(stepRepository, times(1)).countSubmissionsInLastHour(guardianId, timestamp);
    }

    @Test
    @DisplayName("Should reject when submission rate limit exceeded")
    void shouldRejectWhenSubmissionRateLimitExceeded() {
        // Given
        Long guardianId = 1L;
        LocalDateTime timestamp = LocalDateTime.now();
        
        when(stepRepository.countSubmissionsInLastHour(guardianId, timestamp))
            .thenReturn(120); // Exceeds limit

        // When
        boolean result = stepValidationService.isWithinSubmissionRateLimit(guardianId, timestamp);

        // Then
        assertFalse(result);
        verify(stepRepository, times(1)).countSubmissionsInLastHour(guardianId, timestamp);
    }

    @Test
    @DisplayName("Should validate step data integrity")
    void shouldValidateStepDataIntegrity() {
        // Given
        StepRecord stepRecord = new StepRecord(1L, 5000, LocalDateTime.now());

        // When
        boolean result = stepValidationService.hasValidDataIntegrity(stepRecord);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Should reject step data with invalid integrity")
    void shouldRejectStepDataWithInvalidIntegrity() {
        // Given
        StepRecord stepRecord = new StepRecord(null, 5000, LocalDateTime.now()); // Invalid guardian ID

        // When
        boolean result = stepValidationService.hasValidDataIntegrity(stepRecord);

        // Then
        assertFalse(result);
    }
}
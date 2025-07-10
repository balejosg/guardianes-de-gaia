package com.guardianes.walking.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.walking.domain.model.DailyStepAggregate;
import com.guardianes.walking.domain.model.Energy;
import com.guardianes.walking.domain.model.StepCount;
import com.guardianes.walking.domain.model.StepSubmissionResult;
import com.guardianes.walking.domain.repository.EnergyRepository;
import com.guardianes.walking.domain.repository.StepRepository;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("StepSubmissionDomainService Tests")
class StepSubmissionDomainServiceTest {

    @Mock private StepRepository stepRepository;

    @Mock private EnergyRepository energyRepository;

    private StepSubmissionDomainService stepSubmissionDomainService;

    @BeforeEach
    void setUp() {
        stepSubmissionDomainService =
                new StepSubmissionDomainService(stepRepository, energyRepository);
    }

    @Test
    @DisplayName("Should process step submission successfully")
    void shouldProcessStepSubmissionSuccessfully() {
        // Given
        GuardianId guardianId = GuardianId.of(1L);
        StepCount stepCount = StepCount.of(1000);

        // Mock empty daily aggregate (no existing steps)
        when(stepRepository.findDailyAggregatesByGuardianIdAndDateRange(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        // When
        StepSubmissionResult result =
                stepSubmissionDomainService.processStepSubmission(guardianId, stepCount);

        // Then
        assertNotNull(result);
        assertEquals(guardianId, result.getStepRecord().getGuardianId());
        assertEquals(stepCount, result.getStepRecord().getStepCount());
        assertEquals(Energy.of(100), result.getEnergyGenerated()); // 1000 steps = 100 energy
        assertEquals(stepCount, result.getUpdatedDailyAggregate().getTotalSteps());

        // Verify repository interactions
        verify(stepRepository).save(any());
        verify(energyRepository).saveTransaction(any());
        verify(stepRepository).saveDailyAggregate(any());
    }

    @Test
    @DisplayName("Should reject step submission that would exceed daily maximum")
    void shouldRejectStepSubmissionThatWouldExceedDailyMaximum() {
        // Given
        GuardianId guardianId = GuardianId.of(1L);
        StepCount stepCount = StepCount.of(10000); // Would exceed when added to existing 45000

        // Mock existing daily aggregate with 45000 steps
        DailyStepAggregate existingAggregate =
                DailyStepAggregate.create(
                        GuardianId.of(1L), java.time.LocalDate.now(), StepCount.of(45000));
        when(stepRepository.findDailyAggregatesByGuardianIdAndDateRange(any(), any(), any()))
                .thenReturn(Collections.singletonList(existingAggregate));

        // When & Then
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                stepSubmissionDomainService.processStepSubmission(
                                        guardianId, stepCount));

        assertTrue(exception.getMessage().contains("exceed daily maximum"));

        // Verify no repository writes occurred
        verify(stepRepository, never()).save(any());
        verify(energyRepository, never()).saveTransaction(any());
        verify(stepRepository, never()).saveDailyAggregate(any());
    }

    @Test
    @DisplayName("Should reject null guardian ID")
    void shouldRejectNullGuardianId() {
        // Given
        StepCount stepCount = StepCount.of(1000);

        // When & Then
        assertThrows(
                NullPointerException.class,
                () -> stepSubmissionDomainService.processStepSubmission(null, stepCount));
    }

    @Test
    @DisplayName("Should reject null step count")
    void shouldRejectNullStepCount() {
        // Given
        GuardianId guardianId = GuardianId.of(1L);

        // When & Then
        assertThrows(
                NullPointerException.class,
                () -> stepSubmissionDomainService.processStepSubmission(guardianId, null));
    }

    @Test
    @DisplayName("Should validate reasonable step submission")
    void shouldValidateReasonableStepSubmission() {
        // Given
        GuardianId guardianId = GuardianId.of(1L);
        StepCount reasonableSteps = StepCount.of(5000);
        StepCount unreasonableSteps = StepCount.of(15000);

        // When & Then
        assertTrue(
                stepSubmissionDomainService.isReasonableStepSubmission(
                        guardianId, reasonableSteps));
        assertFalse(
                stepSubmissionDomainService.isReasonableStepSubmission(
                        guardianId, unreasonableSteps));
    }
}

package com.guardianes.walking.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.walking.domain.model.DailyStepAggregate;
import com.guardianes.walking.domain.model.StepCount;
import com.guardianes.walking.domain.repository.StepRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Cached Step Aggregation Service Basic Tests")
class CachedStepAggregationServiceBasicTest {

    @Mock private StepRepository stepRepository;

    private CachedStepAggregationService cachedStepAggregationService;

    private GuardianId testGuardianId;
    private LocalDate testDate;
    private DailyStepAggregate testAggregate;

    @BeforeEach
    void setUp() {
        cachedStepAggregationService = new CachedStepAggregationService(stepRepository);

        testGuardianId = GuardianId.of(123L);
        testDate = LocalDate.of(2024, 1, 15);
        testAggregate = DailyStepAggregate.create(testGuardianId, testDate, StepCount.of(5000));
    }

    @Test
    @DisplayName("Should retrieve daily aggregate from repository")
    void shouldRetrieveDailyAggregateFromRepository() {
        // Given
        when(stepRepository.findDailyAggregatesByGuardianIdAndDateRange(
                        testGuardianId, testDate, testDate))
                .thenReturn(List.of(testAggregate));

        // When
        DailyStepAggregate result =
                cachedStepAggregationService.getDailyAggregate(testGuardianId, testDate);

        // Then
        assertNotNull(result);
        assertEquals(testGuardianId, result.getGuardianId());
        assertEquals(testDate, result.getDate());
        assertEquals(5000, result.getTotalSteps().value());
        verify(stepRepository, times(1))
                .findDailyAggregatesByGuardianIdAndDateRange(testGuardianId, testDate, testDate);
    }

    @Test
    @DisplayName("Should return empty aggregate when repository returns empty list")
    void shouldReturnEmptyAggregateWhenRepositoryReturnsEmptyList() {
        // Given
        when(stepRepository.findDailyAggregatesByGuardianIdAndDateRange(
                        testGuardianId, testDate, testDate))
                .thenReturn(List.of());

        // When
        DailyStepAggregate result =
                cachedStepAggregationService.getDailyAggregate(testGuardianId, testDate);

        // Then
        assertNotNull(result);
        assertEquals(testGuardianId, result.getGuardianId());
        assertEquals(testDate, result.getDate());
        assertEquals(0, result.getTotalSteps().value());
    }

    @Test
    @DisplayName("Should handle repository exception gracefully")
    void shouldHandleRepositoryExceptionGracefully() {
        // Given
        when(stepRepository.findDailyAggregatesByGuardianIdAndDateRange(
                        testGuardianId, testDate, testDate))
                .thenThrow(new RuntimeException("Database error"));

        // When
        DailyStepAggregate result =
                cachedStepAggregationService.getDailyAggregate(testGuardianId, testDate);

        // Then
        assertNotNull(result);
        assertEquals(testGuardianId, result.getGuardianId());
        assertEquals(testDate, result.getDate());
        assertEquals(0, result.getTotalSteps().value());
    }

    @Test
    @DisplayName("Should retrieve step history for date range")
    void shouldRetrieveStepHistoryForDateRange() {
        // Given
        LocalDate fromDate = testDate.minusDays(7);
        LocalDate toDate = testDate;
        List<DailyStepAggregate> expectedHistory =
                List.of(
                        DailyStepAggregate.create(testGuardianId, fromDate, StepCount.of(3000)),
                        DailyStepAggregate.create(testGuardianId, testDate, StepCount.of(5000)));

        when(stepRepository.findDailyAggregatesByGuardianIdAndDateRange(
                        testGuardianId, fromDate, toDate))
                .thenReturn(expectedHistory);

        // When
        List<DailyStepAggregate> result =
                cachedStepAggregationService.getStepHistory(testGuardianId, fromDate, toDate);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(stepRepository, times(1))
                .findDailyAggregatesByGuardianIdAndDateRange(testGuardianId, fromDate, toDate);
    }

    @Test
    @DisplayName("Should retrieve current week aggregates")
    void shouldRetrieveCurrentWeekAggregates() {
        // Given
        LocalDate weekStart = testDate.minusDays(testDate.getDayOfWeek().getValue() % 7);
        LocalDate weekEnd = weekStart.plusDays(6);
        List<DailyStepAggregate> expectedWeekData =
                List.of(
                        DailyStepAggregate.create(testGuardianId, weekStart, StepCount.of(4000)),
                        DailyStepAggregate.create(testGuardianId, testDate, StepCount.of(5000)));

        when(stepRepository.findDailyAggregatesByGuardianIdAndDateRange(
                        testGuardianId, weekStart, weekEnd))
                .thenReturn(expectedWeekData);

        // When
        List<DailyStepAggregate> result =
                cachedStepAggregationService.getCurrentWeekAggregates(testGuardianId, weekStart);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(stepRepository, times(1))
                .findDailyAggregatesByGuardianIdAndDateRange(testGuardianId, weekStart, weekEnd);
    }

    @Test
    @DisplayName("Should handle different guardians independently")
    void shouldHandleDifferentGuardiansIndependently() {
        // Given
        GuardianId guardian1 = GuardianId.of(123L);
        GuardianId guardian2 = GuardianId.of(456L);
        DailyStepAggregate aggregate1 =
                DailyStepAggregate.create(guardian1, testDate, StepCount.of(3000));
        DailyStepAggregate aggregate2 =
                DailyStepAggregate.create(guardian2, testDate, StepCount.of(7000));

        when(stepRepository.findDailyAggregatesByGuardianIdAndDateRange(
                        guardian1, testDate, testDate))
                .thenReturn(List.of(aggregate1));
        when(stepRepository.findDailyAggregatesByGuardianIdAndDateRange(
                        guardian2, testDate, testDate))
                .thenReturn(List.of(aggregate2));

        // When
        DailyStepAggregate result1 =
                cachedStepAggregationService.getDailyAggregate(guardian1, testDate);
        DailyStepAggregate result2 =
                cachedStepAggregationService.getDailyAggregate(guardian2, testDate);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(3000, result1.getTotalSteps().value());
        assertEquals(7000, result2.getTotalSteps().value());

        verify(stepRepository, times(1))
                .findDailyAggregatesByGuardianIdAndDateRange(guardian1, testDate, testDate);
        verify(stepRepository, times(1))
                .findDailyAggregatesByGuardianIdAndDateRange(guardian2, testDate, testDate);
    }

    @Test
    @DisplayName("Should handle different dates independently")
    void shouldHandleDifferentDatesIndependently() {
        // Given
        LocalDate date1 = LocalDate.of(2024, 1, 15);
        LocalDate date2 = LocalDate.of(2024, 1, 16);
        DailyStepAggregate aggregate1 =
                DailyStepAggregate.create(testGuardianId, date1, StepCount.of(3000));
        DailyStepAggregate aggregate2 =
                DailyStepAggregate.create(testGuardianId, date2, StepCount.of(7000));

        when(stepRepository.findDailyAggregatesByGuardianIdAndDateRange(
                        testGuardianId, date1, date1))
                .thenReturn(List.of(aggregate1));
        when(stepRepository.findDailyAggregatesByGuardianIdAndDateRange(
                        testGuardianId, date2, date2))
                .thenReturn(List.of(aggregate2));

        // When
        DailyStepAggregate result1 =
                cachedStepAggregationService.getDailyAggregate(testGuardianId, date1);
        DailyStepAggregate result2 =
                cachedStepAggregationService.getDailyAggregate(testGuardianId, date2);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(3000, result1.getTotalSteps().value());
        assertEquals(7000, result2.getTotalSteps().value());

        verify(stepRepository, times(1))
                .findDailyAggregatesByGuardianIdAndDateRange(testGuardianId, date1, date1);
        verify(stepRepository, times(1))
                .findDailyAggregatesByGuardianIdAndDateRange(testGuardianId, date2, date2);
    }

    @Test
    @DisplayName("Should execute invalidation methods without errors")
    void shouldExecuteInvalidationMethodsWithoutErrors() {
        // When & Then - These should not throw exceptions
        assertDoesNotThrow(
                () -> {
                    cachedStepAggregationService.invalidateDailyAggregate(testGuardianId, testDate);
                    cachedStepAggregationService.invalidateStepHistory(testGuardianId);
                    cachedStepAggregationService.invalidateAllCachesForGuardian(testGuardianId);
                });
    }

    @Test
    @DisplayName("Should execute preload methods without errors")
    void shouldExecutePreloadMethodsWithoutErrors() {
        // Given
        LocalDate today = LocalDate.now();
        when(stepRepository.findDailyAggregatesByGuardianIdAndDateRange(any(), any(), any()))
                .thenReturn(List.of());

        // When & Then - These should not throw exceptions
        assertDoesNotThrow(
                () -> {
                    cachedStepAggregationService.preloadTodaysAggregate(testGuardianId);
                    cachedStepAggregationService.preloadCurrentWeekAggregates(testGuardianId);
                });
    }
}

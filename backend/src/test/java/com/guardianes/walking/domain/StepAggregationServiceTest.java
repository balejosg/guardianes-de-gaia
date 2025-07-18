package com.guardianes.walking.domain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@DisplayName("Step Aggregation Service Tests")
class StepAggregationServiceTest {

  @Mock private StepRepository stepRepository;

  private StepAggregationService stepAggregationService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    stepAggregationService = new StepAggregationService(stepRepository);
  }

  @Test
  @DisplayName("Should aggregate daily steps for a guardian")
  void shouldAggregateDailyStepsForGuardian() {
    // Given
    Long guardianId = 1L;
    LocalDate date = LocalDate.now();
    List<StepRecord> stepRecords =
        Arrays.asList(
            new StepRecord(guardianId, 1000, date.atTime(9, 0)),
            new StepRecord(guardianId, 2000, date.atTime(12, 0)),
            new StepRecord(guardianId, 1500, date.atTime(15, 0)));
    when(stepRepository.findByGuardianIdAndDate(guardianId, date)).thenReturn(stepRecords);

    // When
    DailyStepAggregate result = stepAggregationService.aggregateDailySteps(guardianId, date);

    // Then
    assertNotNull(result);
    assertEquals(4500, result.getTotalSteps());
    assertEquals(guardianId, result.getGuardianId());
    assertEquals(date, result.getDate());
    verify(stepRepository, times(1)).findByGuardianIdAndDate(guardianId, date);
  }

  @Test
  @DisplayName("Should return zero steps when no records exist")
  void shouldReturnZeroStepsWhenNoRecordsExist() {
    // Given
    Long guardianId = 1L;
    LocalDate date = LocalDate.now();
    when(stepRepository.findByGuardianIdAndDate(guardianId, date)).thenReturn(Arrays.asList());

    // When
    DailyStepAggregate result = stepAggregationService.aggregateDailySteps(guardianId, date);

    // Then
    assertNotNull(result);
    assertEquals(0, result.getTotalSteps());
    assertEquals(guardianId, result.getGuardianId());
    assertEquals(date, result.getDate());
  }

  @Test
  @DisplayName("Should get current step count for guardian")
  void shouldGetCurrentStepCountForGuardian() {
    // Given
    Long guardianId = 1L;
    LocalDate today = LocalDate.now();
    when(stepRepository.findByGuardianIdAndDate(guardianId, today))
        .thenReturn(Arrays.asList(new StepRecord(guardianId, 5000, today.atTime(10, 0))));

    // When
    int currentSteps = stepAggregationService.getCurrentStepCount(guardianId);

    // Then
    assertEquals(5000, currentSteps);
    verify(stepRepository, times(1)).findByGuardianIdAndDate(guardianId, today);
  }

  @Test
  @DisplayName("Should save daily step aggregate")
  void shouldSaveDailyStepAggregate() {
    // Given
    Long guardianId = 1L;
    LocalDate date = LocalDate.now();
    DailyStepAggregate aggregate = new DailyStepAggregate(guardianId, date, 3000);

    when(stepRepository.saveDailyAggregate(any(DailyStepAggregate.class))).thenReturn(aggregate);

    // When
    DailyStepAggregate result = stepAggregationService.saveDailyAggregate(aggregate);

    // Then
    assertNotNull(result);
    assertEquals(3000, result.getTotalSteps());
    assertEquals(guardianId, result.getGuardianId());
    assertEquals(date, result.getDate());
    verify(stepRepository, times(1)).saveDailyAggregate(aggregate);
  }

  @Test
  @DisplayName("Should get step history for guardian")
  void shouldGetStepHistoryForGuardian() {
    // Given
    Long guardianId = 1L;
    LocalDate fromDate = LocalDate.now().minusDays(7);
    LocalDate toDate = LocalDate.now();

    List<DailyStepAggregate> expectedHistory =
        Arrays.asList(
            new DailyStepAggregate(guardianId, fromDate, 2000),
            new DailyStepAggregate(guardianId, fromDate.plusDays(1), 3000),
            new DailyStepAggregate(guardianId, fromDate.plusDays(2), 4000));

    when(stepRepository.findDailyAggregatesByGuardianIdAndDateRange(guardianId, fromDate, toDate))
        .thenReturn(expectedHistory);

    // When
    List<DailyStepAggregate> result =
        stepAggregationService.getStepHistory(guardianId, fromDate, toDate);

    // Then
    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals(2000, result.get(0).getTotalSteps());
    assertEquals(3000, result.get(1).getTotalSteps());
    assertEquals(4000, result.get(2).getTotalSteps());
    verify(stepRepository, times(1))
        .findDailyAggregatesByGuardianIdAndDateRange(guardianId, fromDate, toDate);
  }
}

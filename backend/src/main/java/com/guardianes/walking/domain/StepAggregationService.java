package com.guardianes.walking.domain;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StepAggregationService {
  private final StepRepository stepRepository;

  public StepAggregationService(StepRepository stepRepository) {
    this.stepRepository = stepRepository;
  }

  @Transactional(readOnly = true)
  public DailyStepAggregate aggregateDailySteps(Long guardianId, LocalDate date) {
    List<StepRecord> stepRecords = stepRepository.findByGuardianIdAndDate(guardianId, date);
    int totalSteps = stepRecords.stream().mapToInt(StepRecord::getStepCount).sum();
    return new DailyStepAggregate(guardianId, date, totalSteps);
  }

  @Transactional(readOnly = true)
  public int getCurrentStepCount(Long guardianId) {
    LocalDate today = LocalDate.now();
    DailyStepAggregate aggregate = aggregateDailySteps(guardianId, today);
    return aggregate.getTotalSteps();
  }

  @Transactional
  public DailyStepAggregate saveDailyAggregate(DailyStepAggregate aggregate) {
    return stepRepository.saveDailyAggregate(aggregate);
  }

  @Transactional(readOnly = true)
  public List<DailyStepAggregate> getStepHistory(
      Long guardianId, LocalDate fromDate, LocalDate toDate) {
    return stepRepository.findDailyAggregatesByGuardianIdAndDateRange(guardianId, fromDate, toDate);
  }
}

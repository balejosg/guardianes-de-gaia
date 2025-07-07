package com.guardianes.walking.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface StepRepository {
    StepRecord save(StepRecord stepRecord);
    List<StepRecord> findByGuardianIdAndDate(Long guardianId, LocalDate date);
    List<StepRecord> findByGuardianIdAndDateRange(Long guardianId, LocalDate fromDate, LocalDate toDate);
    DailyStepAggregate saveDailyAggregate(DailyStepAggregate aggregate);
    List<DailyStepAggregate> findDailyAggregatesByGuardianIdAndDateRange(Long guardianId, LocalDate fromDate, LocalDate toDate);
    int countSubmissionsInLastHour(Long guardianId, LocalDateTime timestamp);
}
package com.guardianes.walking.domain.repository;

import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.walking.domain.model.DailyStepAggregate;
import com.guardianes.walking.domain.model.StepRecord;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface StepRepository {
    StepRecord save(StepRecord stepRecord);

    List<StepRecord> findByGuardianIdAndDate(GuardianId guardianId, LocalDate date);

    List<StepRecord> findByGuardianIdAndDateRange(
            GuardianId guardianId, LocalDate fromDate, LocalDate toDate);

    DailyStepAggregate saveDailyAggregate(DailyStepAggregate aggregate);

    List<DailyStepAggregate> findDailyAggregatesByGuardianIdAndDateRange(
            GuardianId guardianId, LocalDate fromDate, LocalDate toDate);

    int countSubmissionsInLastHour(GuardianId guardianId, LocalDateTime timestamp);
}

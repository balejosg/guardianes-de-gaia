package com.guardianes.walking.domain.service;

import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.walking.domain.model.DailyStepAggregate;
import com.guardianes.walking.domain.model.StepCount;
import com.guardianes.walking.domain.model.StepRecord;
import com.guardianes.walking.domain.repository.StepRepository;
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
    public DailyStepAggregate aggregateDailySteps(GuardianId guardianId, LocalDate date) {
        List<StepRecord> stepRecords = stepRepository.findByGuardianIdAndDate(guardianId, date);
        StepCount totalSteps =
                StepCount.of(
                        stepRecords.stream()
                                .mapToInt(stepRecord -> stepRecord.getStepCount().value())
                                .sum());
        return DailyStepAggregate.create(guardianId, date, totalSteps);
    }

    @Transactional(readOnly = true)
    public StepCount getCurrentStepCount(GuardianId guardianId) {
        LocalDate today = LocalDate.now();
        DailyStepAggregate aggregate = aggregateDailySteps(guardianId, today);
        return aggregate.getTotalSteps();
    }

    @Transactional
    public com.guardianes.walking.domain.model.DailyStepAggregate saveDailyAggregate(
            com.guardianes.walking.domain.model.DailyStepAggregate aggregate) {
        return stepRepository.saveDailyAggregate(aggregate);
    }

    @Transactional(readOnly = true)
    public List<DailyStepAggregate> getStepHistory(
            GuardianId guardianId, LocalDate fromDate, LocalDate toDate) {
        return stepRepository.findDailyAggregatesByGuardianIdAndDateRange(
                guardianId, fromDate, toDate);
    }
}

package com.guardianes.walking.domain;

import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;

@Component
public class StepAggregationService {
    private final StepRepository stepRepository;

    public StepAggregationService(StepRepository stepRepository) {
        this.stepRepository = stepRepository;
    }

    public DailyStepAggregate aggregateDailySteps(Long guardianId, LocalDate date) {
        List<StepRecord> stepRecords = stepRepository.findByGuardianIdAndDate(guardianId, date);
        int totalSteps = stepRecords.stream()
            .mapToInt(StepRecord::getStepCount)
            .sum();
        return new DailyStepAggregate(guardianId, date, totalSteps);
    }

    public int getCurrentStepCount(Long guardianId) {
        LocalDate today = LocalDate.now();
        DailyStepAggregate aggregate = aggregateDailySteps(guardianId, today);
        return aggregate.getTotalSteps();
    }

    public DailyStepAggregate saveDailyAggregate(DailyStepAggregate aggregate) {
        return stepRepository.saveDailyAggregate(aggregate);
    }

    public List<DailyStepAggregate> getStepHistory(Long guardianId, LocalDate fromDate, LocalDate toDate) {
        return stepRepository.findDailyAggregatesByGuardianIdAndDateRange(guardianId, fromDate, toDate);
    }
}
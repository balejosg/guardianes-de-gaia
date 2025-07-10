package com.guardianes.walking.infrastructure.repository;

import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.walking.domain.model.DailyStepAggregate;
import com.guardianes.walking.domain.model.StepRecord;
import com.guardianes.walking.domain.repository.StepRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryStepRepository implements StepRepository {

    private final Map<String, StepRecord> stepRecords = new ConcurrentHashMap<>();
    private final Map<String, DailyStepAggregate> dailyAggregates = new ConcurrentHashMap<>();

    @Override
    public StepRecord save(StepRecord stepRecord) {
        String key = stepRecord.getGuardianId().value() + "_" + stepRecord.getRecordedAt().value();
        stepRecords.put(key, stepRecord);
        return stepRecord;
    }

    @Override
    public List<StepRecord> findByGuardianIdAndDate(GuardianId guardianId, LocalDate date) {
        return stepRecords.values().stream()
                .filter(record -> record.getGuardianId().equals(guardianId))
                .filter(record -> record.getRecordedAt().value().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    @Override
    public List<StepRecord> findByGuardianIdAndDateRange(
            GuardianId guardianId, LocalDate fromDate, LocalDate toDate) {
        return stepRecords.values().stream()
                .filter(record -> record.getGuardianId().equals(guardianId))
                .filter(
                        record -> {
                            LocalDate recordDate = record.getRecordedAt().value().toLocalDate();
                            return !recordDate.isBefore(fromDate) && !recordDate.isAfter(toDate);
                        })
                .collect(Collectors.toList());
    }

    @Override
    public DailyStepAggregate saveDailyAggregate(DailyStepAggregate aggregate) {
        String key = aggregate.getGuardianId().value() + "_" + aggregate.getDate();
        dailyAggregates.put(key, aggregate);
        return aggregate;
    }

    @Override
    public List<DailyStepAggregate> findDailyAggregatesByGuardianIdAndDateRange(
            GuardianId guardianId, LocalDate fromDate, LocalDate toDate) {
        return dailyAggregates.values().stream()
                .filter(aggregate -> aggregate.getGuardianId().equals(guardianId))
                .filter(
                        aggregate -> {
                            LocalDate aggregateDate = aggregate.getDate();
                            return !aggregateDate.isBefore(fromDate)
                                    && !aggregateDate.isAfter(toDate);
                        })
                .collect(Collectors.toList());
    }

    @Override
    public int countSubmissionsInLastHour(GuardianId guardianId, LocalDateTime timestamp) {
        LocalDateTime oneHourAgo = timestamp.minusHours(1);
        return (int)
                stepRecords.values().stream()
                        .filter(record -> record.getGuardianId().equals(guardianId))
                        .filter(record -> record.getRecordedAt().value().isAfter(oneHourAgo))
                        .filter(record -> record.getRecordedAt().value().isBefore(timestamp))
                        .count();
    }

    // Helper method to add step records for testing
    public void addStepRecord(StepRecord record) {
        String key = record.getGuardianId().value() + "_" + record.getRecordedAt().value();
        stepRecords.put(key, record);
    }
}

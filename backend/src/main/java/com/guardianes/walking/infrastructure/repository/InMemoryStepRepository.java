package com.guardianes.walking.infrastructure.repository;

import com.guardianes.walking.domain.StepRepository;
import com.guardianes.walking.domain.StepRecord;
import com.guardianes.walking.domain.DailyStepAggregate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class InMemoryStepRepository implements StepRepository {
    
    private final Map<String, StepRecord> stepRecords = new ConcurrentHashMap<>();
    private final Map<String, DailyStepAggregate> dailyAggregates = new ConcurrentHashMap<>();
    private final Map<String, Integer> submissionCounts = new ConcurrentHashMap<>();
    
    @Override
    public StepRecord save(StepRecord stepRecord) {
        String key = stepRecord.getGuardianId() + "_" + stepRecord.getTimestamp();
        stepRecords.put(key, stepRecord);
        return stepRecord;
    }
    
    @Override
    public List<StepRecord> findByGuardianIdAndDate(Long guardianId, LocalDate date) {
        return stepRecords.values().stream()
                .filter(record -> record.getGuardianId().equals(guardianId))
                .filter(record -> record.getTimestamp().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<StepRecord> findByGuardianIdAndDateRange(Long guardianId, LocalDate fromDate, LocalDate toDate) {
        return stepRecords.values().stream()
                .filter(record -> record.getGuardianId().equals(guardianId))
                .filter(record -> {
                    LocalDate recordDate = record.getTimestamp().toLocalDate();
                    return !recordDate.isBefore(fromDate) && !recordDate.isAfter(toDate);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public DailyStepAggregate saveDailyAggregate(DailyStepAggregate aggregate) {
        String key = aggregate.getGuardianId() + "_" + aggregate.getDate();
        dailyAggregates.put(key, aggregate);
        return aggregate;
    }
    
    @Override
    public List<DailyStepAggregate> findDailyAggregatesByGuardianIdAndDateRange(Long guardianId, LocalDate fromDate, LocalDate toDate) {
        return dailyAggregates.values().stream()
                .filter(aggregate -> aggregate.getGuardianId().equals(guardianId))
                .filter(aggregate -> {
                    LocalDate aggregateDate = aggregate.getDate();
                    return !aggregateDate.isBefore(fromDate) && !aggregateDate.isAfter(toDate);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public int countSubmissionsInLastHour(Long guardianId, LocalDateTime timestamp) {
        LocalDateTime oneHourAgo = timestamp.minusHours(1);
        return (int) stepRecords.values().stream()
                .filter(record -> record.getGuardianId().equals(guardianId))
                .filter(record -> record.getTimestamp().isAfter(oneHourAgo))
                .filter(record -> record.getTimestamp().isBefore(timestamp))
                .count();
    }
    
    // Helper method to add step records for testing
    public void addStepRecord(StepRecord record) {
        String key = record.getGuardianId() + "_" + record.getTimestamp();
        stepRecords.put(key, record);
    }
}
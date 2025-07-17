package com.guardianes.walking.infrastructure.persistence.repository;

import com.guardianes.walking.domain.DailyStepAggregate;
import com.guardianes.walking.domain.StepRecord;
import com.guardianes.walking.domain.StepRepository;
import com.guardianes.walking.infrastructure.persistence.entity.DailyStepAggregateEntity;
import com.guardianes.walking.infrastructure.persistence.entity.StepRecordEntity;
import com.guardianes.walking.infrastructure.persistence.mapper.DailyStepAggregateMapper;
import com.guardianes.walking.infrastructure.persistence.mapper.StepRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Transactional
public class JpaStepRepository implements StepRepository {
    
    private final StepRecordJpaRepository stepRecordJpaRepository;
    private final DailyStepAggregateJpaRepository dailyStepAggregateJpaRepository;
    private final StepRecordMapper stepRecordMapper;
    private final DailyStepAggregateMapper dailyStepAggregateMapper;
    
    @Autowired
    public JpaStepRepository(
            StepRecordJpaRepository stepRecordJpaRepository,
            DailyStepAggregateJpaRepository dailyStepAggregateJpaRepository,
            StepRecordMapper stepRecordMapper,
            DailyStepAggregateMapper dailyStepAggregateMapper) {
        this.stepRecordJpaRepository = stepRecordJpaRepository;
        this.dailyStepAggregateJpaRepository = dailyStepAggregateJpaRepository;
        this.stepRecordMapper = stepRecordMapper;
        this.dailyStepAggregateMapper = dailyStepAggregateMapper;
    }
    
    @Override
    public StepRecord save(StepRecord stepRecord) {
        StepRecordEntity entity = stepRecordMapper.toEntity(stepRecord);
        StepRecordEntity savedEntity = stepRecordJpaRepository.save(entity);
        return stepRecordMapper.toDomain(savedEntity);
    }
    
    @Override
    public List<StepRecord> findByGuardianIdAndDate(Long guardianId, LocalDate date) {
        List<StepRecordEntity> entities = stepRecordJpaRepository.findByGuardianIdAndDate(guardianId, date);
        return entities.stream()
                .map(stepRecordMapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<StepRecord> findByGuardianIdAndDateRange(Long guardianId, LocalDate fromDate, LocalDate toDate) {
        List<StepRecordEntity> entities = stepRecordJpaRepository.findByGuardianIdAndDateRange(guardianId, fromDate, toDate);
        return entities.stream()
                .map(stepRecordMapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public DailyStepAggregate saveDailyAggregate(DailyStepAggregate aggregate) {
        // Check if aggregate already exists for this guardian and date
        Optional<DailyStepAggregateEntity> existingEntity = 
            dailyStepAggregateJpaRepository.findByGuardianIdAndDate(
                aggregate.getGuardianId(), 
                aggregate.getDate()
            );
        
        DailyStepAggregateEntity entityToSave;
        if (existingEntity.isPresent()) {
            // Update existing entity
            entityToSave = existingEntity.get();
            entityToSave.setTotalSteps(aggregate.getTotalSteps());
        } else {
            // Create new entity
            entityToSave = dailyStepAggregateMapper.toEntity(aggregate);
        }
        
        DailyStepAggregateEntity savedEntity = dailyStepAggregateJpaRepository.save(entityToSave);
        return dailyStepAggregateMapper.toDomain(savedEntity);
    }
    
    @Override
    public List<DailyStepAggregate> findDailyAggregatesByGuardianIdAndDateRange(Long guardianId, LocalDate fromDate, LocalDate toDate) {
        List<DailyStepAggregateEntity> entities = 
            dailyStepAggregateJpaRepository.findByGuardianIdAndDateRange(guardianId, fromDate, toDate);
        return entities.stream()
                .map(dailyStepAggregateMapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public int countSubmissionsInLastHour(Long guardianId, LocalDateTime timestamp) {
        LocalDateTime oneHourAgo = timestamp.minusHours(1);
        return stepRecordJpaRepository.countSubmissionsInLastHour(guardianId, oneHourAgo, timestamp);
    }
}
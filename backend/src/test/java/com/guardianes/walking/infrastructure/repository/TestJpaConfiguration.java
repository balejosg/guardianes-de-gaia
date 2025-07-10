package com.guardianes.walking.infrastructure.repository;

import com.guardianes.walking.domain.repository.StepRepository;
import com.guardianes.walking.infrastructure.persistence.mapper.DailyStepAggregateMapper;
import com.guardianes.walking.infrastructure.persistence.mapper.StepRecordMapper;
import com.guardianes.walking.infrastructure.persistence.repository.DailyStepAggregateJpaRepository;
import com.guardianes.walking.infrastructure.persistence.repository.JpaStepRepository;
import com.guardianes.walking.infrastructure.persistence.repository.StepRecordJpaRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestJpaConfiguration {

    @Bean
    @Primary
    public StepRepository stepRepository(
            StepRecordJpaRepository stepRecordJpaRepository,
            DailyStepAggregateJpaRepository dailyStepAggregateJpaRepository,
            StepRecordMapper stepRecordMapper,
            DailyStepAggregateMapper dailyStepAggregateMapper) {
        return new JpaStepRepository(
                stepRecordJpaRepository,
                dailyStepAggregateJpaRepository,
                stepRecordMapper,
                dailyStepAggregateMapper);
    }

    @Bean
    public StepRecordMapper stepRecordMapper() {
        return new StepRecordMapper();
    }

    @Bean
    public DailyStepAggregateMapper dailyStepAggregateMapper() {
        return new DailyStepAggregateMapper();
    }
}

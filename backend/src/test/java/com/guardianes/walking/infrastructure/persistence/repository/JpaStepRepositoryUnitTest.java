package com.guardianes.walking.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.shared.domain.model.Timestamp;
import com.guardianes.walking.domain.model.StepCount;
import com.guardianes.walking.domain.model.StepRecord;
import com.guardianes.walking.infrastructure.persistence.entity.StepRecordEntity;
import com.guardianes.walking.infrastructure.persistence.mapper.DailyStepAggregateMapper;
import com.guardianes.walking.infrastructure.persistence.mapper.StepRecordMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class JpaStepRepositoryUnitTest {

    @Mock private StepRecordJpaRepository stepRecordJpaRepository;

    @Mock private DailyStepAggregateJpaRepository dailyStepAggregateJpaRepository;

    private StepRecordMapper stepRecordMapper;
    private DailyStepAggregateMapper dailyStepAggregateMapper;
    private JpaStepRepository jpaStepRepository;

    @BeforeEach
    void setUp() {
        stepRecordMapper = new StepRecordMapper();
        dailyStepAggregateMapper = new DailyStepAggregateMapper();
        jpaStepRepository =
                new JpaStepRepository(
                        stepRecordJpaRepository,
                        dailyStepAggregateJpaRepository,
                        stepRecordMapper,
                        dailyStepAggregateMapper);
    }

    @Test
    void shouldSaveStepRecord() {
        // Given
        GuardianId guardianId = GuardianId.of(1L);
        StepCount stepCount = StepCount.of(1000);
        LocalDateTime timestamp = LocalDateTime.now().minusHours(1); // Use past timestamp
        StepRecord stepRecord =
                StepRecord.createWithTimestamp(guardianId, stepCount, Timestamp.of(timestamp));

        StepRecordEntity savedEntity =
                new StepRecordEntity(guardianId.value(), stepCount.value(), timestamp);
        savedEntity.setId(123L);

        when(stepRecordJpaRepository.save(any(StepRecordEntity.class))).thenReturn(savedEntity);

        // When
        StepRecord savedStepRecord = jpaStepRepository.save(stepRecord);

        // Then
        assertThat(savedStepRecord).isNotNull();
        assertThat(savedStepRecord.getGuardianId()).isEqualTo(guardianId);
        assertThat(savedStepRecord.getStepCount()).isEqualTo(stepCount);
        assertThat(savedStepRecord.getRecordedAt().value()).isEqualTo(timestamp);
    }

    @Test
    void shouldFindStepRecordsByGuardianIdAndDate() {
        // Given
        GuardianId guardianId = GuardianId.of(1L);
        LocalDate date =
                LocalDate.now().minusDays(1); // Use yesterday to avoid future timestamp validation
        LocalDateTime timestamp1 = date.atTime(9, 0);
        LocalDateTime timestamp2 = date.atTime(15, 30);

        StepRecordEntity entity1 = new StepRecordEntity(guardianId.value(), 500, timestamp1);
        StepRecordEntity entity2 = new StepRecordEntity(guardianId.value(), 750, timestamp2);

        when(stepRecordJpaRepository.findByGuardianIdAndDate(guardianId.value(), date))
                .thenReturn(Arrays.asList(entity1, entity2));

        // When
        List<StepRecord> stepRecords = jpaStepRepository.findByGuardianIdAndDate(guardianId, date);

        // Then
        assertThat(stepRecords).hasSize(2);
        assertThat(stepRecords.get(0).getStepCount().value()).isEqualTo(500);
        assertThat(stepRecords.get(1).getStepCount().value()).isEqualTo(750);
    }
}

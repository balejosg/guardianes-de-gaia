package com.guardianes.walking.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.shared.domain.model.Timestamp;
import com.guardianes.testconfig.NoRedisTestConfiguration;
import com.guardianes.walking.domain.model.StepCount;
import com.guardianes.walking.domain.model.StepRecord;
import com.guardianes.walking.domain.repository.StepRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Import(NoRedisTestConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class StepPersistenceIntegrationTest {

    @Autowired private StepRepository stepRepository;

    @Test
    public void shouldPersistStepRecordToDatabase() {
        // Given
        GuardianId guardianId = GuardianId.of(1L);
        StepCount stepCount = StepCount.of(1000);
        LocalDateTime timestamp = LocalDateTime.now().minusMinutes(10);
        StepRecord stepRecord =
                StepRecord.createWithTimestamp(guardianId, stepCount, Timestamp.of(timestamp));

        // When
        StepRecord savedStepRecord = stepRepository.save(stepRecord);

        // Then
        assertThat(savedStepRecord).isNotNull();
        assertThat(savedStepRecord.getGuardianId()).isEqualTo(guardianId);
        assertThat(savedStepRecord.getStepCount()).isEqualTo(stepCount);
        assertThat(savedStepRecord.getRecordedAt().value()).isEqualTo(timestamp);

        // Verify persistence by retrieving from database
        List<StepRecord> retrievedRecords =
                stepRepository.findByGuardianIdAndDate(guardianId, timestamp.toLocalDate());
        assertThat(retrievedRecords).hasSize(1);
        assertThat(retrievedRecords.get(0)).isEqualTo(stepRecord);
    }

    @Test
    public void shouldRetrieveStepRecordsFromDatabase() {
        // Given
        GuardianId guardianId = GuardianId.of(2L);
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime timestamp1 = yesterday.atTime(9, 0);
        LocalDateTime timestamp2 = yesterday.atTime(15, 30);

        StepRecord record1 =
                StepRecord.createWithTimestamp(
                        guardianId, StepCount.of(500), Timestamp.of(timestamp1));
        StepRecord record2 =
                StepRecord.createWithTimestamp(
                        guardianId, StepCount.of(750), Timestamp.of(timestamp2));

        // When
        stepRepository.save(record1);
        stepRepository.save(record2);

        // Then
        List<StepRecord> retrievedRecords =
                stepRepository.findByGuardianIdAndDate(guardianId, yesterday);
        assertThat(retrievedRecords).hasSize(2);
        assertThat(retrievedRecords).containsExactlyInAnyOrder(record1, record2);
    }
}

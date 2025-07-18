package com.guardianes.walking.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.guardianes.testconfig.GuardianTestConfiguration;
import com.guardianes.walking.domain.StepRecord;
import com.guardianes.walking.domain.StepRepository;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(GuardianTestConfiguration.class)
public class StepPersistenceIntegrationTest {

  @Autowired private StepRepository stepRepository;

  @Test
  public void shouldPersistStepRecordToDatabase() {
    // Given
    Long guardianId = 1L;
    int stepCount = 1000;
    LocalDateTime timestamp = LocalDateTime.now();
    StepRecord stepRecord = new StepRecord(guardianId, stepCount, timestamp);

    // When
    StepRecord savedStepRecord = stepRepository.save(stepRecord);

    // Then
    assertThat(savedStepRecord).isNotNull();
    assertThat(savedStepRecord.getGuardianId()).isEqualTo(guardianId);
    assertThat(savedStepRecord.getStepCount()).isEqualTo(stepCount);
    assertThat(savedStepRecord.getTimestamp()).isEqualTo(timestamp);

    // Verify persistence by retrieving from database
    List<StepRecord> retrievedRecords =
        stepRepository.findByGuardianIdAndDate(guardianId, timestamp.toLocalDate());
    assertThat(retrievedRecords).hasSize(1);
    StepRecord retrievedRecord = retrievedRecords.get(0);
    assertThat(retrievedRecord.getGuardianId()).isEqualTo(guardianId);
    assertThat(retrievedRecord.getStepCount()).isEqualTo(stepCount);
    // Database may truncate microseconds, so we check to the second precision
    assertThat(retrievedRecord.getTimestamp()).isEqualToIgnoringNanos(timestamp);
  }

  @Test
  public void shouldRetrieveStepRecordsFromDatabase() {
    // Given
    Long guardianId = 2L;
    LocalDate today = LocalDate.now();
    LocalDateTime timestamp1 = today.atTime(9, 0);
    LocalDateTime timestamp2 = today.atTime(15, 30);

    StepRecord record1 = new StepRecord(guardianId, 500, timestamp1);
    StepRecord record2 = new StepRecord(guardianId, 750, timestamp2);

    // When
    stepRepository.save(record1);
    stepRepository.save(record2);

    // Then
    List<StepRecord> retrievedRecords = stepRepository.findByGuardianIdAndDate(guardianId, today);
    assertThat(retrievedRecords).hasSize(2);
    assertThat(retrievedRecords).containsExactlyInAnyOrder(record1, record2);
  }
}

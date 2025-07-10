package com.guardianes.walking.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.shared.domain.model.Timestamp;
import com.guardianes.walking.domain.model.Energy;
import com.guardianes.walking.domain.model.StepCount;
import com.guardianes.walking.domain.model.StepRecord;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("StepRecord Domain Object Tests")
class StepRecordTest {

    @Test
    @DisplayName("Should create step record with current timestamp")
    void shouldCreateStepRecordWithCurrentTimestamp() {
        // Given
        GuardianId guardianId = GuardianId.of(1L);
        StepCount stepCount = StepCount.of(1000);

        // When
        StepRecord stepRecord = StepRecord.create(guardianId, stepCount);

        // Then
        assertEquals(guardianId, stepRecord.getGuardianId());
        assertEquals(stepCount, stepRecord.getStepCount());
        assertNotNull(stepRecord.getRecordedAt());
    }

    @Test
    @DisplayName("Should create step record with specific timestamp")
    void shouldCreateStepRecordWithSpecificTimestamp() {
        // Given
        GuardianId guardianId = GuardianId.of(1L);
        StepCount stepCount = StepCount.of(1000);
        Timestamp timestamp = Timestamp.of(LocalDateTime.now().minusHours(1));

        // When
        StepRecord stepRecord = StepRecord.createWithTimestamp(guardianId, stepCount, timestamp);

        // Then
        assertEquals(guardianId, stepRecord.getGuardianId());
        assertEquals(stepCount, stepRecord.getStepCount());
        assertEquals(timestamp, stepRecord.getRecordedAt());
    }

    @Test
    @DisplayName("Should reject null guardian ID")
    void shouldRejectNullGuardianId() {
        // Given
        StepCount stepCount = StepCount.of(1000);

        // When & Then
        assertThrows(NullPointerException.class, () -> StepRecord.create(null, stepCount));
    }

    @Test
    @DisplayName("Should reject null step count")
    void shouldRejectNullStepCount() {
        // Given
        GuardianId guardianId = GuardianId.of(1L);

        // When & Then
        assertThrows(NullPointerException.class, () -> StepRecord.create(guardianId, null));
    }

    @Test
    @DisplayName("Should calculate energy generated correctly")
    void shouldCalculateEnergyGeneratedCorrectly() {
        // Given
        GuardianId guardianId = GuardianId.of(1L);
        StepCount stepCount = StepCount.of(100); // Should generate 10 energy
        StepRecord stepRecord = StepRecord.create(guardianId, stepCount);

        // When
        Energy energyGenerated = stepRecord.calculateEnergyGenerated();

        // Then
        assertEquals(Energy.of(10), energyGenerated);
    }

    @Test
    @DisplayName("Should detect when would exceed daily maximum")
    void shouldDetectWhenWouldExceedDailyMaximum() {
        // Given
        GuardianId guardianId = GuardianId.of(1L);
        StepCount stepCount = StepCount.of(10000);
        StepRecord stepRecord = StepRecord.create(guardianId, stepCount);
        StepCount currentDaily = StepCount.of(45000);

        // When
        boolean wouldExceed = stepRecord.wouldExceedDailyMaximum(currentDaily);

        // Then
        assertTrue(wouldExceed);
    }

    @Test
    @DisplayName("Should validate reasonable increment from previous record")
    void shouldValidateReasonableIncrementFromPreviousRecord() {
        // Given
        GuardianId guardianId = GuardianId.of(1L);
        Timestamp earlierTime = Timestamp.of(LocalDateTime.now().minusMinutes(10));
        StepRecord previousRecord =
                StepRecord.createWithTimestamp(guardianId, StepCount.of(1000), earlierTime);

        StepRecord currentRecord =
                StepRecord.create(guardianId, StepCount.of(1500)); // 500 steps in 10 minutes

        // When
        boolean isReasonable = currentRecord.isReasonableIncrementFrom(previousRecord);

        // Then
        assertTrue(
                isReasonable); // 500 steps in 10 minutes = 50 steps/minute < 200 steps/minute limit
    }

    @Test
    @DisplayName("Should detect unreasonable increment from previous record")
    void shouldDetectUnreasonableIncrementFromPreviousRecord() {
        // Given
        GuardianId guardianId = GuardianId.of(1L);
        Timestamp earlierTime = Timestamp.of(LocalDateTime.now().minusMinutes(1));
        StepRecord previousRecord =
                StepRecord.createWithTimestamp(guardianId, StepCount.of(1000), earlierTime);

        StepRecord currentRecord =
                StepRecord.create(guardianId, StepCount.of(1500)); // 500 steps in 1 minute

        // When
        boolean isReasonable = currentRecord.isReasonableIncrementFrom(previousRecord);

        // Then
        assertFalse(isReasonable); // 500 steps in 1 minute > 200 steps/minute limit
    }

    @Test
    @DisplayName("Should handle first record as always reasonable")
    void shouldHandleFirstRecordAsAlwaysReasonable() {
        // Given
        GuardianId guardianId = GuardianId.of(1L);
        StepRecord firstRecord = StepRecord.create(guardianId, StepCount.of(5000));

        // When
        boolean isReasonable = firstRecord.isReasonableIncrementFrom(null);

        // Then
        assertTrue(isReasonable);
    }

    @Test
    @DisplayName("Should provide backward compatibility with legacy getters")
    void shouldProvideBackwardCompatibilityWithLegacyGetters() {
        // Given
        GuardianId guardianId = GuardianId.of(123L);
        StepCount stepCount = StepCount.of(1000);
        StepRecord stepRecord = StepRecord.create(guardianId, stepCount);

        // When & Then
        assertEquals(123L, stepRecord.getGuardianId().value());
        assertEquals(1000, stepRecord.getStepCount().value());
        assertNotNull(stepRecord.getRecordedAt());
    }

    @Test
    @DisplayName("Should handle equality correctly")
    void shouldHandleEqualityCorrectly() {
        // Given
        GuardianId guardianId = GuardianId.of(1L);
        StepCount stepCount = StepCount.of(1000);
        Timestamp timestamp = Timestamp.of(LocalDateTime.now().minusHours(1));

        StepRecord first = StepRecord.createWithTimestamp(guardianId, stepCount, timestamp);
        StepRecord second = StepRecord.createWithTimestamp(guardianId, stepCount, timestamp);
        StepRecord different =
                StepRecord.createWithTimestamp(guardianId, StepCount.of(2000), timestamp);

        // When & Then
        assertEquals(first, second);
        assertNotEquals(first, different);
        assertEquals(first.hashCode(), second.hashCode());
    }
}

package com.guardianes.walking.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("StepCount Value Object Tests")
class StepCountTest {

    @Test
    @DisplayName("Should create valid step count")
    void shouldCreateValidStepCount() {
        // Given
        int validSteps = 1000;

        // When
        StepCount stepCount = StepCount.of(validSteps);

        // Then
        assertEquals(validSteps, stepCount.value());
    }

    @Test
    @DisplayName("Should create zero step count")
    void shouldCreateZeroStepCount() {
        // When
        StepCount stepCount = StepCount.zero();

        // Then
        assertEquals(0, stepCount.value());
        assertTrue(stepCount.isZero());
    }

    @Test
    @DisplayName("Should reject negative step count")
    void shouldRejectNegativeStepCount() {
        // Given
        int negativeSteps = -100;

        // When & Then
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> StepCount.of(negativeSteps));

        assertTrue(exception.getMessage().contains("cannot be negative"));
    }

    @Test
    @DisplayName("Should reject step count exceeding daily maximum")
    void shouldRejectStepCountExceedingDailyMaximum() {
        // Given
        int excessiveSteps = 60000; // More than 50,000 daily max

        // When & Then
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> StepCount.of(excessiveSteps));

        assertTrue(exception.getMessage().contains("exceeds daily maximum"));
    }

    @Test
    @DisplayName("Should add step counts correctly")
    void shouldAddStepCountsCorrectly() {
        // Given
        StepCount first = StepCount.of(1000);
        StepCount second = StepCount.of(2000);

        // When
        StepCount result = first.add(second);

        // Then
        assertEquals(3000, result.value());
    }

    @Test
    @DisplayName("Should detect when adding would exceed daily maximum")
    void shouldDetectWhenAddingWouldExceedDailyMaximum() {
        // Given
        StepCount currentDaily = StepCount.of(45000);
        StepCount newSteps = StepCount.of(6000);

        // When
        boolean wouldExceed = newSteps.wouldExceedDailyMaximum(currentDaily);

        // Then
        assertTrue(wouldExceed);
    }

    @Test
    @DisplayName("Should allow adding when within daily maximum")
    void shouldAllowAddingWhenWithinDailyMaximum() {
        // Given
        StepCount currentDaily = StepCount.of(40000);
        StepCount newSteps = StepCount.of(5000);

        // When
        boolean wouldExceed = newSteps.wouldExceedDailyMaximum(currentDaily);

        // Then
        assertFalse(wouldExceed);
    }

    @Test
    @DisplayName("Should compare step counts correctly")
    void shouldCompareStepCountsCorrectly() {
        // Given
        StepCount smaller = StepCount.of(1000);
        StepCount larger = StepCount.of(2000);

        // When & Then
        assertTrue(larger.isGreaterThan(smaller));
        assertFalse(smaller.isGreaterThan(larger));
        assertFalse(smaller.isGreaterThan(smaller));
    }

    @Test
    @DisplayName("Should handle equality correctly")
    void shouldHandleEqualityCorrectly() {
        // Given
        StepCount first = StepCount.of(1000);
        StepCount second = StepCount.of(1000);
        StepCount different = StepCount.of(2000);

        // When & Then
        assertEquals(first, second);
        assertNotEquals(first, different);
        assertEquals(first.hashCode(), second.hashCode());
    }
}

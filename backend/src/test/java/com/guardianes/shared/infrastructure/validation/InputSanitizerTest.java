package com.guardianes.shared.infrastructure.validation;

import static org.junit.jupiter.api.Assertions.*;

import com.guardianes.walking.application.dto.StepSubmissionRequest;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Input Sanitizer Tests")
class InputSanitizerTest {

    private InputSanitizer inputSanitizer;

    @BeforeEach
    void setUp() {
        inputSanitizer = new InputSanitizer();
    }

    @Test
    @DisplayName("Should sanitize step count within valid range")
    void shouldSanitizeStepCountWithinValidRange() {
        // Given
        int validStepCount = 5000;

        // When
        int result = inputSanitizer.sanitizeStepCount(validStepCount);

        // Then
        assertEquals(validStepCount, result);
    }

    @Test
    @DisplayName("Should set negative step count to zero")
    void shouldSetNegativeStepCountToZero() {
        // Given
        int negativeStepCount = -100;

        // When
        int result = inputSanitizer.sanitizeStepCount(negativeStepCount);

        // Then
        assertEquals(0, result);
    }

    @Test
    @DisplayName("Should cap excessive step count to maximum")
    void shouldCapExcessiveStepCountToMaximum() {
        // Given
        int excessiveStepCount = 100_000;

        // When
        int result = inputSanitizer.sanitizeStepCount(excessiveStepCount);

        // Then
        assertEquals(50_000, result);
    }

    @Test
    @DisplayName("Should sanitize valid timestamp unchanged")
    void shouldSanitizeValidTimestampUnchanged() {
        // Given
        LocalDateTime validTimestamp = LocalDateTime.now().minusMinutes(30);

        // When
        LocalDateTime result = inputSanitizer.sanitizeTimestamp(validTimestamp);

        // Then
        assertEquals(validTimestamp, result);
    }

    @Test
    @DisplayName("Should replace null timestamp with current time")
    void shouldReplaceNullTimestampWithCurrentTime() {
        // Given
        LocalDateTime nullTimestamp = null;
        LocalDateTime before = LocalDateTime.now();

        // When
        LocalDateTime result = inputSanitizer.sanitizeTimestamp(nullTimestamp);

        // Then
        LocalDateTime after = LocalDateTime.now();
        assertNotNull(result);
        assertTrue(result.isAfter(before.minusSeconds(1)));
        assertTrue(result.isBefore(after.plusSeconds(1)));
    }

    @Test
    @DisplayName("Should adjust future timestamp to current time")
    void shouldAdjustFutureTimestampToCurrentTime() {
        // Given
        LocalDateTime futureTimestamp = LocalDateTime.now().plusHours(1);
        LocalDateTime before = LocalDateTime.now();

        // When
        LocalDateTime result = inputSanitizer.sanitizeTimestamp(futureTimestamp);

        // Then
        LocalDateTime after = LocalDateTime.now();
        assertTrue(result.isAfter(before.minusSeconds(1)));
        assertTrue(result.isBefore(after.plusSeconds(1)));
    }

    @Test
    @DisplayName("Should adjust very old timestamp")
    void shouldAdjustVeryOldTimestamp() {
        // Given
        LocalDateTime oldTimestamp = LocalDateTime.now().minusDays(2);

        // When
        LocalDateTime result = inputSanitizer.sanitizeTimestamp(oldTimestamp);

        // Then
        LocalDateTime expected = LocalDateTime.now().minusHours(24);
        assertTrue(result.isAfter(expected.minusMinutes(1)));
        assertTrue(result.isBefore(expected.plusMinutes(1)));
    }

    @Test
    @DisplayName("Should sanitize complete step submission request")
    void shouldSanitizeCompleteStepSubmissionRequest() {
        // Given
        StepSubmissionRequest request =
                new StepSubmissionRequest(
                        -500, // Invalid negative step count
                        LocalDateTime.now().plusHours(2) // Invalid future timestamp
                        );

        // When
        StepSubmissionRequest result = inputSanitizer.sanitizeStepSubmission(request);

        // Then
        assertEquals(0, result.stepCount());
        assertTrue(result.timestamp().isBefore(LocalDateTime.now().plusMinutes(1)));
    }

    @Test
    @DisplayName("Should validate guardian ID correctly")
    void shouldValidateGuardianIdCorrectly() {
        // Given
        Long validGuardianId = 123L;

        // When
        Long result = inputSanitizer.sanitizeGuardianId(validGuardianId);

        // Then
        assertEquals(validGuardianId, result);
    }

    @Test
    @DisplayName("Should reject null guardian ID")
    void shouldRejectNullGuardianId() {
        // Given
        Long nullGuardianId = null;

        // When & Then
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    inputSanitizer.sanitizeGuardianId(nullGuardianId);
                });
    }

    @Test
    @DisplayName("Should reject negative guardian ID")
    void shouldRejectNegativeGuardianId() {
        // Given
        Long negativeGuardianId = -1L;

        // When & Then
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    inputSanitizer.sanitizeGuardianId(negativeGuardianId);
                });
    }

    @Test
    @DisplayName("Should sanitize string input by removing dangerous characters")
    void shouldSanitizeStringInputByRemovingDangerousCharacters() {
        // Given
        String dangerousInput = "<script>alert('xss')</script>";

        // When
        String result = inputSanitizer.sanitizeString(dangerousInput);

        // Then
        assertEquals("scriptalertxss/script", result);
    }

    @Test
    @DisplayName("Should truncate overly long strings")
    void shouldTruncateOverlyLongStrings() {
        // Given
        String longString = "a".repeat(1500);

        // When
        String result = inputSanitizer.sanitizeString(longString);

        // Then
        assertEquals(1000, result.length());
    }

    @Test
    @DisplayName("Should validate reasonable step count")
    void shouldValidateReasonableStepCount() {
        // Given
        int reasonableStepCount = 10000;
        int unreasonableStepCount = 60000;

        // When & Then
        assertTrue(inputSanitizer.isReasonableStepCount(reasonableStepCount));
        assertFalse(inputSanitizer.isReasonableStepCount(unreasonableStepCount));
    }

    @Test
    @DisplayName("Should validate reasonable timestamp")
    void shouldValidateReasonableTimestamp() {
        // Given
        LocalDateTime reasonableTimestamp = LocalDateTime.now().minusMinutes(30);
        LocalDateTime unreasonableTimestamp = LocalDateTime.now().minusDays(2);

        // When & Then
        assertTrue(inputSanitizer.isReasonableTimestamp(reasonableTimestamp));
        assertFalse(inputSanitizer.isReasonableTimestamp(unreasonableTimestamp));
    }
}

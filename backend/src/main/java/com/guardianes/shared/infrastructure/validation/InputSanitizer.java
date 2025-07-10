package com.guardianes.shared.infrastructure.validation;

import com.guardianes.walking.application.dto.StepSubmissionRequest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Input sanitization service for cleaning and validating user inputs. Provides protection against
 * malicious or malformed data.
 */
@Component
public class InputSanitizer {

    private static final Logger logger = LoggerFactory.getLogger(InputSanitizer.class);

    // Business constants
    private static final int MIN_STEP_COUNT = 0;
    private static final int MAX_STEP_COUNT = 50_000; // Daily maximum
    private static final int MAX_FUTURE_MINUTES = 5; // Allow 5 minutes in future for clock skew
    private static final int MAX_PAST_HOURS = 24; // Don't allow submissions older than 24 hours

    /** Sanitizes step submission requests to ensure data integrity */
    public StepSubmissionRequest sanitizeStepSubmission(StepSubmissionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Step submission request cannot be null");
        }

        int sanitizedStepCount = sanitizeStepCount(request.stepCount());
        LocalDateTime sanitizedTimestamp = sanitizeTimestamp(request.timestamp());

        // Log if any sanitization occurred
        if (sanitizedStepCount != request.stepCount()
                || !sanitizedTimestamp.equals(request.timestamp())) {
            logger.warn(
                    "Input sanitization applied - Original: steps={}, timestamp={}; Sanitized: steps={}, timestamp={}",
                    request.stepCount(),
                    request.timestamp(),
                    sanitizedStepCount,
                    sanitizedTimestamp);
        }

        return new StepSubmissionRequest(sanitizedStepCount, sanitizedTimestamp);
    }

    /** Sanitizes step count to ensure it's within reasonable bounds */
    public int sanitizeStepCount(int stepCount) {
        if (stepCount < MIN_STEP_COUNT) {
            logger.warn("Step count {} is below minimum, setting to {}", stepCount, MIN_STEP_COUNT);
            return MIN_STEP_COUNT;
        }

        if (stepCount > MAX_STEP_COUNT) {
            logger.warn("Step count {} exceeds maximum, setting to {}", stepCount, MAX_STEP_COUNT);
            return MAX_STEP_COUNT;
        }

        return stepCount;
    }

    /** Sanitizes timestamp to ensure it's within reasonable time bounds */
    public LocalDateTime sanitizeTimestamp(LocalDateTime timestamp) {
        if (timestamp == null) {
            logger.warn("Null timestamp provided, using current time");
            return LocalDateTime.now();
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maxFuture = now.plusMinutes(MAX_FUTURE_MINUTES);
        LocalDateTime maxPast = now.minusHours(MAX_PAST_HOURS);

        // Check if timestamp is too far in the future
        if (timestamp.isAfter(maxFuture)) {
            logger.warn("Timestamp {} is too far in future, setting to current time", timestamp);
            return now;
        }

        // Check if timestamp is too far in the past
        if (timestamp.isBefore(maxPast)) {
            logger.warn(
                    "Timestamp {} is too far in past, setting to {} hours ago",
                    timestamp,
                    MAX_PAST_HOURS);
            return maxPast;
        }

        return timestamp;
    }

    /** Sanitizes string inputs to prevent injection attacks */
    public String sanitizeString(String input) {
        if (input == null) {
            return null;
        }

        // Remove potentially dangerous characters
        String sanitized =
                input.replaceAll("[<>\"'%;()&+]", "") // Remove common injection characters
                        .trim();

        // Limit length to prevent DoS attacks
        if (sanitized.length() > 1000) {
            sanitized = sanitized.substring(0, 1000);
            logger.warn("String input truncated to 1000 characters");
        }

        return sanitized;
    }

    /** Validates if a guardian ID is in acceptable format */
    public Long sanitizeGuardianId(Long guardianId) {
        if (guardianId == null) {
            throw new IllegalArgumentException("Guardian ID cannot be null");
        }

        if (guardianId <= 0) {
            throw new IllegalArgumentException("Guardian ID must be positive");
        }

        // Check for unreasonably large IDs (potential overflow attacks)
        if (guardianId > Long.MAX_VALUE / 2) {
            throw new IllegalArgumentException("Guardian ID is too large");
        }

        return guardianId;
    }

    /** Sanitizes numeric inputs to prevent overflow attacks */
    public int sanitizeNumericInput(int value, int min, int max, String fieldName) {
        if (value < min) {
            logger.warn(
                    "{} value {} is below minimum {}, setting to minimum", fieldName, value, min);
            return min;
        }

        if (value > max) {
            logger.warn(
                    "{} value {} exceeds maximum {}, setting to maximum", fieldName, value, max);
            return max;
        }

        return value;
    }

    /** Validates that a timestamp is reasonable for step submission */
    public boolean isReasonableTimestamp(LocalDateTime timestamp) {
        if (timestamp == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        long minutesFromNow = ChronoUnit.MINUTES.between(timestamp, now);

        // Allow some future time for clock skew, but not too much past time
        return minutesFromNow >= -MAX_FUTURE_MINUTES && minutesFromNow <= (MAX_PAST_HOURS * 60);
    }

    /** Validates that step count is reasonable for a single submission */
    public boolean isReasonableStepCount(int stepCount) {
        return stepCount >= MIN_STEP_COUNT && stepCount <= MAX_STEP_COUNT;
    }
}

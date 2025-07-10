package com.guardianes.walking.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Value object representing a step count with built-in validation. Ensures step counts are always
 * within valid ranges.
 */
public record StepCount(int value) {

    private static final int MIN_STEPS = 0;
    private static final int MAX_DAILY_STEPS = 50_000;

    public StepCount {
        if (value < MIN_STEPS) {
            throw new IllegalArgumentException(
                    String.format("Step count cannot be negative, got: %d", value));
        }
        if (value > MAX_DAILY_STEPS) {
            throw new IllegalArgumentException(
                    String.format(
                            "Step count exceeds daily maximum (%d), got: %d",
                            MAX_DAILY_STEPS, value));
        }
    }

    public static StepCount of(int value) {
        return new StepCount(value);
    }

    public static StepCount zero() {
        return new StepCount(0);
    }

    public StepCount add(StepCount other) {
        return new StepCount(this.value + other.value);
    }

    public boolean isGreaterThan(StepCount other) {
        return this.value > other.value;
    }

    @JsonIgnore
    public boolean isZero() {
        return this.value == 0;
    }

    /** Validates if adding this step count to a daily total would exceed the maximum. */
    public boolean wouldExceedDailyMaximum(StepCount currentDailyTotal) {
        return (currentDailyTotal.value + this.value) > MAX_DAILY_STEPS;
    }

    @Override
    public String toString() {
        return "StepCount{" + value + "}";
    }
}

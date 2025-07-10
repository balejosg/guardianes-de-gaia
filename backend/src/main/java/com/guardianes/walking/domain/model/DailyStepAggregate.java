package com.guardianes.walking.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.guardianes.shared.domain.model.GuardianId;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Rich domain object representing daily step aggregation with business logic. Encapsulates the
 * business rules for daily step tracking and energy calculation.
 */
public class DailyStepAggregate {
    private final GuardianId guardianId;
    private final LocalDate date;
    private final StepCount totalSteps;

    @JsonCreator
    private DailyStepAggregate(
            @JsonProperty("guardianId") GuardianId guardianId,
            @JsonProperty("date") LocalDate date,
            @JsonProperty("totalSteps") StepCount totalSteps) {
        this.guardianId = guardianId;
        this.date = date;
        this.totalSteps = totalSteps;
    }

    /** Creates a new daily step aggregate. */
    public static DailyStepAggregate create(
            GuardianId guardianId, LocalDate date, StepCount totalSteps) {
        Objects.requireNonNull(guardianId, "Guardian ID cannot be null");
        Objects.requireNonNull(date, "Date cannot be null");
        Objects.requireNonNull(totalSteps, "Total steps cannot be null");

        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot create aggregate for future date: " + date);
        }

        return new DailyStepAggregate(guardianId, date, totalSteps);
    }

    /** Creates an empty daily aggregate (zero steps). */
    public static DailyStepAggregate empty(GuardianId guardianId, LocalDate date) {
        return create(guardianId, date, StepCount.zero());
    }

    /** Calculates the total energy generated for this day. */
    public Energy calculateTotalEnergyGenerated() {
        return Energy.fromSteps(this.totalSteps);
    }

    /** Adds steps to this aggregate, returning a new aggregate. */
    public DailyStepAggregate addSteps(StepCount additionalSteps) {
        StepCount newTotal = this.totalSteps.add(additionalSteps);
        return new DailyStepAggregate(this.guardianId, this.date, newTotal);
    }

    /** Checks if this aggregate represents today's data. */
    @JsonIgnore
    public boolean isToday() {
        return this.date.equals(LocalDate.now());
    }

    /** Checks if the guardian has been active (has any steps) on this day. */
    public boolean hasActivity() {
        return !this.totalSteps.isZero();
    }

    /** Checks if the daily step goal has been reached. Default goal is 10,000 steps per day. */
    public boolean hasReachedDailyGoal() {
        return hasReachedGoal(StepCount.of(10_000));
    }

    /** Checks if a specific step goal has been reached. */
    public boolean hasReachedGoal(StepCount goal) {
        return this.totalSteps.isGreaterThan(goal) || this.totalSteps.equals(goal);
    }

    // Getters
    public GuardianId getGuardianId() {
        return guardianId;
    }

    public LocalDate getDate() {
        return date;
    }

    public StepCount getTotalSteps() {
        return totalSteps;
    }

    // Legacy getters for backward compatibility (to be removed after migration)
    @Deprecated
    @JsonIgnore
    public Long getGuardianIdValue() {
        return guardianId.value();
    }

    @Deprecated
    @JsonIgnore
    public int getTotalStepsValue() {
        return totalSteps.value();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DailyStepAggregate that = (DailyStepAggregate) o;
        return Objects.equals(guardianId, that.guardianId)
                && Objects.equals(date, that.date)
                && Objects.equals(totalSteps, that.totalSteps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(guardianId, date, totalSteps);
    }

    @Override
    public String toString() {
        return "DailyStepAggregate{"
                + "guardianId="
                + guardianId
                + ", date="
                + date
                + ", totalSteps="
                + totalSteps
                + '}';
    }
}

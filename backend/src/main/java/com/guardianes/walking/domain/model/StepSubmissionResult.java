package com.guardianes.walking.domain.model;

import java.util.Objects;

/**
 * Value object representing the result of a step submission operation. Encapsulates all the domain
 * objects created during step submission.
 */
public class StepSubmissionResult {
    private final StepRecord stepRecord;
    private final EnergyTransaction energyTransaction;
    private final DailyStepAggregate updatedDailyAggregate;
    private final Energy energyGenerated;

    private StepSubmissionResult(
            StepRecord stepRecord,
            EnergyTransaction energyTransaction,
            DailyStepAggregate updatedDailyAggregate,
            Energy energyGenerated) {
        this.stepRecord = stepRecord;
        this.energyTransaction = energyTransaction;
        this.updatedDailyAggregate = updatedDailyAggregate;
        this.energyGenerated = energyGenerated;
    }

    /** Creates a successful step submission result. */
    public static StepSubmissionResult success(
            StepRecord stepRecord,
            EnergyTransaction energyTransaction,
            DailyStepAggregate updatedDailyAggregate) {
        Objects.requireNonNull(stepRecord, "Step record cannot be null");
        Objects.requireNonNull(energyTransaction, "Energy transaction cannot be null");
        Objects.requireNonNull(updatedDailyAggregate, "Updated daily aggregate cannot be null");

        Energy energyGenerated = stepRecord.calculateEnergyGenerated();

        return new StepSubmissionResult(
                stepRecord, energyTransaction, updatedDailyAggregate, energyGenerated);
    }

    /** Checks if the submission resulted in reaching the daily goal. */
    public boolean hasReachedDailyGoal() {
        return updatedDailyAggregate.hasReachedDailyGoal();
    }

    /** Checks if this is the first activity of the day. */
    public boolean isFirstActivityOfDay() {
        StepCount previousTotal =
                updatedDailyAggregate
                        .getTotalSteps()
                        .add(
                                stepRecord
                                        .getStepCount()
                                        .add(StepCount.of(-stepRecord.getStepCount().value())));
        return previousTotal.isZero();
    }

    // Getters
    public StepRecord getStepRecord() {
        return stepRecord;
    }

    public EnergyTransaction getEnergyTransaction() {
        return energyTransaction;
    }

    public DailyStepAggregate getUpdatedDailyAggregate() {
        return updatedDailyAggregate;
    }

    public Energy getEnergyGenerated() {
        return energyGenerated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StepSubmissionResult that = (StepSubmissionResult) o;
        return Objects.equals(stepRecord, that.stepRecord)
                && Objects.equals(energyTransaction, that.energyTransaction)
                && Objects.equals(updatedDailyAggregate, that.updatedDailyAggregate)
                && Objects.equals(energyGenerated, that.energyGenerated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stepRecord, energyTransaction, updatedDailyAggregate, energyGenerated);
    }

    @Override
    public String toString() {
        return "StepSubmissionResult{"
                + "stepRecord="
                + stepRecord
                + ", energyTransaction="
                + energyTransaction
                + ", updatedDailyAggregate="
                + updatedDailyAggregate
                + ", energyGenerated="
                + energyGenerated
                + '}';
    }
}

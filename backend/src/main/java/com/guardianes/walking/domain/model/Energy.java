package com.guardianes.walking.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Value object representing energy in the game system. Handles energy calculations and validation.
 */
public record Energy(int amount) {

    private static final int STEPS_PER_ENERGY = 10;
    private static final int MIN_ENERGY = 0;

    public Energy {
        if (amount < MIN_ENERGY) {
            throw new IllegalArgumentException(
                    String.format("Energy amount cannot be negative, got: %d", amount));
        }
    }

    public static Energy of(int amount) {
        return new Energy(amount);
    }

    public static Energy zero() {
        return new Energy(0);
    }

    /** Converts step count to energy using the game's conversion rate. 1 energy = 10 steps */
    public static Energy fromSteps(StepCount steps) {
        return new Energy(steps.value() / STEPS_PER_ENERGY);
    }

    public Energy add(Energy other) {
        return new Energy(this.amount + other.amount);
    }

    public Energy subtract(Energy other) {
        if (this.amount < other.amount) {
            throw new InsufficientEnergyException(
                    String.format(
                            "Cannot subtract %d energy from %d (insufficient energy)",
                            other.amount, this.amount));
        }
        return new Energy(this.amount - other.amount);
    }

    public int negateValue() {
        return -this.amount;
    }

    public boolean isGreaterThanOrEqual(Energy other) {
        return this.amount >= other.amount;
    }

    @JsonIgnore
    public boolean isZero() {
        return this.amount == 0;
    }

    public boolean isSufficientFor(Energy required) {
        return this.amount >= required.amount;
    }

    @Override
    public String toString() {
        return "Energy{" + amount + "}";
    }
}

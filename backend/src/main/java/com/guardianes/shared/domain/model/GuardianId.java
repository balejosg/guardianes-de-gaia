package com.guardianes.shared.domain.model;

/**
 * Value object representing a Guardian's unique identifier. Ensures type safety and validation for
 * Guardian IDs throughout the system.
 */
public record GuardianId(Long value) {

    public GuardianId {
        if (value == null) {
            throw new IllegalArgumentException("Guardian ID cannot be null");
        }
        if (value <= 0) {
            throw new IllegalArgumentException("Guardian ID must be positive, got: " + value);
        }
    }

    public static GuardianId of(Long value) {
        return new GuardianId(value);
    }

    public static GuardianId of(String value) {
        try {
            return new GuardianId(Long.valueOf(value));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Guardian ID format: " + value, e);
        }
    }

    @Override
    public String toString() {
        return "GuardianId{" + value + "}";
    }
}

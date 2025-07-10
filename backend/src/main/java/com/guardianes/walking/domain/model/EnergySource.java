package com.guardianes.walking.domain.model;

/** Value object representing the source of energy transactions. */
public record EnergySource(String name) {

    public EnergySource {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Energy source name cannot be null or empty");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("Energy source name cannot exceed 50 characters");
        }
    }

    public static EnergySource of(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Energy source name cannot be null or empty");
        }
        return new EnergySource(name.trim());
    }

    // Common energy sources
    public static EnergySource steps() {
        return new EnergySource("STEPS");
    }

    public static EnergySource battle() {
        return new EnergySource("BATTLE");
    }

    public static EnergySource challenge() {
        return new EnergySource("CHALLENGE");
    }

    public static EnergySource bonus() {
        return new EnergySource("BONUS");
    }

    @Override
    public String toString() {
        return "EnergySource{" + name + "}";
    }
}

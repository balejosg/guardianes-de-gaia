package com.guardianes.walking.domain.model;

/** Domain exception thrown when attempting to spend more energy than available. */
public class InsufficientEnergyException extends RuntimeException {

    public InsufficientEnergyException(String message) {
        super(message);
    }

    public InsufficientEnergyException(String message, Throwable cause) {
        super(message, cause);
    }
}

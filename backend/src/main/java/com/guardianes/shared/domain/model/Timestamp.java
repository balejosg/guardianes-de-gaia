package com.guardianes.shared.domain.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/** Value object representing a timestamp with domain-specific validation. */
public record Timestamp(LocalDateTime value) {

    public Timestamp {
        if (value == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }
        if (value.isAfter(LocalDateTime.now().plusMinutes(5))) {
            throw new IllegalArgumentException("Timestamp cannot be in the future");
        }
        if (value.isBefore(LocalDateTime.now().minusDays(7))) {
            throw new IllegalArgumentException("Timestamp cannot be older than 7 days");
        }
    }

    public static Timestamp now() {
        return new Timestamp(LocalDateTime.now());
    }

    public static Timestamp of(LocalDateTime dateTime) {
        return new Timestamp(dateTime);
    }

    public boolean isAfter(Timestamp other) {
        return this.value.isAfter(other.value);
    }

    public boolean isBefore(Timestamp other) {
        return this.value.isBefore(other.value);
    }

    public long minutesBetween(Timestamp other) {
        return ChronoUnit.MINUTES.between(other.value, this.value);
    }

    public Timestamp minusHours(long hours) {
        return new Timestamp(this.value.minusHours(hours));
    }

    @Override
    public String toString() {
        return "Timestamp{" + value + "}";
    }
}

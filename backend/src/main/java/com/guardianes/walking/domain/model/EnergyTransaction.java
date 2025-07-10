package com.guardianes.walking.domain.model;

import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.shared.domain.model.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Rich domain object representing an energy transaction with business logic. Encapsulates the
 * business rules for energy earning and spending.
 */
public class EnergyTransaction {
    private final GuardianId guardianId;
    private final EnergyTransactionType type;
    private final Energy amount;
    private final EnergySource source;
    private final Timestamp occurredAt;

    private EnergyTransaction(
            GuardianId guardianId,
            EnergyTransactionType type,
            Energy amount,
            EnergySource source,
            Timestamp occurredAt) {
        this.guardianId = guardianId;
        this.type = type;
        this.amount = amount;
        this.source = source;
        this.occurredAt = occurredAt;
    }

    /** Creates an energy earning transaction. */
    public static EnergyTransaction earned(
            GuardianId guardianId, Energy amount, EnergySource source) {
        Objects.requireNonNull(guardianId, "Guardian ID cannot be null");
        Objects.requireNonNull(amount, "Energy amount cannot be null");
        Objects.requireNonNull(source, "Energy source cannot be null");

        if (amount.isZero()) {
            throw new IllegalArgumentException("Cannot create transaction for zero energy");
        }

        return new EnergyTransaction(
                guardianId, EnergyTransactionType.EARNED, amount, source, Timestamp.now());
    }

    /** Creates an energy spending transaction. */
    public static EnergyTransaction spent(
            GuardianId guardianId, Energy amount, EnergySource source) {
        Objects.requireNonNull(guardianId, "Guardian ID cannot be null");
        Objects.requireNonNull(amount, "Energy amount cannot be null");
        Objects.requireNonNull(source, "Energy source cannot be null");

        if (amount.isZero()) {
            throw new IllegalArgumentException("Cannot create transaction for zero energy");
        }

        return new EnergyTransaction(
                guardianId, EnergyTransactionType.SPENT, amount, source, Timestamp.now());
    }

    /** Creates a transaction with specific timestamp (for testing or data migration). */
    public static EnergyTransaction createWithTimestamp(
            GuardianId guardianId,
            EnergyTransactionType type,
            Energy amount,
            EnergySource source,
            Timestamp timestamp) {
        Objects.requireNonNull(guardianId, "Guardian ID cannot be null");
        Objects.requireNonNull(type, "Transaction type cannot be null");
        Objects.requireNonNull(amount, "Energy amount cannot be null");
        Objects.requireNonNull(source, "Energy source cannot be null");
        Objects.requireNonNull(timestamp, "Timestamp cannot be null");

        if (amount.isZero()) {
            throw new IllegalArgumentException("Cannot create transaction for zero energy");
        }

        return new EnergyTransaction(guardianId, type, amount, source, timestamp);
    }

    /** Returns the signed energy amount as an integer (positive for earned, negative for spent). */
    public int getSignedAmountValue() {
        return type == EnergyTransactionType.EARNED ? amount.amount() : -amount.amount();
    }

    /** Checks if this transaction represents energy earning. */
    public boolean isEarning() {
        return type == EnergyTransactionType.EARNED;
    }

    /** Checks if this transaction represents energy spending. */
    public boolean isSpending() {
        return type == EnergyTransactionType.SPENT;
    }

    /** Checks if this transaction is from steps. */
    public boolean isFromSteps() {
        return EnergySource.steps().equals(source);
    }

    // Getters
    public GuardianId getGuardianId() {
        return guardianId;
    }

    public EnergyTransactionType getType() {
        return type;
    }

    public Energy getAmount() {
        return amount;
    }

    public EnergySource getSource() {
        return source;
    }

    public Timestamp getOccurredAt() {
        return occurredAt;
    }

    // Legacy getters for backward compatibility (to be removed after migration)
    @Deprecated
    public Long getGuardianIdValue() {
        return guardianId.value();
    }

    @Deprecated
    public int getAmountValue() {
        return amount.amount();
    }

    @Deprecated
    public String getSourceName() {
        return source.name();
    }

    @Deprecated
    public LocalDateTime getTimestamp() {
        return occurredAt.value();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnergyTransaction that = (EnergyTransaction) o;
        return Objects.equals(guardianId, that.guardianId)
                && type == that.type
                && Objects.equals(amount, that.amount)
                && Objects.equals(source, that.source)
                && Objects.equals(occurredAt, that.occurredAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(guardianId, type, amount, source, occurredAt);
    }

    @Override
    public String toString() {
        return "EnergyTransaction{"
                + "guardianId="
                + guardianId
                + ", type="
                + type
                + ", amount="
                + amount
                + ", source="
                + source
                + ", occurredAt="
                + occurredAt
                + '}';
    }
}

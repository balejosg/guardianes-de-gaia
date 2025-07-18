package com.guardianes.walking.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class EnergyTransaction {
  private Long guardianId;
  private EnergyTransactionType type;
  private int amount;
  private String source;
  private LocalDateTime timestamp;

  public EnergyTransaction(
      Long guardianId,
      EnergyTransactionType type,
      int amount,
      String source,
      LocalDateTime timestamp) {
    this.guardianId = guardianId;
    this.type = type;
    this.amount = amount;
    this.source = source;
    this.timestamp = timestamp;
  }

  public Long getGuardianId() {
    return guardianId;
  }

  public EnergyTransactionType getType() {
    return type;
  }

  public int getAmount() {
    return amount;
  }

  public String getSource() {
    return source;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    EnergyTransaction that = (EnergyTransaction) o;
    return amount == that.amount
        && Objects.equals(guardianId, that.guardianId)
        && type == that.type
        && Objects.equals(source, that.source)
        && Objects.equals(timestamp, that.timestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(guardianId, type, amount, source, timestamp);
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
        + ", source='"
        + source
        + '\''
        + ", timestamp="
        + timestamp
        + '}';
  }
}

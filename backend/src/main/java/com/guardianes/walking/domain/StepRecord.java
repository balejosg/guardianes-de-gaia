package com.guardianes.walking.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class StepRecord {
  private Long guardianId;
  private int stepCount;
  private LocalDateTime timestamp;

  public StepRecord(Long guardianId, int stepCount, LocalDateTime timestamp) {
    this.guardianId = guardianId;
    this.stepCount = stepCount;
    this.timestamp = timestamp;
  }

  public Long getGuardianId() {
    return guardianId;
  }

  public int getStepCount() {
    return stepCount;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    StepRecord that = (StepRecord) o;
    return stepCount == that.stepCount
        && Objects.equals(guardianId, that.guardianId)
        && Objects.equals(timestamp, that.timestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(guardianId, stepCount, timestamp);
  }

  @Override
  public String toString() {
    return "StepRecord{"
        + "guardianId="
        + guardianId
        + ", stepCount="
        + stepCount
        + ", timestamp="
        + timestamp
        + '}';
  }
}

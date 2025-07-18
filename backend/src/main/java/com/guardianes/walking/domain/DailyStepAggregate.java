package com.guardianes.walking.domain;

import java.time.LocalDate;
import java.util.Objects;

public class DailyStepAggregate {
  private Long guardianId;
  private LocalDate date;
  private int totalSteps;

  public DailyStepAggregate(Long guardianId, LocalDate date, int totalSteps) {
    this.guardianId = guardianId;
    this.date = date;
    this.totalSteps = totalSteps;
  }

  public Long getGuardianId() {
    return guardianId;
  }

  public LocalDate getDate() {
    return date;
  }

  public int getTotalSteps() {
    return totalSteps;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DailyStepAggregate that = (DailyStepAggregate) o;
    return totalSteps == that.totalSteps
        && Objects.equals(guardianId, that.guardianId)
        && Objects.equals(date, that.date);
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

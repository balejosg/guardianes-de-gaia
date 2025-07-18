package com.guardianes.walking.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(
    name = "daily_step_aggregates",
    uniqueConstraints = @UniqueConstraint(columnNames = {"guardian_id", "date"}),
    indexes = {@Index(name = "idx_daily_steps_guardian_date", columnList = "guardian_id, date")})
public class DailyStepAggregateEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "guardian_id", nullable = false)
  private Long guardianId;

  @Column(name = "date", nullable = false)
  private LocalDate date;

  @Column(name = "total_steps", nullable = false)
  private Integer totalSteps;

  protected DailyStepAggregateEntity() {
    // JPA requires default constructor
  }

  public DailyStepAggregateEntity(Long guardianId, LocalDate date, Integer totalSteps) {
    this.guardianId = guardianId;
    this.date = date;
    this.totalSteps = totalSteps;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getGuardianId() {
    return guardianId;
  }

  public void setGuardianId(Long guardianId) {
    this.guardianId = guardianId;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public Integer getTotalSteps() {
    return totalSteps;
  }

  public void setTotalSteps(Integer totalSteps) {
    this.totalSteps = totalSteps;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DailyStepAggregateEntity that = (DailyStepAggregateEntity) o;
    return Objects.equals(guardianId, that.guardianId)
        && Objects.equals(date, that.date)
        && Objects.equals(totalSteps, that.totalSteps);
  }

  @Override
  public int hashCode() {
    return Objects.hash(guardianId, date, totalSteps);
  }

  @Override
  public String toString() {
    return "DailyStepAggregateEntity{"
        + "id="
        + id
        + ", guardianId="
        + guardianId
        + ", date="
        + date
        + ", totalSteps="
        + totalSteps
        + '}';
  }
}

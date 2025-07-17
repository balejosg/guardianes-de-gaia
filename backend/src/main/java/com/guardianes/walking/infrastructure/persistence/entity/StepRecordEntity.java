package com.guardianes.walking.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "step_records", indexes = {
    @Index(name = "idx_step_records_guardian_date", columnList = "guardianId, recorded_at"),
    @Index(name = "idx_step_records_guardian_recorded_at", columnList = "guardianId, recorded_at")
})
public class StepRecordEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "guardian_id", nullable = false)
    private Long guardianId;
    
    @Column(name = "step_count", nullable = false)
    private Integer stepCount;
    
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime timestamp;
    
    protected StepRecordEntity() {
        // JPA requires default constructor
    }
    
    public StepRecordEntity(Long guardianId, Integer stepCount, LocalDateTime timestamp) {
        this.guardianId = guardianId;
        this.stepCount = stepCount;
        this.timestamp = timestamp;
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
    
    public Integer getStepCount() {
        return stepCount;
    }
    
    public void setStepCount(Integer stepCount) {
        this.stepCount = stepCount;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StepRecordEntity that = (StepRecordEntity) o;
        return Objects.equals(guardianId, that.guardianId) &&
               Objects.equals(stepCount, that.stepCount) &&
               Objects.equals(timestamp, that.timestamp);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(guardianId, stepCount, timestamp);
    }
    
    @Override
    public String toString() {
        return "StepRecordEntity{" +
               "id=" + id +
               ", guardianId=" + guardianId +
               ", stepCount=" + stepCount +
               ", timestamp=" + timestamp +
               '}';
    }
}
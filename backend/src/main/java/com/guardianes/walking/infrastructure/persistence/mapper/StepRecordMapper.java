package com.guardianes.walking.infrastructure.persistence.mapper;

import com.guardianes.walking.domain.StepRecord;
import com.guardianes.walking.infrastructure.persistence.entity.StepRecordEntity;
import org.springframework.stereotype.Component;

@Component
public class StepRecordMapper {

  public StepRecordEntity toEntity(StepRecord domain) {
    if (domain == null) {
      return null;
    }
    return new StepRecordEntity(
        domain.getGuardianId(), domain.getStepCount(), domain.getTimestamp());
  }

  public StepRecord toDomain(StepRecordEntity entity) {
    if (entity == null) {
      return null;
    }
    return new StepRecord(entity.getGuardianId(), entity.getStepCount(), entity.getTimestamp());
  }
}

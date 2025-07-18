package com.guardianes.walking.infrastructure.persistence.mapper;

import com.guardianes.walking.domain.DailyStepAggregate;
import com.guardianes.walking.infrastructure.persistence.entity.DailyStepAggregateEntity;
import org.springframework.stereotype.Component;

@Component
public class DailyStepAggregateMapper {

  public DailyStepAggregateEntity toEntity(DailyStepAggregate domain) {
    if (domain == null) {
      return null;
    }
    return new DailyStepAggregateEntity(
        domain.getGuardianId(), domain.getDate(), domain.getTotalSteps());
  }

  public DailyStepAggregate toDomain(DailyStepAggregateEntity entity) {
    if (entity == null) {
      return null;
    }
    return new DailyStepAggregate(entity.getGuardianId(), entity.getDate(), entity.getTotalSteps());
  }
}

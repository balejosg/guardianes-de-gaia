package com.guardianes.walking.infrastructure.persistence.mapper;

import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.walking.domain.model.DailyStepAggregate;
import com.guardianes.walking.domain.model.StepCount;
import com.guardianes.walking.infrastructure.persistence.entity.DailyStepAggregateEntity;
import org.springframework.stereotype.Component;

@Component
public class DailyStepAggregateMapper {

    public DailyStepAggregateEntity toEntity(DailyStepAggregate domain) {
        if (domain == null) {
            return null;
        }
        return new DailyStepAggregateEntity(
                domain.getGuardianId().value(), domain.getDate(), domain.getTotalSteps().value());
    }

    public DailyStepAggregate toDomain(DailyStepAggregateEntity entity) {
        if (entity == null) {
            return null;
        }
        return DailyStepAggregate.create(
                GuardianId.of(entity.getGuardianId()),
                entity.getDate(),
                StepCount.of(entity.getTotalSteps()));
    }
}

package com.guardianes.walking.infrastructure.persistence.mapper;

import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.shared.domain.model.Timestamp;
import com.guardianes.walking.domain.model.StepCount;
import com.guardianes.walking.domain.model.StepRecord;
import com.guardianes.walking.infrastructure.persistence.entity.StepRecordEntity;
import org.springframework.stereotype.Component;

@Component
public class StepRecordMapper {

    public StepRecordEntity toEntity(StepRecord domain) {
        if (domain == null) {
            return null;
        }
        return new StepRecordEntity(
                domain.getGuardianId().value(),
                domain.getStepCount().value(),
                domain.getRecordedAt().value());
    }

    public StepRecord toDomain(StepRecordEntity entity) {
        if (entity == null) {
            return null;
        }
        return StepRecord.createWithTimestamp(
                GuardianId.of(entity.getGuardianId()),
                StepCount.of(entity.getStepCount()),
                Timestamp.of(entity.getTimestamp()));
    }
}

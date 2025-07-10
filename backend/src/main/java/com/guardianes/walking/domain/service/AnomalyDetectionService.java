package com.guardianes.walking.domain.service;

import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.walking.domain.model.StepCount;
import java.time.LocalDateTime;

public interface AnomalyDetectionService {
    boolean isAnomalous(GuardianId guardianId, StepCount stepCount, LocalDateTime timestamp);
}

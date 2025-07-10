package com.guardianes.walking.infrastructure.service;

import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.walking.domain.model.StepCount;
import com.guardianes.walking.domain.service.AnomalyDetectionService;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class SimpleAnomalyDetectionService implements AnomalyDetectionService {

    @Override
    public boolean isAnomalous(
            GuardianId guardianId, StepCount stepCount, LocalDateTime timestamp) {
        // Simple rule: more than 15000 steps in one submission is considered anomalous
        return stepCount.value() > 15000;
    }
}

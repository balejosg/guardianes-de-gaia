package com.guardianes.walking.domain;

import java.time.LocalDateTime;

public interface AnomalyDetectionService {
    boolean isAnomalous(Long guardianId, int stepCount, LocalDateTime timestamp);
}
package com.guardianes.walking.infrastructure.service;

import com.guardianes.walking.domain.AnomalyDetectionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SimpleAnomalyDetectionService implements AnomalyDetectionService {
    
    @Override
    public boolean isAnomalous(Long guardianId, int stepCount, LocalDateTime timestamp) {
        // Simple rule: more than 15000 steps in one submission is considered anomalous
        return stepCount > 15000;
    }
}
package com.guardianes.shared.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for tracking business metrics across the application
 * Provides convenient methods for recording business events and performance metrics
 */
@Service
public class BusinessMetricsService {
    private static final Logger logger = LoggerFactory.getLogger(BusinessMetricsService.class);

    private final MeterRegistry meterRegistry;
    
    // Gauge for tracking active sessions
    private final AtomicInteger activeSessions = new AtomicInteger(0);

    @Autowired
    public BusinessMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Register gauge for active sessions
        meterRegistry.gauge("guardians.sessions.active", activeSessions);
        
        logger.info("Business metrics service initialized with meter registry");
    }

    // Step Tracking Methods
    public void recordStepSubmission(Long guardianId, Integer stepCount) {
        Counter.builder("guardians.steps.submissions")
                .description("Total number of step submissions")
                .tags(Tags.of(
                    "domain", "walking",
                    "guardian_id", guardianId.toString(),
                    "step_range", getStepRange(stepCount)
                ))
                .register(meterRegistry)
                .increment();
        logger.debug("Recorded step submission for guardian {}: {} steps", guardianId, stepCount);
    }

    public void recordStepValidationFailure(Long guardianId, String reason) {
        Counter.builder("guardians.steps.validation.failures")
                .description("Number of step submission validation failures")
                .tags(Tags.of(
                    "domain", "walking",
                    "guardian_id", guardianId.toString(),
                    "reason", reason
                ))
                .register(meterRegistry)
                .increment();
        logger.debug("Recorded step validation failure for guardian {}: {}", guardianId, reason);
    }

    public void recordAnomalousSteps(Long guardianId, Integer stepCount, String reason) {
        Counter.builder("guardians.steps.anomalous")
                .description("Number of anomalous step submissions detected")
                .tags(Tags.of(
                    "domain", "walking",
                    "guardian_id", guardianId.toString(),
                    "reason", reason,
                    "severity", getSeverityLevel(stepCount)
                ))
                .register(meterRegistry)
                .increment();
        logger.warn("Recorded anomalous steps for guardian {}: {} steps, reason: {}", guardianId, stepCount, reason);
    }

    public Timer.Sample startStepValidation() {
        return Timer.start(meterRegistry);
    }

    public void endStepValidation(Timer.Sample sample) {
        sample.stop(Timer.builder("guardians.steps.validation.duration")
                .description("Time taken to validate step submissions")
                .tag("domain", "walking")
                .register(meterRegistry));
    }

    // Energy Management Methods
    public void recordEnergyEarned(Long guardianId, Integer amount) {
        Counter.builder("guardians.energy.earned")
                .description("Total energy earned from steps")
                .tags(Tags.of(
                    "domain", "walking",
                    "guardian_id", guardianId.toString(),
                    "amount_range", getEnergyRange(amount)
                ))
                .register(meterRegistry)
                .increment(amount);
    }

    public void recordEnergySpent(Long guardianId, Integer amount, String source) {
        Counter.builder("guardians.energy.spent")
                .description("Total energy spent on activities")
                .tags(Tags.of(
                    "domain", "walking",
                    "guardian_id", guardianId.toString(),
                    "source", source,
                    "amount_range", getEnergyRange(amount)
                ))
                .register(meterRegistry)
                .increment(amount);
    }

    public void recordInsufficientEnergy(Long guardianId, Integer attempted, Integer available) {
        Counter.builder("guardians.energy.insufficient")
                .description("Number of insufficient energy attempts")
                .tags(Tags.of(
                    "domain", "walking",
                    "guardian_id", guardianId.toString(),
                    "deficit_range", getEnergyRange(attempted - available)
                ))
                .register(meterRegistry)
                .increment();
    }

    // Guardian Activity Methods
    public void recordGuardianLogin(Long guardianId) {
        Counter.builder("guardians.logins")
                .description("Number of guardian logins")
                .tags(Tags.of(
                    "domain", "guardian",
                    "guardian_id", guardianId.toString()
                ))
                .register(meterRegistry)
                .increment();
        activeSessions.incrementAndGet();
    }

    public void recordGuardianLogout(Long guardianId) {
        activeSessions.decrementAndGet();
    }

    public void recordActiveGuardian(Long guardianId) {
        Counter.builder("guardians.active.daily")
                .description("Number of active guardians per day")
                .tags(Tags.of(
                    "domain", "guardian",
                    "guardian_id", guardianId.toString()
                ))
                .register(meterRegistry)
                .increment();
    }

    // Rate Limiting Methods
    public void recordRateLimitExceeded(Long guardianId, String endpoint) {
        Counter.builder("guardians.rate.limit.exceeded")
                .description("Number of rate limit violations")
                .tags(Tags.of(
                    "domain", "walking",
                    "guardian_id", guardianId.toString(),
                    "endpoint", endpoint
                ))
                .register(meterRegistry)
                .increment();
    }

    // API Performance Methods
    public Timer.Sample startApiRequest(String endpoint, String method) {
        return Timer.start(meterRegistry);
    }

    public void endApiRequest(Timer.Sample sample, String endpoint, String method, int statusCode) {
        sample.stop(Timer.builder("guardians.api.response.time")
                .description("API response times by endpoint")
                .tags(Tags.of(
                    "endpoint", endpoint,
                    "method", method,
                    "status_code", String.valueOf(statusCode)
                ))
                .register(meterRegistry));
    }

    public void recordApiError(String endpoint, String method, int statusCode) {
        Counter.builder("guardians.api.errors")
                .description("API error count by status code")
                .tags(Tags.of(
                    "endpoint", endpoint,
                    "method", method,
                    "status_code", String.valueOf(statusCode)
                ))
                .register(meterRegistry)
                .increment();
    }

    // Database Performance Methods
    public Timer.Sample startDatabaseQuery(String queryType) {
        return Timer.start(meterRegistry);
    }

    public void endDatabaseQuery(Timer.Sample sample, String queryType) {
        sample.stop(Timer.builder("guardians.database.query.time")
                .description("Database query execution times")
                .tag("query_type", queryType)
                .register(meterRegistry));
    }

    public void recordDatabaseConnection() {
        Counter.builder("guardians.database.connections")
                .description("Database connection usage")
                .register(meterRegistry)
                .increment();
    }

    // Helper methods for categorizing metrics
    private String getStepRange(Integer stepCount) {
        if (stepCount < 1000) return "low";
        if (stepCount < 5000) return "medium";
        if (stepCount < 10000) return "high";
        return "extreme";
    }

    private String getEnergyRange(Integer amount) {
        if (amount < 100) return "small";
        if (amount < 500) return "medium";
        if (amount < 1000) return "large";
        return "huge";
    }

    private String getSeverityLevel(Integer stepCount) {
        if (stepCount > 50000) return "critical";
        if (stepCount > 20000) return "high";
        if (stepCount > 10000) return "medium";
        return "low";
    }
}
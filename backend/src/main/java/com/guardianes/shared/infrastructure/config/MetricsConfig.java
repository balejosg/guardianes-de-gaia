package com.guardianes.shared.infrastructure.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for custom business metrics using Micrometer Provides counters, timers, and gauges
 * for tracking business KPIs
 */
@Configuration
public class MetricsConfig {

    // Step Tracking Metrics
    @Bean
    public Counter stepSubmissionCounter(MeterRegistry meterRegistry) {
        return Counter.builder("guardians.steps.submissions")
                .description("Total number of step submissions")
                .tag("domain", "walking")
                .register(meterRegistry);
    }

    @Bean
    public Counter stepValidationFailureCounter(MeterRegistry meterRegistry) {
        return Counter.builder("guardians.steps.validation.failures")
                .description("Number of step submission validation failures")
                .tag("domain", "walking")
                .register(meterRegistry);
    }

    @Bean
    public Counter anomalousStepCounter(MeterRegistry meterRegistry) {
        return Counter.builder("guardians.steps.anomalous")
                .description("Number of anomalous step submissions detected")
                .tag("domain", "walking")
                .register(meterRegistry);
    }

    @Bean
    public Timer stepValidationTimer(MeterRegistry meterRegistry) {
        return Timer.builder("guardians.steps.validation.duration")
                .description("Time taken to validate step submissions")
                .tag("domain", "walking")
                .register(meterRegistry);
    }

    // Energy Management Metrics
    @Bean
    public Counter energyEarnedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("guardians.energy.earned")
                .description("Total energy earned from steps")
                .tag("domain", "walking")
                .register(meterRegistry);
    }

    @Bean
    public Counter energySpentCounter(MeterRegistry meterRegistry) {
        return Counter.builder("guardians.energy.spent")
                .description("Total energy spent on activities")
                .tag("domain", "walking")
                .register(meterRegistry);
    }

    @Bean
    public Counter insufficientEnergyCounter(MeterRegistry meterRegistry) {
        return Counter.builder("guardians.energy.insufficient")
                .description("Number of insufficient energy attempts")
                .tag("domain", "walking")
                .register(meterRegistry);
    }

    // Guardian Activity Metrics
    @Bean
    public Counter guardianLoginCounter(MeterRegistry meterRegistry) {
        return Counter.builder("guardians.logins")
                .description("Number of guardian logins")
                .tag("domain", "guardian")
                .register(meterRegistry);
    }

    @Bean
    public Counter activeGuardiansGauge(MeterRegistry meterRegistry) {
        return Counter.builder("guardians.active.daily")
                .description("Number of active guardians per day")
                .tag("domain", "guardian")
                .register(meterRegistry);
    }

    // Rate Limiting Metrics
    @Bean
    public Counter rateLimitExceededCounter(MeterRegistry meterRegistry) {
        return Counter.builder("guardians.rate.limit.exceeded")
                .description("Number of rate limit violations")
                .tag("domain", "walking")
                .register(meterRegistry);
    }

    // API Performance Metrics
    @Bean
    public Timer apiResponseTimer(MeterRegistry meterRegistry) {
        return Timer.builder("guardians.api.response.time")
                .description("API response times by endpoint")
                .register(meterRegistry);
    }

    @Bean
    public Counter apiErrorCounter(MeterRegistry meterRegistry) {
        return Counter.builder("guardians.api.errors")
                .description("API error count by status code")
                .register(meterRegistry);
    }

    // Database Performance Metrics
    @Bean
    public Timer databaseQueryTimer(MeterRegistry meterRegistry) {
        return Timer.builder("guardians.database.query.time")
                .description("Database query execution times")
                .register(meterRegistry);
    }

    @Bean
    public Counter databaseConnectionCounter(MeterRegistry meterRegistry) {
        return Counter.builder("guardians.database.connections")
                .description("Database connection usage")
                .register(meterRegistry);
    }
}

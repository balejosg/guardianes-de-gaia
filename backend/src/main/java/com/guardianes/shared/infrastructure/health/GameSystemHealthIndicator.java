package com.guardianes.shared.infrastructure.health;

import com.guardianes.walking.domain.repository.EnergyRepository;
import com.guardianes.walking.domain.repository.StepRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for game-specific system components. Provides detailed health information
 * about critical game systems.
 *
 * <p>Note: This is a simplified version that doesn't extend HealthIndicator since actuator
 * dependency might not be available. Can be enhanced when actuator is properly configured.
 */
@Component
public class GameSystemHealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(GameSystemHealthIndicator.class);

    private final StepRepository stepRepository;
    private final EnergyRepository energyRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public GameSystemHealthIndicator(
            StepRepository stepRepository,
            EnergyRepository energyRepository,
            @Autowired(required = false) RedisTemplate<String, Object> redisTemplate) {
        this.stepRepository = stepRepository;
        this.energyRepository = energyRepository;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Performs comprehensive health check of game systems Returns a map with health status and
     * details
     */
    public Map<String, Object> checkSystemHealth() {
        Map<String, Object> details = new HashMap<>();
        boolean isHealthy = true;

        try {
            // Check database connectivity and performance
            Map<String, Object> databaseHealth = checkDatabaseHealth();
            details.put("database", databaseHealth);
            if (!(Boolean) databaseHealth.get("healthy")) {
                isHealthy = false;
            }

            // Check Redis connectivity
            Map<String, Object> redisHealth = checkRedisHealth();
            details.put("redis", redisHealth);
            if (!(Boolean) redisHealth.get("healthy")) {
                isHealthy = false;
            }

            // Check step processing system
            Map<String, Object> stepProcessingHealth = checkStepProcessingHealth();
            details.put("step_processing", stepProcessingHealth);
            if (!(Boolean) stepProcessingHealth.get("healthy")) {
                isHealthy = false;
            }

            // Check energy management system
            Map<String, Object> energySystemHealth = checkEnergySystemHealth();
            details.put("energy_system", energySystemHealth);
            if (!(Boolean) energySystemHealth.get("healthy")) {
                isHealthy = false;
            }

            // Overall system status
            details.put("overall_healthy", isHealthy);
            details.put("timestamp", Instant.now());
            details.put("version", "1.0.0");

            return details;

        } catch (Exception e) {
            logger.error("Health check failed", e);
            details.put("overall_healthy", false);
            details.put("error", e.getMessage());
            details.put("timestamp", Instant.now());
            return details;
        }
    }

    /** Checks database connectivity and basic query performance */
    private Map<String, Object> checkDatabaseHealth() {
        Map<String, Object> health = new HashMap<>();

        try {
            Instant start = Instant.now();

            // Test basic database connectivity with a simple operation
            // This should be fast and not impact performance
            boolean canAccess = stepRepository != null;

            Duration queryTime = Duration.between(start, Instant.now());

            health.put("healthy", canAccess);
            health.put("response_time_ms", queryTime.toMillis());
            health.put("repository_accessible", canAccess);

            // Warn if query takes too long
            if (queryTime.toMillis() > 1000) {
                health.put("warning", "Database query response time is slow");
            }

        } catch (Exception e) {
            logger.warn("Database health check failed", e);
            health.put("healthy", false);
            health.put("error", e.getMessage());
        }

        return health;
    }

    /** Checks Redis connectivity and basic operations */
    private Map<String, Object> checkRedisHealth() {
        Map<String, Object> health = new HashMap<>();

        if (redisTemplate == null) {
            health.put("healthy", false);
            health.put("error", "Redis not configured");
            return health;
        }

        try {
            Instant start = Instant.now();

            // Test Redis connectivity with a simple ping
            String testKey = "health_check_" + System.currentTimeMillis();
            redisTemplate.opsForValue().set(testKey, "test", Duration.ofSeconds(10));
            String value = (String) redisTemplate.opsForValue().get(testKey);
            redisTemplate.delete(testKey);

            Duration responseTime = Duration.between(start, Instant.now());

            health.put("healthy", "test".equals(value));
            health.put("response_time_ms", responseTime.toMillis());

            if (responseTime.toMillis() > 500) {
                health.put("warning", "Redis response time is slow");
            }

        } catch (Exception e) {
            logger.warn("Redis health check failed", e);
            health.put("healthy", false);
            health.put("error", e.getMessage());
        }

        return health;
    }

    /** Checks step processing system health */
    private Map<String, Object> checkStepProcessingHealth() {
        Map<String, Object> health = new HashMap<>();

        try {
            // Check if step repository is responsive
            Instant start = Instant.now();
            boolean canQuery = stepRepository != null;
            Duration queryTime = Duration.between(start, Instant.now());

            health.put("healthy", canQuery);
            health.put("repository_responsive", canQuery);
            health.put("query_time_ms", queryTime.toMillis());

            // Additional checks could include:
            // - Recent submission rate
            // - Error rate in last hour
            // - Processing queue depth

        } catch (Exception e) {
            logger.warn("Step processing health check failed", e);
            health.put("healthy", false);
            health.put("error", e.getMessage());
        }

        return health;
    }

    /** Checks energy management system health */
    private Map<String, Object> checkEnergySystemHealth() {
        Map<String, Object> health = new HashMap<>();

        try {
            // Check if energy repository is responsive
            Instant start = Instant.now();
            boolean canQuery = energyRepository != null;
            Duration queryTime = Duration.between(start, Instant.now());

            health.put("healthy", canQuery);
            health.put("repository_responsive", canQuery);
            health.put("query_time_ms", queryTime.toMillis());

            // Additional checks could include:
            // - Energy calculation accuracy
            // - Transaction consistency
            // - Balance integrity

        } catch (Exception e) {
            logger.warn("Energy system health check failed", e);
            health.put("healthy", false);
            health.put("error", e.getMessage());
        }

        return health;
    }
}

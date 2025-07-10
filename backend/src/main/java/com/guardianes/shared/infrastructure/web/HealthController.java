package com.guardianes.shared.infrastructure.web;

import com.guardianes.shared.infrastructure.health.GameSystemHealthIndicator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health check controller for game-specific system monitoring. Provides detailed health information
 * about critical game systems.
 */
@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Game system health monitoring")
public class HealthController {

    private final GameSystemHealthIndicator gameSystemHealthIndicator;

    @Autowired
    public HealthController(GameSystemHealthIndicator gameSystemHealthIndicator) {
        this.gameSystemHealthIndicator = gameSystemHealthIndicator;
    }

    @GetMapping("/game-systems")
    @Operation(
            summary = "Check game systems health",
            description =
                    "Provides detailed health information about database, Redis, step processing, and energy management systems.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Health check completed"),
                @ApiResponse(
                        responseCode = "503",
                        description = "One or more systems are unhealthy")
            })
    public ResponseEntity<Map<String, Object>> checkGameSystemsHealth() {
        Map<String, Object> healthStatus = gameSystemHealthIndicator.checkSystemHealth();

        // Return 503 if any system is unhealthy, 200 if all are healthy
        boolean overallHealthy = (Boolean) healthStatus.getOrDefault("overall_healthy", false);

        if (overallHealthy) {
            return ResponseEntity.ok(healthStatus);
        } else {
            return ResponseEntity.status(503).body(healthStatus);
        }
    }
}

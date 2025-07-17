package com.guardianes.testutils;

import org.testcontainers.containers.GenericContainer;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Health Check Utilities
 * 
 * Provides utilities for validating health endpoints and ensuring
 * production readiness of the application stack.
 */
public class HealthCheckUtils {

    /**
     * Validates that the health endpoint returns a healthy status
     */
    public static void validateHealthEndpoint(HttpClient client, GenericContainer<?> backend, String basicAuth) throws Exception {
        HttpResponse<String> response = DockerTestUtils.makeAuthenticatedRequest(
                client, backend, "/actuator/health", basicAuth);
        
        assertEquals(200, response.statusCode(), "Health endpoint should return 200 OK");
        
        String body = response.body();
        assertTrue(body.contains("\"status\":\"UP\""), "Health status should be UP");
        assertTrue(body.contains("\"diskSpace\""), "Should include disk space health");
        assertTrue(body.contains("\"ping\""), "Should include ping health");
    }

    /**
     * Validates that all actuator endpoints are accessible
     */
    public static void validateActuatorEndpoints(HttpClient client, GenericContainer<?> backend, String basicAuth) throws Exception {
        String[] endpoints = {
            "/actuator/health",
            "/actuator/info",
            "/actuator/metrics",
            "/actuator/prometheus"
        };
        
        for (String endpoint : endpoints) {
            HttpResponse<String> response = DockerTestUtils.makeAuthenticatedRequest(
                    client, backend, endpoint, basicAuth);
            assertEquals(200, response.statusCode(), 
                        "Endpoint " + endpoint + " should be accessible");
        }
    }

    /**
     * Validates that API endpoints are functional
     */
    public static void validateApiEndpoints(HttpClient client, GenericContainer<?> backend, String basicAuth) throws Exception {
        // Test current steps endpoint
        HttpResponse<String> stepsResponse = DockerTestUtils.makeAuthenticatedRequest(
                client, backend, "/api/v1/guardians/1/steps/current", basicAuth);
        assertEquals(200, stepsResponse.statusCode(), "Steps endpoint should be accessible");
        assertTrue(DockerTestUtils.validateJsonResponse(stepsResponse.body(), 
                   "guardianId", "currentSteps", "availableEnergy", "date"),
                   "Steps response should contain expected fields");
        
        // Test energy balance endpoint
        HttpResponse<String> energyResponse = DockerTestUtils.makeAuthenticatedRequest(
                client, backend, "/api/v1/guardians/1/energy/balance", basicAuth);
        assertEquals(200, energyResponse.statusCode(), "Energy endpoint should be accessible");
        assertTrue(DockerTestUtils.validateJsonResponse(energyResponse.body(),
                   "guardianId", "currentBalance", "transactionSummary"),
                   "Energy response should contain expected fields");
    }

    /**
     * Validates that authentication is properly enforced
     */
    public static void validateAuthentication(HttpClient client, GenericContainer<?> backend) throws Exception {
        // Test that unauthenticated requests are rejected
        HttpResponse<String> unauthResponse = DockerTestUtils.makeUnauthenticatedRequest(
                client, backend, "/actuator/health");
        assertEquals(401, unauthResponse.statusCode(), 
                    "Unauthenticated requests should be rejected");
        
        // Test that invalid credentials are rejected
        String invalidAuth = DockerTestUtils.createBasicAuth("invalid", "invalid");
        HttpResponse<String> invalidResponse = DockerTestUtils.makeAuthenticatedRequest(
                client, backend, "/actuator/health", invalidAuth);
        assertEquals(401, invalidResponse.statusCode(), 
                    "Invalid credentials should be rejected");
    }

    /**
     * Validates that the backend connects to all required external services
     */
    public static void validateExternalServiceConnectivity(HttpClient client, GenericContainer<?> backend, String basicAuth) throws Exception {
        HttpResponse<String> response = DockerTestUtils.makeAuthenticatedRequest(
                client, backend, "/actuator/health", basicAuth);
        
        String body = response.body();
        assertTrue(body.contains("\"redis\""), "Should be connected to Redis");
        
        // Check logs for connection errors
        String logs = backend.getLogs();
        assertFalse(logs.contains("Connection refused"), 
                   "Should not have connection refused errors");
        assertFalse(logs.contains("Unable to connect"), 
                   "Should not have connectivity errors");
        assertFalse(logs.contains("Connection timeout"), 
                   "Should not have connection timeouts");
    }

    /**
     * Validates that there are no critical startup errors
     */
    public static void validateNoStartupErrors(GenericContainer<?> backend) {
        String logs = backend.getLogs();
        
        // Check for critical errors
        assertFalse(logs.contains("FATAL"), "Should not have FATAL errors");
        assertFalse(logs.contains("ERROR") && logs.contains("Failed to start"), 
                   "Should not have startup failure errors");
        assertFalse(logs.contains("OutOfMemoryError"), "Should not have memory errors");
        
        // Check for specific metrics/cgroup issues
        assertFalse(logs.contains("Cannot invoke \"jdk.internal.platform.CgroupInfo"), 
                   "Should not have cgroup issues");
        assertFalse(logs.contains("ProcessorMetrics"), 
                   "Should not have ProcessorMetrics issues");
        assertFalse(logs.contains("TomcatMetricsBinder"), 
                   "Should not have TomcatMetricsBinder issues");
    }

    /**
     * Validates performance characteristics within acceptable bounds
     */
    public static void validatePerformance(HttpClient client, GenericContainer<?> backend, String basicAuth) throws Exception {
        // Test health endpoint response time
        long startTime = System.currentTimeMillis();
        HttpResponse<String> healthResponse = DockerTestUtils.makeAuthenticatedRequest(
                client, backend, "/actuator/health", basicAuth);
        long healthTime = System.currentTimeMillis() - startTime;
        
        assertEquals(200, healthResponse.statusCode(), "Health endpoint should respond");
        assertTrue(healthTime < 5000, "Health endpoint should respond within 5 seconds, took: " + healthTime + "ms");
        
        // Test API endpoint response time
        startTime = System.currentTimeMillis();
        HttpResponse<String> apiResponse = DockerTestUtils.makeAuthenticatedRequest(
                client, backend, "/api/v1/guardians/1/steps/current", basicAuth);
        long apiTime = System.currentTimeMillis() - startTime;
        
        assertEquals(200, apiResponse.statusCode(), "API endpoint should respond");
        assertTrue(apiTime < 10000, "API endpoint should respond within 10 seconds, took: " + apiTime + "ms");
    }

    /**
     * Waits for the backend to be fully started and healthy
     */
    public static boolean waitForBackendHealth(HttpClient client, GenericContainer<?> backend, String basicAuth, int timeoutSeconds) {
        long startTime = System.currentTimeMillis();
        long timeoutMs = timeoutSeconds * 1000L;
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                HttpResponse<String> response = DockerTestUtils.makeAuthenticatedRequest(
                        client, backend, "/actuator/health", basicAuth);
                
                if (response.statusCode() == 200 && 
                    response.body().contains("\"status\":\"UP\"")) {
                    return true;
                }
            } catch (Exception e) {
                // Continue waiting
            }
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        
        return false;
    }

    /**
     * Comprehensive production readiness check
     */
    public static void validateProductionReadiness(HttpClient client, GenericContainer<?> backend, String basicAuth) throws Exception {
        // Basic health and startup validation
        DockerTestUtils.assertContainerHealthy(backend, "Backend");
        DockerTestUtils.assertNoCgroupIssues(backend);
        DockerTestUtils.assertSuccessfulSpringBootStartup(backend);
        
        // Health endpoint validation
        validateHealthEndpoint(client, backend, basicAuth);
        
        // All actuator endpoints validation
        validateActuatorEndpoints(client, backend, basicAuth);
        
        // API functionality validation
        validateApiEndpoints(client, backend, basicAuth);
        
        // Security validation
        validateAuthentication(client, backend);
        
        // External service connectivity
        validateExternalServiceConnectivity(client, backend, basicAuth);
        
        // Startup error validation
        validateNoStartupErrors(backend);
        
        // Performance validation
        validatePerformance(client, backend, basicAuth);
    }

    /**
     * Validates monitoring stack integration
     */
    public static void validateMonitoringIntegration(HttpClient client, GenericContainer<?> backend, String basicAuth) throws Exception {
        // Prometheus metrics should be available
        HttpResponse<String> prometheusResponse = DockerTestUtils.makeAuthenticatedRequest(
                client, backend, "/actuator/prometheus", basicAuth);
        assertEquals(200, prometheusResponse.statusCode(), "Prometheus endpoint should be accessible");
        
        String prometheusBody = prometheusResponse.body();
        assertTrue(prometheusBody.contains("# HELP"), "Should contain Prometheus help text");
        assertTrue(prometheusBody.contains("# TYPE"), "Should contain Prometheus type definitions");
        assertTrue(prometheusBody.contains("jvm_"), "Should contain JVM metrics");
        
        // Metrics endpoint should list available metrics
        HttpResponse<String> metricsResponse = DockerTestUtils.makeAuthenticatedRequest(
                client, backend, "/actuator/metrics", basicAuth);
        assertEquals(200, metricsResponse.statusCode(), "Metrics endpoint should be accessible");
        assertTrue(metricsResponse.body().contains("\"names\":["), "Should list available metrics");
    }
}
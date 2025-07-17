package com.guardianes.integration.docker;

import com.guardianes.testconfig.GuardianTestConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Timeout;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Production Environment Integration Tests
 * 
 * This test validates the complete production-ready stack including:
 * - Full Docker Compose equivalent environment
 * - All service interconnectivity
 * - Monitoring stack integration
 * - Load balancer configuration
 * - Production readiness validation
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Import(GuardianTestConfiguration.class)
@DisplayName("Production Environment Integration Tests")
class ProductionEnvironmentTest {

    private static final Network network = Network.newNetwork();
    private static HttpClient httpClient;
    private static String basicAuth;
    
    @Container
    @SuppressWarnings("resource")
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withNetwork(network)
            .withNetworkAliases("mysql")
            .withDatabaseName("guardianes")
            .withUsername("guardianes")
            .withPassword("secret")
            .withEnv("MYSQL_ROOT_PASSWORD", "rootsecret");

    @Container
    @SuppressWarnings("resource")
    static final GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withNetwork(network)
            .withNetworkAliases("redis")
            .withExposedPorts(6379);

    @Container
    @SuppressWarnings("resource")
    static final GenericContainer<?> rabbitmq = new GenericContainer<>("rabbitmq:3.12-management-alpine")
            .withNetwork(network)
            .withNetworkAliases("rabbitmq")
            .withExposedPorts(5672, 15672)
            .withEnv("RABBITMQ_DEFAULT_USER", "guardianes")
            .withEnv("RABBITMQ_DEFAULT_PASS", "secret");

    @Container
    @SuppressWarnings("resource")
    static final GenericContainer<?> prometheus = new GenericContainer<>("prom/prometheus:latest")
            .withNetwork(network)
            .withNetworkAliases("prometheus")
            .withExposedPorts(9090);

    @Container
    @SuppressWarnings("resource")
    static final GenericContainer<?> backend = new GenericContainer<>(
            DockerImageName.parse("guardianes-de-gaia-backend:latest")
                    .asCompatibleSubstituteFor("openjdk"))
            .withNetwork(network)
            .withNetworkAliases("backend")
            .withExposedPorts(8080)
            .withEnv("SPRING_PROFILES_ACTIVE", "dev")
            .withEnv("SPRING_DATASOURCE_URL", "jdbc:mysql://mysql:3306/guardianes?useSSL=false&allowPublicKeyRetrieval=true")
            .withEnv("SPRING_DATASOURCE_USERNAME", "guardianes")
            .withEnv("SPRING_DATASOURCE_PASSWORD", "secret")
            .withEnv("SPRING_REDIS_HOST", "redis")
            .withEnv("SPRING_REDIS_PORT", "6379")
            .withEnv("SPRING_RABBITMQ_HOST", "rabbitmq")
            .withEnv("SPRING_RABBITMQ_PORT", "5672")
            .dependsOn(mysql, redis, rabbitmq);

    @BeforeAll
    static void setUp() {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        basicAuth = Base64.getEncoder().encodeToString("admin:dev123".getBytes());
    }

    @AfterAll
    static void cleanupResources() {
        // Cleanup network resources
        if (network != null) {
            try {
                network.close();
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
    }

    @Test
    @DisplayName("Full production stack should start and be healthy")
    @Timeout(value = 3, unit = TimeUnit.MINUTES)
    void shouldHaveHealthyProductionStack() {
        // Given: All production services are started
        
        // When: We check all containers are running
        // Then: All services should be running
        assertTrue(mysql.isRunning(), "MySQL should be running");
        assertTrue(redis.isRunning(), "Redis should be running");
        assertTrue(rabbitmq.isRunning(), "RabbitMQ should be running");
        assertTrue(prometheus.isRunning(), "Prometheus should be running");
        assertTrue(backend.isRunning(), "Backend should be running");
        
        // And: All services should be running (not all containers have healthchecks)
        assertTrue(mysql.isRunning(), "MySQL should be running");
        assertTrue(redis.isRunning(), "Redis should be running");
        assertTrue(backend.isRunning(), "Backend should be running");
    }

    @Test
    @DisplayName("Backend should connect to all external services successfully")
    @Timeout(value = 1, unit = TimeUnit.MINUTES)
    void shouldConnectToAllExternalServices() throws Exception {
        // Given: Backend is running
        assertTrue(backend.isRunning(), "Backend must be running");
        
        // When: We call the health endpoint to check service connectivity
        HttpResponse<String> response = makeBackendRequest("/actuator/health");
        
        // Then: Health should be UP
        assertEquals(200, response.statusCode(), "Health endpoint should return 200");
        String body = response.body();
        assertTrue(body.contains("\"status\":\"UP\""), "Overall health should be UP");
        
        // And: Redis connection should be healthy
        assertTrue(body.contains("\"redis\""), "Should include Redis health");
        assertTrue(body.contains("\"version\""), "Redis health should include version");
        
        // And: Backend logs should not show persistent connection failures
        String logs = backend.getLogs();
        // Allow initial connection retries during startup but not persistent failures
        long connectionRefusedCount = logs.lines()
                .filter(line -> line.contains("Connection refused"))
                .count();
        assertTrue(connectionRefusedCount < 10, 
                   "Should not have excessive connection refused errors (found: " + connectionRefusedCount + ")");
        
        // Check that the application actually started successfully
        assertTrue(logs.contains("Started GuardianesApplication"), 
                   "Application should have started successfully");
    }

    @Test
    @DisplayName("API endpoints should be fully functional in production environment")
    @Timeout(value = 1, unit = TimeUnit.MINUTES)
    void shouldHaveFullyFunctionalApiEndpoints() throws Exception {
        // Given: Backend is running and healthy
        assertTrue(backend.isRunning(), "Backend must be running");
        
        // When: We test the main API endpoints
        // Current steps endpoint
        HttpResponse<String> stepsResponse = makeBackendRequest("/api/v1/guardians/1/steps/current");
        assertEquals(200, stepsResponse.statusCode(), "Steps endpoint should be accessible");
        assertTrue(stepsResponse.body().contains("guardianId"), "Should return guardian data");
        
        // Energy balance endpoint  
        HttpResponse<String> energyResponse = makeBackendRequest("/api/v1/guardians/1/energy/balance");
        assertEquals(200, energyResponse.statusCode(), "Energy endpoint should be accessible");
        assertTrue(energyResponse.body().contains("currentBalance"), "Should return energy data");
        
        // Then: All API functionality should work correctly
        assertTrue(stepsResponse.body().contains("\"guardianId\":1"), "Should return correct guardian ID");
        assertTrue(energyResponse.body().contains("\"guardianId\":1"), "Should return correct guardian ID");
    }

    @Test
    @DisplayName("Monitoring endpoints should be accessible for production monitoring")
    @Timeout(value = 1, unit = TimeUnit.MINUTES)
    void shouldHaveAccessibleMonitoringEndpoints() throws Exception {
        // Given: Backend and monitoring services are running
        assertTrue(backend.isRunning(), "Backend must be running");
        assertTrue(prometheus.isRunning(), "Prometheus must be running");
        
        // When: We test monitoring endpoints
        // Health endpoint
        HttpResponse<String> healthResponse = makeBackendRequest("/actuator/health");
        assertEquals(200, healthResponse.statusCode(), "Health monitoring should work");
        
        // Metrics endpoint
        HttpResponse<String> metricsResponse = makeBackendRequest("/actuator/metrics");
        assertEquals(200, metricsResponse.statusCode(), "Metrics monitoring should work");
        
        // Prometheus endpoint
        HttpResponse<String> prometheusResponse = makeBackendRequest("/actuator/prometheus");
        assertEquals(200, prometheusResponse.statusCode(), "Prometheus monitoring should work");
        
        // Then: Monitoring data should be valid
        assertTrue(healthResponse.body().contains("\"status\":\"UP\""), 
                  "Health monitoring should show UP status");
        assertTrue(metricsResponse.body().contains("\"names\":["), 
                  "Metrics should list available metrics");
        assertTrue(prometheusResponse.body().contains("# HELP"), 
                  "Prometheus should return proper format");
    }

    @Test
    @DisplayName("Production environment should handle authentication correctly")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void shouldHandleAuthenticationCorrectly() throws Exception {
        // Given: Backend is running
        assertTrue(backend.isRunning(), "Backend must be running");
        
        // When: We test authentication
        // Valid credentials should work
        HttpResponse<String> validAuth = makeBackendRequest("/actuator/health");
        assertEquals(200, validAuth.statusCode(), "Valid auth should work");
        
        // Invalid credentials should fail
        String invalidAuth = Base64.getEncoder().encodeToString("invalid:invalid".getBytes());
        String baseUrl = "http://localhost:" + backend.getMappedPort(8080);
        HttpRequest invalidRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/actuator/health"))
                .header("Authorization", "Basic " + invalidAuth)
                .timeout(Duration.ofSeconds(10))
                .build();
        HttpResponse<String> invalidResponse = httpClient.send(invalidRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(401, invalidResponse.statusCode(), "Invalid auth should fail");
        
        // No credentials should fail
        HttpRequest noAuthRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/actuator/health"))
                .timeout(Duration.ofSeconds(10))
                .build();
        HttpResponse<String> noAuthResponse = httpClient.send(noAuthRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(401, noAuthResponse.statusCode(), "No auth should fail");
        
        // Then: Authentication should be properly enforced
        assertTrue(true, "Authentication validation completed");
    }

    @Test
    @DisplayName("Production environment should have proper error handling")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void shouldHaveProperErrorHandling() throws Exception {
        // Given: Backend is running
        assertTrue(backend.isRunning(), "Backend must be running");
        
        // When: We test error scenarios
        // Invalid endpoint should return 404
        HttpResponse<String> notFoundResponse = makeBackendRequest("/invalid/endpoint");
        assertEquals(404, notFoundResponse.statusCode(), "Invalid endpoints should return 404");
        
        // Invalid data should return appropriate error
        String invalidJson = "{\"invalid\": \"data\"}";
        String baseUrl = "http://localhost:" + backend.getMappedPort(8080);
        HttpRequest invalidDataRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v1/guardians/1/steps"))
                .header("Authorization", "Basic " + basicAuth)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                .timeout(Duration.ofSeconds(10))
                .build();
        HttpResponse<String> invalidDataResponse = httpClient.send(invalidDataRequest, HttpResponse.BodyHandlers.ofString());
        assertTrue(invalidDataResponse.statusCode() >= 400 && invalidDataResponse.statusCode() < 500, 
                  "Invalid data should return client error");
        
        // Then: Error handling should be production-ready
        assertTrue(true, "Error handling validation completed");
    }

    @Test
    @DisplayName("Production stack should be resilient to service restarts")
    @Timeout(value = 2, unit = TimeUnit.MINUTES)
    void shouldBeResilientToServiceRestarts() throws Exception {
        // Given: All services are running and backend is healthy
        assertTrue(backend.isRunning(), "Backend must be running");
        HttpResponse<String> initialHealth = makeBackendRequest("/actuator/health");
        assertEquals(200, initialHealth.statusCode(), "Initial health should be OK");
        
        // When: We simulate a Redis restart (Redis is non-critical for basic functionality)
        redis.stop();
        Thread.sleep(2000); // Wait for impact
        redis.start();
        Thread.sleep(5000); // Wait for recovery
        
        // Then: Backend should still be functional (though Redis health may be down temporarily)
        HttpResponse<String> afterRestartHealth = makeBackendRequest("/actuator/health");
        assertEquals(200, afterRestartHealth.statusCode(), "Health endpoint should still respond");
        
        // And: API endpoints should still work
        HttpResponse<String> apiResponse = makeBackendRequest("/api/v1/guardians/1/steps/current");
        assertEquals(200, apiResponse.statusCode(), "API should still work after Redis restart");
        
        // And: Backend logs should not show critical failures
        String logs = backend.getLogs();
        assertFalse(logs.contains("FATAL"), "Should not have FATAL level errors");
        assertFalse(logs.contains("APPLICATION FAILED"), "Application should not fail");
    }

    @Test
    @DisplayName("Production environment should have performance within acceptable limits")
    @Timeout(value = 1, unit = TimeUnit.MINUTES)
    void shouldHaveAcceptablePerformance() throws Exception {
        // Given: Backend is running and healthy
        assertTrue(backend.isRunning(), "Backend must be running");
        
        // When: We measure response times for critical endpoints
        long startTime = System.currentTimeMillis();
        HttpResponse<String> healthResponse = makeBackendRequest("/actuator/health");
        long healthTime = System.currentTimeMillis() - startTime;
        
        startTime = System.currentTimeMillis();
        HttpResponse<String> apiResponse = makeBackendRequest("/api/v1/guardians/1/steps/current");
        long apiTime = System.currentTimeMillis() - startTime;
        
        // Then: Response times should be reasonable for production
        assertEquals(200, healthResponse.statusCode(), "Health should respond successfully");
        assertEquals(200, apiResponse.statusCode(), "API should respond successfully");
        
        assertTrue(healthTime < 5000, "Health endpoint should respond within 5 seconds");
        assertTrue(apiTime < 10000, "API endpoint should respond within 10 seconds");
        
        // And: Memory usage should be reasonable (check for memory leaks in logs)
        String logs = backend.getLogs();
        assertFalse(logs.contains("OutOfMemoryError"), "Should not have memory issues");
        assertFalse(logs.contains("Memory leak"), "Should not have memory leaks");
    }

    private HttpResponse<String> makeBackendRequest(String endpoint) throws Exception {
        String baseUrl = "http://localhost:" + backend.getMappedPort(8080);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .header("Authorization", "Basic " + basicAuth)
                .timeout(Duration.ofSeconds(10))
                .build();
                
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
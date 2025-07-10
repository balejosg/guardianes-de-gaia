package com.guardianes.integration.docker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;
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
 * Actuator Endpoints Integration Tests
 * 
 * Validates that all Spring Boot Actuator endpoints are accessible and working
 * correctly in the Docker container environment. This test would have caught
 * issues with missing actuator dependency and metrics configuration problems.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@DisplayName("Actuator Endpoints Integration Tests")
class ActuatorEndpointsTest {

    private static final Network network = Network.newNetwork();
    private static HttpClient httpClient;
    private static String basicAuth;
    
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withNetwork(network)
            .withNetworkAliases("mysql")
            .withDatabaseName("guardianes")
            .withUsername("guardianes")
            .withPassword("secret");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withNetwork(network)
            .withNetworkAliases("redis")
            .withExposedPorts(6379);

    @Container
    static GenericContainer<?> backend = new GenericContainer<>(
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
            .dependsOn(mysql, redis);

    @BeforeAll
    static void setUp() {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        basicAuth = Base64.getEncoder().encodeToString("admin:dev123".getBytes());
    }

    @Test
    @DisplayName("Health endpoint should be accessible and return comprehensive health info")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void shouldHaveAccessibleHealthEndpoint() throws Exception {
        // When: We call the health endpoint
        HttpResponse<String> response = makeRequest("/actuator/health");
        
        // Then: Should return 200 OK
        assertEquals(200, response.statusCode(), "Health endpoint should return 200 OK");
        
        // And: Should contain comprehensive health information
        String body = response.body();
        assertTrue(body.contains("\"status\":\"UP\""), "Should indicate UP status");
        assertTrue(body.contains("\"diskSpace\""), "Should include disk space health");
        assertTrue(body.contains("\"ping\""), "Should include ping health");
        assertTrue(body.contains("\"redis\""), "Should include Redis health");
        
        // And: Should show detailed health information (show-details=always in config)
        assertTrue(body.contains("\"details\""), "Should include detailed health information");
    }

    @Test
    @DisplayName("Info endpoint should be accessible and return application info")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void shouldHaveAccessibleInfoEndpoint() throws Exception {
        // When: We call the info endpoint
        HttpResponse<String> response = makeRequest("/actuator/info");
        
        // Then: Should return 200 OK
        assertEquals(200, response.statusCode(), "Info endpoint should return 200 OK");
        
        // And: Should return valid JSON
        String body = response.body();
        assertTrue(body.startsWith("{") && body.endsWith("}"), "Should return valid JSON");
    }

    @Test
    @DisplayName("Metrics endpoint should be accessible without cgroup-related failures")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void shouldHaveAccessibleMetricsEndpoint() throws Exception {
        // When: We call the metrics endpoint
        HttpResponse<String> response = makeRequest("/actuator/metrics");
        
        // Then: Should return 200 OK (not fail due to ProcessorMetrics issues)
        assertEquals(200, response.statusCode(), "Metrics endpoint should return 200 OK");
        
        // And: Should contain metrics list
        String body = response.body();
        assertTrue(body.contains("\"names\":["), "Should contain metrics names list");
        
        // And: Should not contain problematic processor metrics that cause cgroup issues
        String logs = backend.getLogs();
        assertFalse(logs.contains("ProcessorMetrics"), 
                   "Should not have ProcessorMetrics initialization issues");
        assertFalse(logs.contains("CgroupInfo"), 
                   "Should not have cgroup access issues");
    }

    @Test
    @DisplayName("Prometheus endpoint should be accessible for monitoring integration")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void shouldHaveAccessiblePrometheusEndpoint() throws Exception {
        // When: We call the prometheus metrics endpoint
        HttpResponse<String> response = makeRequest("/actuator/prometheus");
        
        // Then: Should return 200 OK
        assertEquals(200, response.statusCode(), "Prometheus endpoint should return 200 OK");
        
        // And: Should return Prometheus format metrics
        String body = response.body();
        assertTrue(body.contains("# HELP"), "Should contain Prometheus help comments");
        assertTrue(body.contains("# TYPE"), "Should contain Prometheus type definitions");
        
        // And: Should contain JVM metrics (since jvm metrics are enabled)
        assertTrue(body.contains("jvm_"), "Should contain JVM metrics");
        
        // But: Should not contain system processor metrics (since they're disabled)
        assertFalse(body.contains("system_cpu_usage"), 
                   "Should not contain system CPU metrics that cause cgroup issues");
    }

    @Test
    @DisplayName("All configured actuator endpoints should be accessible")
    @Timeout(value = 1, unit = TimeUnit.MINUTES)
    void shouldHaveAllConfiguredEndpointsAccessible() throws Exception {
        // Given: Expected endpoints from application-dev.properties
        String[] endpoints = {
            "/actuator/health",
            "/actuator/info", 
            "/actuator/metrics",
            "/actuator/prometheus"
        };
        
        // When & Then: All endpoints should be accessible
        for (String endpoint : endpoints) {
            HttpResponse<String> response = makeRequest(endpoint);
            assertEquals(200, response.statusCode(), 
                        "Endpoint " + endpoint + " should be accessible");
        }
    }

    @Test
    @DisplayName("Actuator endpoints should require authentication")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void shouldRequireAuthenticationForActuatorEndpoints() throws Exception {
        // When: We call health endpoint without authentication
        String baseUrl = "http://localhost:" + backend.getMappedPort(8080);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/actuator/health"))
                .timeout(Duration.ofSeconds(10))
                .build();
                
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Then: Should require authentication (401 Unauthorized)
        assertEquals(401, response.statusCode(), 
                    "Actuator endpoints should require authentication");
    }

    @Test
    @DisplayName("Health endpoint should include all expected components")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void shouldIncludeAllExpectedHealthComponents() throws Exception {
        // When: We call the health endpoint
        HttpResponse<String> response = makeRequest("/actuator/health");
        
        // Then: Should include all expected health components
        String body = response.body();
        
        // Core components
        assertTrue(body.contains("\"diskSpace\""), "Should include disk space component");
        assertTrue(body.contains("\"ping\""), "Should include ping component");
        
        // External dependencies
        assertTrue(body.contains("\"redis\""), "Should include Redis health component");
        
        // Each component should have status
        assertTrue(body.contains("\"status\":\"UP\""), "Components should report UP status");
        
        // Redis component should include version details
        assertTrue(body.contains("\"version\""), "Redis component should include version info");
    }

    @Test
    @DisplayName("Metrics endpoint should not expose problematic system metrics")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void shouldNotExposeProblematicSystemMetrics() throws Exception {
        // When: We call the metrics endpoint to get available metrics
        HttpResponse<String> response = makeRequest("/actuator/metrics");
        String body = response.body();
        
        // Then: Should not include processor metrics that cause cgroup issues
        assertFalse(body.contains("system.cpu.usage"), 
                   "Should not expose system CPU usage metrics");
        assertFalse(body.contains("process.cpu.usage"), 
                   "Should not expose process CPU usage metrics");
        
        // But: Should still include safe JVM metrics
        assertTrue(body.contains("jvm.memory.used") || body.contains("jvm."), 
                  "Should include safe JVM metrics");
    }

    private HttpResponse<String> makeRequest(String endpoint) throws Exception {
        String baseUrl = "http://localhost:" + backend.getMappedPort(8080);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .header("Authorization", "Basic " + basicAuth)
                .timeout(Duration.ofSeconds(10))
                .build();
                
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
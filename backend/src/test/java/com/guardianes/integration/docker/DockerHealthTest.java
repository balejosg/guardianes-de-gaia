package com.guardianes.integration.docker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
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
 * Docker Health Integration Tests
 * 
 * This test class validates that our Spring Boot application can start successfully
 * in a Docker container environment, preventing issues like:
 * - Java 17 cgroup access problems 
 * - Spring Boot Actuator metrics failures (ProcessorMetrics, TomcatMetrics)
 * - Container startup configuration issues
 * 
 * These tests would have caught the cgroup/SystemMetricsAutoConfiguration issues
 * we encountered during development.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@DisplayName("Docker Health Integration Tests")
class DockerHealthTest {

    private static final Network network = Network.newNetwork();
    
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

    // Note: This test validates production Docker environment by building the actual image
    @Container
    static GenericContainer<?> backend = new GenericContainer<>(
            DockerImageName.parse("guardianes-de-gaia-backend:latest")
                    .asCompatibleSubstituteFor("openjdk"))
            .withNetwork(network)
            .withNetworkAliases("backend")
            .withExposedPorts(8080)
            .withEnv("SPRING_PROFILES_ACTIVE", "test")
            .withEnv("SPRING_DATASOURCE_URL", "jdbc:mysql://mysql:3306/guardianes?useSSL=false&allowPublicKeyRetrieval=true")
            .withEnv("SPRING_DATASOURCE_USERNAME", "guardianes")
            .withEnv("SPRING_DATASOURCE_PASSWORD", "secret")
            .withEnv("SPRING_REDIS_HOST", "redis")
            .withEnv("SPRING_REDIS_PORT", "6379")
            .dependsOn(mysql, redis);

    @BeforeAll
    static void buildDockerImage() {
        // Build the production Docker image before testing
        try {
            ProcessBuilder pb = new ProcessBuilder("docker", "build", "-t", "guardianes-de-gaia-backend:latest", "-f", "Dockerfile.dev", ".");
            pb.directory(new java.io.File("../.."));
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Failed to build Docker image for testing");
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot build Docker image for production environment testing", e);
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Configure test properties if needed
    }

    @Test
    @DisplayName("Backend container should start successfully without cgroup issues")
    @Timeout(value = 2, unit = TimeUnit.MINUTES)
    void shouldStartBackendContainerSuccessfully() {
        // Given: All containers are started (handled by @Container)
        
        // When: We check if the backend container is running
        boolean isRunning = backend.isRunning();
        
        // Then: The container should be running without startup failures
        assertTrue(isRunning, "Backend container should be running");
        
        // And: No cgroup-related errors should be present in logs
        String logs = backend.getLogs();
        assertFalse(logs.contains("Cannot invoke \"jdk.internal.platform.CgroupInfo.getMountPoint()\""), 
                   "Container should not have cgroup access issues");
        assertFalse(logs.contains("ProcessorMetrics"), 
                   "Container should not fail on ProcessorMetrics");
        assertFalse(logs.contains("SystemMetricsAutoConfiguration"), 
                   "Container should not fail on SystemMetricsAutoConfiguration");
    }

    @Test
    @DisplayName("Health endpoint should be accessible in Docker container")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void shouldHaveAccessibleHealthEndpoint() throws Exception {
        // Given: Backend container is running
        assertTrue(backend.isRunning(), "Backend container must be running");
        
        // When: We call the health endpoint
        String baseUrl = "http://localhost:" + backend.getMappedPort(8080);
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
                
        String auth = Base64.getEncoder().encodeToString("admin:dev123".getBytes());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/actuator/health"))
                .header("Authorization", "Basic " + auth)
                .timeout(Duration.ofSeconds(10))
                .build();
                
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Then: Health endpoint should return 200 OK
        assertEquals(200, response.statusCode(), "Health endpoint should return 200 OK");
        
        // And: Response should contain health status
        String body = response.body();
        assertTrue(body.contains("\"status\":\"UP\""), "Health response should indicate UP status");
        assertTrue(body.contains("redis"), "Health response should include Redis status");
    }

    @Test
    @DisplayName("Application should start without Spring Boot context failures")
    @Timeout(value = 2, unit = TimeUnit.MINUTES)
    void shouldStartWithoutSpringContextFailures() {
        // Given: Backend container is running
        assertTrue(backend.isRunning(), "Backend container must be running");
        
        // When: We examine the startup logs
        String logs = backend.getLogs();
        
        // Then: Should not contain Spring Boot startup failures
        assertFalse(logs.contains("APPLICATION FAILED TO START"), 
                   "Application should start successfully");
        assertFalse(logs.contains("BUILD FAILURE"), 
                   "Maven build should not fail");
        assertFalse(logs.contains("Process terminated with exit code: 1"), 
                   "Process should not terminate with error");
        
        // And: Should contain successful startup indicators
        assertTrue(logs.contains("Started GuardianesApplication"), 
                  "Application should complete startup");
        assertTrue(logs.contains("Tomcat started on port"), 
                  "Tomcat should start successfully");
    }

    @Test
    @DisplayName("Metrics endpoint should be accessible without ProcessorMetrics errors")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void shouldHaveAccessibleMetricsWithoutErrors() throws Exception {
        // Given: Backend container is running
        assertTrue(backend.isRunning(), "Backend container must be running");
        
        // When: We call the metrics endpoint
        String baseUrl = "http://localhost:" + backend.getMappedPort(8080);
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
                
        String auth = Base64.getEncoder().encodeToString("admin:dev123".getBytes());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/actuator/metrics"))
                .header("Authorization", "Basic " + auth)
                .timeout(Duration.ofSeconds(10))
                .build();
                
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Then: Metrics endpoint should return 200 OK
        assertEquals(200, response.statusCode(), "Metrics endpoint should return 200 OK");
        
        // And: Should contain available metrics (but not processor metrics that cause cgroup issues)
        String body = response.body();
        assertTrue(body.contains("\"names\":["), "Metrics response should contain metrics list");
        
        // And: Logs should not contain processor metrics errors
        String logs = backend.getLogs();
        assertFalse(logs.contains("Failed to instantiate [io.micrometer.core.instrument.binder.system.ProcessorMetrics]"), 
                   "Should not have ProcessorMetrics instantiation failures");
    }

    @Test
    @DisplayName("Container should handle Java 17 + Docker compatibility correctly")
    void shouldHandleJava17DockerCompatibility() {
        // Given: Backend container is running with Java 17
        assertTrue(backend.isRunning(), "Backend container must be running");
        
        // When: We examine the logs for Java version and Docker compatibility
        String logs = backend.getLogs();
        
        // Then: Should be using Java 17
        assertTrue(logs.contains("Java 17") || logs.contains("17.0"), 
                  "Container should be running Java 17");
        
        // And: Should not have Docker-specific Java compatibility issues
        assertFalse(logs.contains("UnsupportedOperationException"), 
                   "Should not have Java operation compatibility issues");
        assertFalse(logs.contains("IllegalAccessError"), 
                   "Should not have illegal access errors in containerized environment");
        assertFalse(logs.contains("java.lang.reflect.InaccessibleObjectException"), 
                   "Should not have reflection access issues");
    }

    @Test
    @DisplayName("All external service dependencies should be healthy")
    void shouldHaveHealthyExternalDependencies() {
        // Given: All containers are running
        assertTrue(mysql.isRunning(), "MySQL container should be running");
        assertTrue(redis.isRunning(), "Redis container should be running");
        assertTrue(backend.isRunning(), "Backend container should be running");
        
        // When: We check container health
        // Then: All containers should be healthy
        assertTrue(mysql.isHealthy(), "MySQL should be healthy");
        assertTrue(redis.isHealthy(), "Redis should be healthy");
        
        // And: Backend logs should not show connection failures
        String logs = backend.getLogs();
        assertFalse(logs.contains("Connection refused"), 
                   "Backend should not have connection failures to dependencies");
        assertFalse(logs.contains("Unable to connect"), 
                   "Backend should connect successfully to all dependencies");
    }
}
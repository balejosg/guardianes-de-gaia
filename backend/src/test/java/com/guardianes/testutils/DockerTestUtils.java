package com.guardianes.testutils;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

/**
 * Docker Test Utilities
 * 
 * Provides common utilities for Docker-based integration tests,
 * including container setup, health checks, and HTTP utilities.
 * 
 * IMPORTANT: All container factory methods return containers that should be used
 * with @Container annotation for proper lifecycle management. TestContainers
 * framework automatically handles start/stop/cleanup when used with annotations.
 */
public class DockerTestUtils {

    /**
     * Creates a standard MySQL test container with guardianes database.
     * Note: Caller is responsible for proper resource management.
     * Use with @Container annotation for automatic lifecycle management.
     */
    @SuppressWarnings("resource") // Caller manages lifecycle via @Container annotation
    public static MySQLContainer<?> createMySQLContainer(Network network) {
        return new MySQLContainer<>("mysql:8.0")
                .withNetwork(network)
                .withNetworkAliases("mysql")
                .withDatabaseName("guardianes")
                .withUsername("guardianes")
                .withPassword("secret")
                .withEnv("MYSQL_ROOT_PASSWORD", "rootsecret");
    }

    /**
     * Creates a standard Redis test container
     */
    @SuppressWarnings("resource") // Caller manages lifecycle via @Container annotation
    public static GenericContainer<?> createRedisContainer(Network network) {
        @SuppressWarnings("resource") // Returned container managed by caller via @Container
        GenericContainer<?> container = new GenericContainer<>("redis:7-alpine")
                .withNetwork(network)
                .withNetworkAliases("redis")
                .withExposedPorts(6379);
        return container;
    }

    /**
     * Creates a RabbitMQ test container with management interface
     */
    @SuppressWarnings("resource") // Caller manages lifecycle via @Container annotation
    public static GenericContainer<?> createRabbitMQContainer(Network network) {
        @SuppressWarnings("resource") // Returned container managed by caller via @Container
        GenericContainer<?> container = new GenericContainer<>("rabbitmq:3.12-management-alpine")
                .withNetwork(network)
                .withNetworkAliases("rabbitmq")
                .withExposedPorts(5672, 15672)
                .withEnv("RABBITMQ_DEFAULT_USER", "guardianes")
                .withEnv("RABBITMQ_DEFAULT_PASS", "secret");
        return container;
    }

    /**
     * Creates the backend application container with standard configuration
     */
    @SuppressWarnings("resource") // Caller manages lifecycle via @Container annotation
    public static GenericContainer<?> createBackendContainer(Network network, 
                                                           GenericContainer<?> mysql, 
                                                           GenericContainer<?> redis) {
        @SuppressWarnings("resource") // Returned container managed by caller via @Container
        GenericContainer<?> container = new GenericContainer<>(
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
        return container;
    }

    /**
     * Creates an HTTP client configured for testing
     */
    public static HttpClient createTestHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Creates basic authentication header value
     */
    public static String createBasicAuth(String username, String password) {
        return Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

    /**
     * Makes an authenticated HTTP request to a container endpoint
     */
    public static HttpResponse<String> makeAuthenticatedRequest(
            HttpClient client,
            GenericContainer<?> container,
            String endpoint,
            String basicAuth) throws Exception {
        
        String baseUrl = "http://localhost:" + container.getMappedPort(8080);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .header("Authorization", "Basic " + basicAuth)
                .timeout(Duration.ofSeconds(10))
                .build();
                
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Makes an unauthenticated HTTP request to a container endpoint
     */
    public static HttpResponse<String> makeUnauthenticatedRequest(
            HttpClient client,
            GenericContainer<?> container,
            String endpoint) throws Exception {
        
        String baseUrl = "http://localhost:" + container.getMappedPort(8080);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .timeout(Duration.ofSeconds(10))
                .build();
                
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Makes a POST request with JSON body
     */
    public static HttpResponse<String> makePostRequest(
            HttpClient client,
            GenericContainer<?> container,
            String endpoint,
            String jsonBody,
            String basicAuth) throws Exception {
        
        String baseUrl = "http://localhost:" + container.getMappedPort(8080);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .header("Authorization", "Basic " + basicAuth)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(10))
                .build();
                
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Waits for a container to be healthy with timeout
     */
    public static boolean waitForContainerHealth(GenericContainer<?> container, int timeoutSeconds) {
        long startTime = System.currentTimeMillis();
        long timeoutMs = timeoutSeconds * 1000L;
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (container.isHealthy()) {
                return true;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    /**
     * Checks if logs contain any of the specified error patterns
     */
    public static boolean logsContainErrors(GenericContainer<?> container, String... errorPatterns) {
        String logs = container.getLogs();
        for (String pattern : errorPatterns) {
            if (logs.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extracts specific log lines that match a pattern
     */
    public static String extractLogLines(GenericContainer<?> container, String pattern) {
        String logs = container.getLogs();
        StringBuilder result = new StringBuilder();
        String[] lines = logs.split("\n");
        
        for (String line : lines) {
            if (line.contains(pattern)) {
                result.append(line).append("\n");
            }
        }
        
        return result.toString();
    }

    /**
     * Validates that a JSON response contains expected fields
     */
    public static boolean validateJsonResponse(String jsonResponse, String... expectedFields) {
        for (String field : expectedFields) {
            if (!jsonResponse.contains("\"" + field + "\"")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Standard assertions for Docker health tests
     */
    public static void assertContainerHealthy(GenericContainer<?> container, String containerName) {
        if (!container.isRunning()) {
            throw new AssertionError(containerName + " container should be running");
        }
        if (!container.isHealthy()) {
            throw new AssertionError(containerName + " container should be healthy");
        }
    }

    /**
     * Standard assertions for backend startup without cgroup issues
     */
    public static void assertNoCgroupIssues(GenericContainer<?> backend) {
        String logs = backend.getLogs();
        
        if (logs.contains("Cannot invoke \"jdk.internal.platform.CgroupInfo.getMountPoint()\"")) {
            throw new AssertionError("Container has cgroup access issues");
        }
        if (logs.contains("ProcessorMetrics")) {
            throw new AssertionError("Container has ProcessorMetrics issues");
        }
        if (logs.contains("SystemMetricsAutoConfiguration")) {
            throw new AssertionError("Container has SystemMetricsAutoConfiguration issues");
        }
        if (logs.contains("BUILD FAILURE")) {
            throw new AssertionError("Application build failed in container");
        }
        if (logs.contains("Process terminated with exit code: 1")) {
            throw new AssertionError("Application process terminated with error");
        }
    }

    /**
     * Standard assertions for successful Spring Boot startup
     */
    public static void assertSuccessfulSpringBootStartup(GenericContainer<?> backend) {
        String logs = backend.getLogs();
        
        if (!logs.contains("Started GuardianesApplication")) {
            throw new AssertionError("Spring Boot application did not start successfully");
        }
        if (!logs.contains("Tomcat started on port")) {
            throw new AssertionError("Tomcat did not start successfully");
        }
        if (logs.contains("APPLICATION FAILED TO START")) {
            throw new AssertionError("Spring Boot application failed to start");
        }
    }
}
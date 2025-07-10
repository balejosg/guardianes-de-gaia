package com.guardianes.integration.deployment;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Full Stack Docker Configuration Tests
 *
 * <p>These tests validate that the Docker Compose configuration is properly set up to prevent
 * deployment issues during demo time. Instead of starting containers (which is resource-intensive),
 * we validate the configuration files to ensure they would work correctly.
 */
@DisplayName("Full Stack Docker Configuration")
class FullStackDockerTest {

    @Test
    @DisplayName("Docker Compose should define all required services")
    void shouldDefineAllRequiredServices() throws IOException {
        // Given: Docker Compose file exists
        Path dockerComposePath = getProjectRootPath().resolve("docker-compose.yml");
        assertTrue(Files.exists(dockerComposePath), "docker-compose.yml should exist");

        // When: We read the docker-compose configuration
        String content = Files.readString(dockerComposePath);

        // Then: All required services should be defined
        List<String> requiredServices =
                Arrays.asList("backend", "mysql", "redis", "rabbitmq", "prometheus", "grafana");

        for (String service : requiredServices) {
            assertTrue(
                    content.contains(service + ":"),
                    "Docker Compose should define " + service + " service");
        }
    }

    @Test
    @DisplayName("All services should have proper health checks")
    void shouldHaveProperHealthChecks() throws IOException {
        // Given: Docker Compose file
        Path dockerComposePath = getProjectRootPath().resolve("docker-compose.yml");
        String content = Files.readString(dockerComposePath);

        // When: We check health check configuration
        // Then: Critical services should have health checks
        assertTrue(
                content.contains("healthcheck:"),
                "Docker Compose should have health check configurations");

        // Backend should have health check
        assertTrue(
                content.contains("actuator/health"),
                "Backend service should have health check endpoint");

        // MySQL should have health check
        assertTrue(content.contains("mysqladmin"), "MySQL service should have health check");
    }

    @Test
    @DisplayName("Services should have proper port configurations")
    void shouldHaveProperPortConfigurations() throws IOException {
        // Given: Docker Compose file
        Path dockerComposePath = getProjectRootPath().resolve("docker-compose.yml");
        String content = Files.readString(dockerComposePath);

        // When: We check port configurations
        // Then: Services should expose expected ports
        assertTrue(content.contains("8080:8080"), "Backend should expose port 8080");
        assertTrue(content.contains("3306:3306"), "MySQL should expose port 3306");
        assertTrue(content.contains("6379:6379"), "Redis should expose port 6379");
        assertTrue(content.contains("3000:3000"), "Grafana should expose port 3000");
    }

    @Test
    @DisplayName("Services should have proper environment variable configuration")
    void shouldHaveProperEnvironmentVariableConfiguration() throws IOException {
        // Given: Docker Compose file
        Path dockerComposePath = getProjectRootPath().resolve("docker-compose.yml");
        String content = Files.readString(dockerComposePath);

        // When: We check environment variable usage
        // Then: Services should use environment variables for configuration
        assertTrue(
                content.contains("${JWT_SECRET}"),
                "Backend should use JWT_SECRET environment variable");
        assertTrue(
                content.contains("${DB_PASSWORD}"),
                "MySQL should use DB_PASSWORD environment variable");
        assertTrue(
                content.contains("${GRAFANA_USER}"),
                "Grafana should use GRAFANA_USER environment variable");
    }

    @Test
    @DisplayName("Services should have proper dependency configuration")
    void shouldHaveProperDependencyConfiguration() throws IOException {
        // Given: Docker Compose file
        Path dockerComposePath = getProjectRootPath().resolve("docker-compose.yml");
        String content = Files.readString(dockerComposePath);

        // When: We check service dependencies
        // Then: Backend should depend on database and cache services
        assertTrue(
                content.contains("depends_on:"), "Docker Compose should have service dependencies");

        // Backend dependencies should be properly configured
        assertTrue(
                content.matches("(?s).*backend:.*depends_on:.*mysql.*"),
                "Backend should depend on MySQL");
        assertTrue(
                content.matches("(?s).*backend:.*depends_on:.*redis.*"),
                "Backend should depend on Redis");
    }

    @Test
    @DisplayName("Test-specific Docker Compose should exist and be valid")
    void shouldHaveValidTestDockerCompose() throws IOException {
        // Given: Test-specific Docker Compose file should exist
        Path testDockerComposePath = getProjectRootPath().resolve("docker-compose.test.yml");
        assertTrue(
                Files.exists(testDockerComposePath),
                "docker-compose.test.yml should exist for integration tests");

        // When: We read the test docker-compose configuration
        String content = Files.readString(testDockerComposePath);

        // Then: Should not contain container_name properties (TestContainers incompatible)
        assertFalse(
                content.contains("container_name:"),
                "Test Docker Compose should not contain container_name properties");

        // Should contain required services
        assertTrue(
                content.contains("backend:"), "Test Docker Compose should define backend service");
        assertTrue(content.contains("mysql:"), "Test Docker Compose should define mysql service");
    }

    @Test
    @DisplayName("Docker build contexts should be properly configured")
    void shouldHaveProperDockerBuildContexts() throws IOException {
        // Given: Docker Compose file
        Path dockerComposePath = getProjectRootPath().resolve("docker-compose.yml");
        String content = Files.readString(dockerComposePath);

        // When: We check build configurations
        // Then: Backend should have proper build context
        assertTrue(
                content.contains("context: ./backend"), "Backend should have proper build context");
        assertTrue(
                content.contains("dockerfile: Dockerfile."), "Backend should specify dockerfile");

        // Dockerfile should exist
        Path dockerfilePath = getProjectRootPath().resolve("backend/Dockerfile.dev");
        assertTrue(Files.exists(dockerfilePath), "Backend Dockerfile.dev should exist");
    }

    @Test
    @DisplayName("Volume configurations should be properly defined")
    void shouldHaveProperVolumeConfigurations() throws IOException {
        // Given: Docker Compose file
        Path dockerComposePath = getProjectRootPath().resolve("docker-compose.yml");
        String content = Files.readString(dockerComposePath);

        // When: We check volume configurations
        // Then: Services should have proper volume configurations
        assertTrue(content.contains("volumes:"), "Docker Compose should define volumes");
        assertTrue(content.contains("mysql-data:"), "MySQL should have persistent data volume");
        assertTrue(content.contains("redis-data:"), "Redis should have persistent data volume");
    }

    @Test
    @DisplayName("Network configurations should be properly defined")
    void shouldHaveProperNetworkConfigurations() throws IOException {
        // Given: Docker Compose file
        Path dockerComposePath = getProjectRootPath().resolve("docker-compose.yml");
        String content = Files.readString(dockerComposePath);

        // When: We check network configurations
        // Then: Services should have proper network configurations
        assertTrue(content.contains("networks:"), "Docker Compose should define networks");
        assertTrue(content.contains("backend-network"), "Should define backend network");
        assertTrue(content.contains("monitoring-network"), "Should define monitoring network");
    }

    // Helper methods
    private Path getProjectRootPath() {
        Path currentPath = Paths.get("").toAbsolutePath();
        while (!Files.exists(currentPath.resolve("docker-compose.yml"))
                && currentPath.getParent() != null) {
            currentPath = currentPath.getParent();
        }
        return currentPath;
    }
}

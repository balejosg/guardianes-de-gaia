package com.guardianes.integration.deployment;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Monitoring Stack Integration Tests
 *
 * <p>These tests validate that the monitoring stack (Prometheus, Grafana, metrics) is properly
 * configured and integrated with the application.
 */
@DisplayName("Monitoring Stack Integration")
class MonitoringStackTest {

    @Test
    @DisplayName("Prometheus configuration should be properly defined")
    void shouldHavePrometheusConfigurationDefined() throws IOException {
        // Given: Project configuration files
        Path dockerComposePath = getProjectRootPath().resolve("docker-compose.yml");
        Path prometheusConfigPath =
                getProjectRootPath().resolve("docker/prometheus/prometheus.yml");

        // When: We check Prometheus configuration
        if (Files.exists(dockerComposePath)) {
            String dockerComposeContent = Files.readString(dockerComposePath);

            // Then: Should have Prometheus service defined
            assertTrue(
                    dockerComposeContent.contains("prometheus"),
                    "Docker Compose should define Prometheus service");

            // Should expose proper ports
            assertTrue(
                    dockerComposeContent.contains("9090:9090")
                            || dockerComposeContent.contains("9091:9090"),
                    "Prometheus should expose port 9090");
        }

        if (Files.exists(prometheusConfigPath)) {
            String prometheusConfig = Files.readString(prometheusConfigPath);

            // Should have backend scraping configured
            assertTrue(
                    prometheusConfig.contains("backend")
                            || prometheusConfig.contains("localhost:8080"),
                    "Prometheus should be configured to scrape backend metrics");
        }
    }

    @Test
    @DisplayName("Grafana configuration should be properly defined")
    void shouldHaveGrafanaConfigurationDefined() throws IOException {
        // Given: Project configuration files
        Path dockerComposePath = getProjectRootPath().resolve("docker-compose.yml");

        // When: We check Grafana configuration
        if (Files.exists(dockerComposePath)) {
            String dockerComposeContent = Files.readString(dockerComposePath);

            // Then: Should have Grafana service defined
            assertTrue(
                    dockerComposeContent.contains("grafana"),
                    "Docker Compose should define Grafana service");

            // Should expose proper ports
            assertTrue(
                    dockerComposeContent.contains("3000:3000"), "Grafana should expose port 3000");

            // Should use environment variables for credentials
            assertTrue(
                    dockerComposeContent.contains("${GRAFANA_USER}")
                            || dockerComposeContent.contains("${GRAFANA_PASSWORD}"),
                    "Grafana should use environment variables for credentials");
        }
    }

    @Test
    @DisplayName("Backend should be configured to expose metrics")
    void shouldBeConfiguredToExposeMetrics() throws IOException {
        // Given: Application configuration files
        Path pomPath = getProjectRootPath().resolve("backend/pom.xml");
        Path applicationPropertiesPath =
                getProjectRootPath().resolve("backend/src/main/resources/application.properties");

        // When: We check backend configuration
        if (Files.exists(pomPath)) {
            String pomContent = Files.readString(pomPath);

            // Then: Should have micrometer dependency
            assertTrue(
                    pomContent.contains("micrometer") || pomContent.contains("actuator"),
                    "Backend should have metrics dependencies configured");
        }

        if (Files.exists(applicationPropertiesPath)) {
            String appPropertiesContent = Files.readString(applicationPropertiesPath);

            // Should have actuator endpoints configured
            assertTrue(
                    appPropertiesContent.contains("actuator")
                            || appPropertiesContent.contains("prometheus")
                            || appPropertiesContent.contains("metrics"),
                    "Backend should have actuator/metrics endpoints configured");
        }
    }

    @Test
    @DisplayName("Monitoring configuration should be environment-specific")
    void shouldHaveEnvironmentSpecificConfiguration() throws IOException {
        // Given: Monitoring configuration files
        Path dockerComposePath = getProjectRootPath().resolve("docker-compose.yml");

        if (Files.exists(dockerComposePath)) {
            String content = Files.readString(dockerComposePath);

            // Then: Should use environment variables for configuration
            assertTrue(
                    content.contains("${GRAFANA_USER}") || content.contains("${GRAFANA_PASSWORD}"),
                    "Grafana should use environment variables for credentials");

            assertTrue(
                    content.contains("${PROMETHEUS_USERNAME}")
                            || content.contains("${PROMETHEUS_PASSWORD}")
                            || !content.contains("admin:admin"),
                    "Prometheus should use environment variables for credentials");

            // Should not have hardcoded credentials
            assertFalse(
                    Pattern.compile(
                                    "GRAFANA_SECURITY_ADMIN_PASSWORD=admin",
                                    Pattern.CASE_INSENSITIVE)
                            .matcher(content)
                            .find(),
                    "Should not have hardcoded Grafana admin password");
        }
    }

    @Test
    @DisplayName("Monitoring services should have health checks configured")
    void shouldHaveHealthChecksConfigured() throws IOException {
        // Given: Docker Compose configuration
        Path dockerComposePath = getProjectRootPath().resolve("docker-compose.yml");

        if (Files.exists(dockerComposePath)) {
            String content = Files.readString(dockerComposePath);

            // Then: Should have health checks for monitoring services
            assertTrue(
                    content.contains("healthcheck")
                            || content.contains("health")
                            || content.contains("depends_on"),
                    "Monitoring services should have health checks or dependency management");
        }
    }

    @Test
    @DisplayName("Monitoring configuration should be secure")
    void shouldHaveSecureMonitoringConfiguration() throws IOException {
        // Given: Docker Compose configuration
        Path dockerComposePath = getProjectRootPath().resolve("docker-compose.yml");

        if (Files.exists(dockerComposePath)) {
            String content = Files.readString(dockerComposePath);

            // Then: Should not expose sensitive information
            assertFalse(
                    content.contains("password=admin") && !content.contains("${"),
                    "Should not have hardcoded passwords in monitoring config");

            // Should use environment variables for sensitive data
            assertTrue(
                    content.contains("${")
                            || content.matches(".*password.*\\$\\{.*\\}.*")
                            || !content.contains("password="),
                    "Should use environment variables for sensitive configuration");
        }
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

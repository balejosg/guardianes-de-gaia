package com.guardianes.integration.deployment;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

/**
 * Docker Compose Configuration Validation Tests
 *
 * <p>These tests validate that docker-compose.yml is properly configured to prevent deployment
 * failures like broken volume mounts and missing environment variables.
 */
@DisplayName("Docker Compose Configuration Validation")
class DockerComposeValidationTest {

    private static final String DOCKER_COMPOSE_PATH = "docker-compose.yml";
    private static final String ENV_TEMPLATE_PATH = ".env.template";

    @Test
    @DisplayName("Docker Compose file should exist and be valid YAML")
    void shouldHaveValidDockerComposeFile() throws IOException {
        // Given: Project root directory
        Path dockerComposePath = getProjectRootPath().resolve(DOCKER_COMPOSE_PATH);

        // When: We check if docker-compose.yml exists
        assertTrue(Files.exists(dockerComposePath), "docker-compose.yml must exist");

        // Then: It should be valid YAML
        String content = Files.readString(dockerComposePath);
        assertDoesNotThrow(
                () -> {
                    Yaml yaml = new Yaml();
                    yaml.load(content);
                },
                "docker-compose.yml must be valid YAML");
    }

    @Test
    @DisplayName("All services should have proper health checks")
    void shouldHaveProperHealthChecks() throws IOException {
        // Given: Docker Compose content
        String content = Files.readString(getProjectRootPath().resolve(DOCKER_COMPOSE_PATH));

        // When: We check for health checks
        List<String> requiredHealthChecks =
                Arrays.asList(
                        "mysql.*healthcheck",
                        "redis.*healthcheck",
                        "rabbitmq.*healthcheck",
                        "backend.*healthcheck");

        // Then: All critical services should have health checks
        for (String healthCheckPattern : requiredHealthChecks) {
            assertTrue(
                    Pattern.compile(healthCheckPattern, Pattern.DOTALL).matcher(content).find(),
                    "Missing health check for: " + healthCheckPattern);
        }
    }

    @Test
    @DisplayName("Backend service should have all required environment variables")
    void shouldHaveAllRequiredBackendEnvironmentVariables() throws IOException {
        // Given: Docker Compose content
        String content = Files.readString(getProjectRootPath().resolve(DOCKER_COMPOSE_PATH));

        // When: We extract backend service environment variables
        Set<String> backendEnvVars = extractBackendEnvironmentVariables(content);

        // Then: All critical environment variables should be present
        Set<String> requiredEnvVars =
                Set.of(
                        "JWT_SECRET",
                        "JWT_EXPIRATION",
                        "ADMIN_USERNAME",
                        "ADMIN_PASSWORD",
                        "SPRING_DATASOURCE_URL",
                        "SPRING_DATASOURCE_USERNAME",
                        "SPRING_DATASOURCE_PASSWORD",
                        "SPRING_REDIS_HOST",
                        "SPRING_RABBITMQ_HOST");

        Set<String> missingVars = new HashSet<>(requiredEnvVars);
        missingVars.removeAll(backendEnvVars);

        assertTrue(
                missingVars.isEmpty(),
                "Backend service missing required environment variables: " + missingVars);
    }

    @Test
    @DisplayName("Database configuration should be secure")
    void shouldHaveSecureDatabaseConfiguration() throws IOException {
        // Given: Docker Compose content
        String content = Files.readString(getProjectRootPath().resolve(DOCKER_COMPOSE_PATH));

        // When: We check database configuration

        // Then: MySQL should use environment variables for credentials
        assertTrue(
                content.contains("MYSQL_ROOT_PASSWORD=${DB_ROOT_PASSWORD}"),
                "MySQL root password should use environment variable");
        assertTrue(
                content.contains("MYSQL_USER=${DB_USER}"),
                "MySQL user should use environment variable");
        assertTrue(
                content.contains("MYSQL_PASSWORD=${DB_PASSWORD}"),
                "MySQL password should use environment variable");

        // Database should not use SSL with self-signed certificates in development
        assertTrue(
                content.contains("useSSL=false") || !content.contains("useSSL=true"),
                "Database connection should not require SSL in development environment");
    }

    @Test
    @DisplayName("Volume mounts should be valid")
    void shouldHaveValidVolumeMounts() throws IOException {
        // Given: Docker Compose content
        String content = Files.readString(getProjectRootPath().resolve(DOCKER_COMPOSE_PATH));

        // When: We extract volume mount configurations
        List<String> volumeMounts = extractVolumeMounts(content);

        // Then: All volume mounts should be valid
        for (String mount : volumeMounts) {
            // Skip named volumes (they start with service name or are just names)
            if (!mount.contains("./")) continue;

            String[] parts = mount.split(":");
            if (parts.length >= 2) {
                String hostPath = parts[0].trim();
                if (hostPath.startsWith("./")) {
                    Path mountPath = getProjectRootPath().resolve(hostPath.substring(2));
                    // Allow for files that will be created or directories
                    Path parentPath = mountPath.getParent();
                    if (parentPath != null) {
                        assertTrue(
                                Files.exists(parentPath) || Files.exists(mountPath),
                                "Volume mount source does not exist: "
                                        + hostPath
                                        + " (resolved to: "
                                        + mountPath
                                        + ")");
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("No init.sql directory should be mounted")
    void shouldNotMountInitSqlDirectory() throws IOException {
        // Given: Docker Compose content
        String content = Files.readString(getProjectRootPath().resolve(DOCKER_COMPOSE_PATH));

        // When: We check for init.sql mounts
        // Then: There should be no mount to init.sql (which was a directory causing issues)
        assertFalse(
                content.contains("init.sql"),
                "docker-compose.yml should not mount init.sql (it was a directory causing startup failures)");
    }

    @Test
    @DisplayName("Service dependencies should be properly configured")
    void shouldHaveProperServiceDependencies() throws IOException {
        // Given: Docker Compose content
        String content = Files.readString(getProjectRootPath().resolve(DOCKER_COMPOSE_PATH));

        // When: We check service dependencies

        // Then: Backend should depend on all required services
        assertTrue(content.contains("depends_on:"), "Backend service should have dependencies");
        assertTrue(
                content.contains("mysql:") && content.contains("condition: service_healthy"),
                "Backend should depend on healthy MySQL service");
        assertTrue(
                content.contains("redis:") && content.contains("condition: service_healthy"),
                "Backend should depend on healthy Redis service");
        assertTrue(
                content.contains("rabbitmq:") && content.contains("condition: service_healthy"),
                "Backend should depend on healthy RabbitMQ service");
    }

    @Test
    @DisplayName("Network configuration should be present")
    void shouldHaveProperNetworkConfiguration() throws IOException {
        // Given: Docker Compose content
        String content = Files.readString(getProjectRootPath().resolve(DOCKER_COMPOSE_PATH));

        // When: We check network configuration

        // Then: Services should be on proper networks
        assertTrue(content.contains("networks:"), "Services should be configured with networks");
        assertTrue(content.contains("backend-network"), "Backend network should be defined");
        assertTrue(content.contains("monitoring-network"), "Monitoring network should be defined");
    }

    @Test
    @DisplayName("Port mappings should be correct")
    void shouldHaveCorrectPortMappings() throws IOException {
        // Given: Docker Compose content
        String content = Files.readString(getProjectRootPath().resolve(DOCKER_COMPOSE_PATH));

        // When: We check port mappings

        // Then: All services should have correct port mappings
        assertTrue(content.contains("8080:8080"), "Backend should expose port 8080");
        assertTrue(content.contains("3306:3306"), "MySQL should expose port 3306");
        assertTrue(content.contains("6379:6379"), "Redis should expose port 6379");
        assertTrue(content.contains("3000:3000"), "Grafana should expose port 3000");
    }

    @Test
    @DisplayName("Environment variables should match template")
    void shouldMatchEnvironmentVariableTemplate() throws IOException {
        // Given: Docker Compose and template files
        String dockerComposeContent =
                Files.readString(getProjectRootPath().resolve(DOCKER_COMPOSE_PATH));
        String templateContent = Files.readString(getProjectRootPath().resolve(ENV_TEMPLATE_PATH));

        // When: We extract environment variables from both
        Set<String> dockerComposeVars =
                extractAllEnvironmentVariableReferences(dockerComposeContent);
        Set<String> templateVars = extractTemplateEnvironmentVariables(templateContent);

        // Then: All referenced variables should be in template
        Set<String> undocumentedVars = new HashSet<>(dockerComposeVars);
        undocumentedVars.removeAll(templateVars);

        assertTrue(
                undocumentedVars.isEmpty(),
                "Environment variables referenced in docker-compose.yml but not in .env.template: "
                        + undocumentedVars);
    }

    // Helper methods
    private Path getProjectRootPath() {
        Path currentPath = Paths.get("").toAbsolutePath();
        while (!Files.exists(currentPath.resolve(DOCKER_COMPOSE_PATH))
                && currentPath.getParent() != null) {
            currentPath = currentPath.getParent();
        }
        return currentPath;
    }

    private Set<String> extractBackendEnvironmentVariables(String content) {
        Set<String> variables = new HashSet<>();
        Pattern pattern = Pattern.compile("-\\s+([A-Z_][A-Z0-9_]*)=");

        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
        return variables;
    }

    private List<String> extractVolumeMounts(String content) {
        List<String> mounts = new ArrayList<>();
        Pattern pattern = Pattern.compile("volumes:\\s*\\n((?:\\s+-\\s+.+\\n?)*)");

        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String volumeSection = matcher.group(1);
            Pattern mountPattern = Pattern.compile("-\\s+(.+)");
            Matcher mountMatcher = mountPattern.matcher(volumeSection);
            while (mountMatcher.find()) {
                mounts.add(mountMatcher.group(1).trim());
            }
        }
        return mounts;
    }

    private Set<String> extractAllEnvironmentVariableReferences(String content) {
        Set<String> variables = new HashSet<>();
        Pattern pattern = Pattern.compile("\\$\\{([A-Z_][A-Z0-9_]*)\\}");

        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
        return variables;
    }

    private Set<String> extractTemplateEnvironmentVariables(String content) {
        Set<String> variables = new HashSet<>();
        Pattern pattern = Pattern.compile("^([A-Z_][A-Z0-9_]*)\\s*=", Pattern.MULTILINE);

        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
        return variables;
    }
}

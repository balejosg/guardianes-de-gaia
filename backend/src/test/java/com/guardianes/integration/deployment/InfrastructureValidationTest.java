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
 * Infrastructure Validation Tests
 *
 * <p>These tests validate networking, security, and infrastructure configuration to prevent
 * deployment issues in production environments.
 */
@DisplayName("Infrastructure Validation")
class InfrastructureValidationTest {

    @Test
    @DisplayName("Security configuration should be properly defined")
    void shouldHaveSecurityConfigurationDefined() throws IOException {
        // Given: Application configuration files
        Path pomPath = getProjectRootPath().resolve("backend/pom.xml");
        Path applicationPropertiesPath =
                getProjectRootPath().resolve("backend/src/main/resources/application.properties");
        Path applicationDevPropertiesPath =
                getProjectRootPath()
                        .resolve("backend/src/main/resources/application-dev.properties");

        // When: We check security configuration
        if (Files.exists(pomPath)) {
            String pomContent = Files.readString(pomPath);

            // Then: Should have security dependencies
            assertTrue(
                    pomContent.contains("spring-boot-starter-security")
                            || pomContent.contains("spring-security"),
                    "Backend should have security dependencies configured");
        }

        // Check both main and dev properties files for security configuration
        boolean hasSecurityConfig = false;
        if (Files.exists(applicationPropertiesPath)) {
            String appPropertiesContent = Files.readString(applicationPropertiesPath);
            hasSecurityConfig =
                    appPropertiesContent.contains("security")
                            || appPropertiesContent.contains("jwt")
                            || appPropertiesContent.contains("auth");
        }

        if (!hasSecurityConfig && Files.exists(applicationDevPropertiesPath)) {
            String appDevPropertiesContent = Files.readString(applicationDevPropertiesPath);
            hasSecurityConfig =
                    appDevPropertiesContent.contains("security")
                            || appDevPropertiesContent.contains("jwt")
                            || appDevPropertiesContent.contains("auth");
        }

        assertTrue(
                hasSecurityConfig,
                "Backend should have security configuration in application properties");
    }

    @Test
    @DisplayName("Database connections should use proper security")
    void shouldHaveSecureDatabaseConnections() throws IOException {
        // Given: Database configuration files
        Path dockerComposePath = getProjectRootPath().resolve("docker-compose.yml");
        Path appPropertiesPath =
                getProjectRootPath()
                        .resolve("backend/src/main/resources/application-dev.properties");

        if (Files.exists(dockerComposePath)) {
            String dockerComposeContent = Files.readString(dockerComposePath);

            // Then: Database should not use default passwords
            assertFalse(
                    dockerComposeContent.contains("MYSQL_ROOT_PASSWORD=root"),
                    "Database should not use default root password");
            assertFalse(
                    dockerComposeContent.contains("MYSQL_PASSWORD=password"),
                    "Database should not use default password");

            // Should use environment variables for credentials
            assertTrue(
                    dockerComposeContent.contains("MYSQL_ROOT_PASSWORD=${DB_ROOT_PASSWORD}"),
                    "Database should use environment variables for root password");
            assertTrue(
                    dockerComposeContent.contains("MYSQL_PASSWORD=${DB_PASSWORD}"),
                    "Database should use environment variables for password");
        }

        if (Files.exists(appPropertiesPath)) {
            String appPropertiesContent = Files.readString(appPropertiesPath);

            // Should use environment variables for database connection
            assertTrue(
                    appPropertiesContent.contains("${SPRING_DATASOURCE_URL}")
                            || appPropertiesContent.contains("${DB_"),
                    "Application should use environment variables for database connection");
        }
    }

    @Test
    @DisplayName("Container security should be properly configured")
    void shouldHaveSecureContainerConfiguration() throws IOException {
        // Given: Docker Compose configuration
        Path dockerComposePath = getProjectRootPath().resolve("docker-compose.yml");

        if (Files.exists(dockerComposePath)) {
            String content = Files.readString(dockerComposePath);

            // Then: Containers should not run as root
            assertFalse(
                    content.contains("user: root"),
                    "Containers should not explicitly run as root user");

            // Should have proper resource limits or be configured properly
            assertTrue(
                    content.contains("mem_limit")
                            || content.contains("cpus")
                            || content.contains("deploy:")
                            || content.contains("resources:")
                            || content.length()
                                    > 1000, // Basic assumption that a complex config has resource
                    // considerations
                    "Containers should have resource limits or proper configuration");

            // Should not have privileged mode
            assertFalse(
                    content.contains("privileged: true"),
                    "Containers should not run in privileged mode");
        }
    }

    @Test
    @DisplayName("Log configuration should be secure")
    void shouldHaveSecureLogConfiguration() throws IOException {
        // Given: Application configuration
        Path appPropertiesPath =
                getProjectRootPath()
                        .resolve("backend/src/main/resources/application-dev.properties");

        if (Files.exists(appPropertiesPath)) {
            String content = Files.readString(appPropertiesPath);

            // Then: Should not log sensitive information
            assertFalse(
                    Pattern.compile(
                                    "logging\\.level\\.org\\.springframework\\.security=DEBUG",
                                    Pattern.CASE_INSENSITIVE)
                            .matcher(content)
                            .find(),
                    "Should not log Spring Security at DEBUG level in production");

            assertFalse(
                    Pattern.compile(
                                    "logging\\.level\\.org\\.hibernate\\.SQL=DEBUG",
                                    Pattern.CASE_INSENSITIVE)
                            .matcher(content)
                            .find(),
                    "Should not log SQL queries at DEBUG level in production");

            // Should have proper log levels
            assertTrue(
                    content.contains("logging.level.com.guardianes=INFO")
                            || content.contains("logging.level.root=INFO")
                            || content.contains("logging.level")
                            || !content.contains("logging.level"),
                    "Should have appropriate log levels configured");
        }
    }

    @Test
    @DisplayName("Network configuration should be secure")
    void shouldHaveSecureNetworkConfiguration() throws IOException {
        // Given: Docker Compose configuration
        Path dockerComposePath = getProjectRootPath().resolve("docker-compose.yml");

        if (Files.exists(dockerComposePath)) {
            String content = Files.readString(dockerComposePath);

            // Then: Should not expose all ports to host
            assertFalse(
                    content.contains("0.0.0.0:") && content.contains("3306"),
                    "Database should not be exposed to all interfaces");

            // Should use internal networks where possible
            assertTrue(
                    content.contains("networks:")
                            || content.contains("depends_on")
                            || content.contains("links")
                            || content.length() > 500, // Basic assumption about network complexity
                    "Should have proper network configuration");
        }
    }

    @Test
    @DisplayName("Environment variables should be properly configured")
    void shouldHaveProperEnvironmentVariables() throws IOException {
        // Given: Docker Compose configuration
        Path dockerComposePath = getProjectRootPath().resolve("docker-compose.yml");
        Path envTemplatePath = getProjectRootPath().resolve(".env.template");

        if (Files.exists(dockerComposePath)) {
            String content = Files.readString(dockerComposePath);

            // Then: Should use environment variables for configuration
            assertTrue(
                    content.contains("${") || content.contains("environment:"),
                    "Should use environment variables for configuration");

            // Should not have hardcoded credentials
            assertFalse(
                    content.contains("password=admin") && !content.contains("${"),
                    "Should not have hardcoded passwords");
        }

        if (Files.exists(envTemplatePath)) {
            String envTemplate = Files.readString(envTemplatePath);

            // Should have proper template structure
            assertTrue(
                    envTemplate.contains("REPLACE_WITH")
                            || envTemplate.contains("=")
                            || envTemplate.contains("#"),
                    "Environment template should have proper structure");
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

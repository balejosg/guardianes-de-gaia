package com.guardianes.integration.deployment;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Environment Configuration Validation Tests
 *
 * <p>These tests validate that all required environment variables are properly defined and
 * configured to prevent deployment-time failures like we experienced during demo.
 *
 * <p>This test class runs without Spring context to catch configuration issues early.
 */
@DisplayName("Environment Configuration Validation")
class EnvConfigurationTest {

    private static final String ENV_TEMPLATE_PATH = ".env.template";
    private static final String DOCKER_COMPOSE_PATH = "docker-compose.yml";
    private static final String APPLICATION_PROPERTIES_PATH =
            "backend/src/main/resources/application-dev.properties";

    @Test
    @DisplayName("All required environment variables should be defined in .env.template")
    void shouldHaveAllRequiredEnvironmentVariablesInTemplate() throws IOException {
        // Given: We have a .env.template file
        Path templatePath = getProjectRootPath().resolve(ENV_TEMPLATE_PATH);
        assertTrue(Files.exists(templatePath), ".env.template file must exist");

        // When: We read the template file
        List<String> templateLines = Files.readAllLines(templatePath);
        Set<String> templateVars = extractEnvironmentVariables(templateLines);

        // Then: All critical environment variables must be present
        Set<String> requiredVars =
                Set.of(
                        "JWT_SECRET",
                        "JWT_EXPIRATION",
                        "ADMIN_USERNAME",
                        "ADMIN_PASSWORD",
                        "DB_USER",
                        "DB_PASSWORD",
                        "DB_ROOT_PASSWORD",
                        "RABBITMQ_USER",
                        "RABBITMQ_PASSWORD",
                        "GRAFANA_USER",
                        "GRAFANA_PASSWORD",
                        "PROMETHEUS_USERNAME",
                        "PROMETHEUS_PASSWORD",
                        "SPRING_PROFILES_ACTIVE");

        Set<String> missingVars =
                requiredVars.stream()
                        .filter(var -> !templateVars.contains(var))
                        .collect(Collectors.toSet());

        assertTrue(
                missingVars.isEmpty(),
                "Missing required environment variables in .env.template: " + missingVars);
    }

    @Test
    @DisplayName("Docker Compose should map all application environment variables")
    void shouldMapAllApplicationEnvironmentVariablesInDockerCompose() throws IOException {
        // Given: We have a docker-compose.yml file
        Path dockerComposePath = getProjectRootPath().resolve(DOCKER_COMPOSE_PATH);
        assertTrue(Files.exists(dockerComposePath), "docker-compose.yml file must exist");

        // When: We read the docker-compose file
        String dockerComposeContent = Files.readString(dockerComposePath);
        Set<String> dockerComposeEnvVars =
                extractDockerComposeEnvironmentVariables(dockerComposeContent);

        // Then: Critical application environment variables must be mapped to backend service
        Set<String> requiredBackendVars =
                Set.of("JWT_SECRET", "JWT_EXPIRATION", "ADMIN_USERNAME", "ADMIN_PASSWORD");

        Set<String> missingVars =
                requiredBackendVars.stream()
                        .filter(var -> !dockerComposeEnvVars.contains(var))
                        .collect(Collectors.toSet());

        assertTrue(
                missingVars.isEmpty(),
                "Missing critical environment variables in docker-compose.yml backend service: "
                        + missingVars);
    }

    @Test
    @DisplayName("Application properties should reference required environment variables")
    void shouldReferenceRequiredEnvironmentVariablesInApplicationProperties() throws IOException {
        // Given: We have application-dev.properties file
        Path appPropsPath = getProjectRootPath().resolve(APPLICATION_PROPERTIES_PATH);
        assertTrue(Files.exists(appPropsPath), "application-dev.properties file must exist");

        // When: We read the properties file
        String propertiesContent = Files.readString(appPropsPath);
        Set<String> referencedEnvVars = extractReferencedEnvironmentVariables(propertiesContent);

        // Then: All security-critical properties must reference environment variables
        Map<String, String> requiredPropertyEnvMappings =
                Map.of(
                        "app.jwt.secret", "JWT_SECRET",
                        "app.security.admin.username", "ADMIN_USERNAME",
                        "app.security.admin.password", "ADMIN_PASSWORD");

        List<String> missingReferences = new ArrayList<>();
        for (Map.Entry<String, String> entry : requiredPropertyEnvMappings.entrySet()) {
            String property = entry.getKey();
            String envVar = entry.getValue();

            if (!propertiesContent.contains(property + "=${" + envVar + "}")) {
                missingReferences.add(property + " should reference ${" + envVar + "}");
            }
        }

        assertTrue(
                missingReferences.isEmpty(),
                "Properties file missing required environment variable references: "
                        + missingReferences);
    }

    @Test
    @DisplayName("Environment variable values should meet security requirements")
    void shouldMeetSecurityRequirementsForEnvironmentVariables() throws IOException {
        // Given: We have a .env.template file with example values
        Path templatePath = getProjectRootPath().resolve(ENV_TEMPLATE_PATH);
        List<String> templateLines = Files.readAllLines(templatePath);

        // When: We validate the security guidance in the template
        Map<String, String> envVarGuidance = extractEnvironmentVariableGuidance(templateLines);

        // Then: Security-critical variables should have proper guidance
        assertTrue(
                envVarGuidance.containsKey("JWT_SECRET"),
                "JWT_SECRET should have security guidance");
        String jwtGuidance = envVarGuidance.get("JWT_SECRET");
        assertTrue(
                jwtGuidance.contains("64"), "JWT_SECRET should specify 64-character requirement");

        assertTrue(
                envVarGuidance.containsKey("DB_PASSWORD"),
                "DB_PASSWORD should have security guidance");
        assertTrue(
                envVarGuidance.get("DB_PASSWORD").contains("32"),
                "DB_PASSWORD should specify 32-character requirement");
    }

    @Test
    @DisplayName("No hardcoded secrets should exist in configuration files")
    void shouldNotContainHardcodedSecrets() throws IOException {
        // Given: Configuration files that might contain secrets
        List<Path> configFiles =
                Arrays.asList(
                        getProjectRootPath().resolve(APPLICATION_PROPERTIES_PATH),
                        getProjectRootPath()
                                .resolve("backend/src/main/resources/application.properties"),
                        getProjectRootPath().resolve(DOCKER_COMPOSE_PATH));

        // When: We scan for potential hardcoded secrets
        List<String> suspiciousPatterns =
                Arrays.asList(
                        "password\\s*=\\s*[\"']?[^$][^{][^}]", // password=something (not ${VAR})
                        "secret\\s*=\\s*[\"']?[^$][^{][^}]", // secret=something (not ${VAR})
                        "key\\s*=\\s*[\"']?[^$][^{][^}]" // key=something (not ${VAR})
                        );

        List<String> violations = new ArrayList<>();
        for (Path configFile : configFiles) {
            if (!Files.exists(configFile)) continue;

            String content = Files.readString(configFile);
            for (String pattern : suspiciousPatterns) {
                Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(content);
                while (m.find()) {
                    String match = m.group().trim();
                    // Skip comments and template placeholders
                    if (!match.startsWith("#") && !match.contains("REPLACE_WITH")) {
                        violations.add(configFile.getFileName() + ": " + match);
                    }
                }
            }
        }

        // Then: No hardcoded secrets should be found
        assertTrue(violations.isEmpty(), "Found potential hardcoded secrets: " + violations);
    }

    @Test
    @DisplayName("JWT secret configuration should be secure")
    void shouldHaveSecureJwtConfiguration() throws IOException {
        // Given: Application properties file
        Path appPropsPath = getProjectRootPath().resolve(APPLICATION_PROPERTIES_PATH);
        String propertiesContent = Files.readString(appPropsPath);

        // When: We check JWT configuration
        boolean hasJwtSecretEnvVar = propertiesContent.contains("app.jwt.secret=${JWT_SECRET}");
        boolean hasJwtExpirationConfig = propertiesContent.contains("app.jwt.expiration=");
        boolean hasDefaultFallback = propertiesContent.contains("app.jwt.secret=${JWT_SECRET");

        // Then: JWT should be properly configured with environment variables
        assertTrue(
                hasJwtSecretEnvVar,
                "JWT secret must be configured to use JWT_SECRET environment variable");
        assertTrue(hasJwtExpirationConfig, "JWT expiration must be configured");
        assertTrue(
                hasDefaultFallback || propertiesContent.contains("JWT_SECRET}"),
                "JWT secret should reference environment variable without hardcoded fallback");
    }

    // Helper methods
    private Path getProjectRootPath() {
        Path currentPath = Paths.get("").toAbsolutePath();
        // Navigate up to find project root (contains .env.template)
        while (!Files.exists(currentPath.resolve(ENV_TEMPLATE_PATH))
                && currentPath.getParent() != null) {
            currentPath = currentPath.getParent();
        }
        return currentPath;
    }

    private Set<String> extractEnvironmentVariables(List<String> lines) {
        Set<String> variables = new HashSet<>();
        Pattern pattern = Pattern.compile("^([A-Z_][A-Z0-9_]*)\\s*=");

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("#") || line.isEmpty()) continue;

            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                variables.add(matcher.group(1));
            }
        }
        return variables;
    }

    private Set<String> extractDockerComposeEnvironmentVariables(String content) {
        Set<String> variables = new HashSet<>();
        Pattern pattern = Pattern.compile("- ([A-Z_][A-Z0-9_]*)=\\$\\{([A-Z_][A-Z0-9_]*)\\}");

        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            variables.add(matcher.group(2)); // The environment variable name
        }
        return variables;
    }

    private Set<String> extractReferencedEnvironmentVariables(String content) {
        Set<String> variables = new HashSet<>();
        Pattern pattern = Pattern.compile("\\$\\{([A-Z_][A-Z0-9_]*)\\}");

        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
        return variables;
    }

    private Map<String, String> extractEnvironmentVariableGuidance(List<String> lines) {
        Map<String, String> guidance = new HashMap<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            // Check if this is a variable definition line
            if (line.matches("^[A-Z_][A-Z0-9_]*\\s*=.*")) {
                String varName = line.split("=")[0].trim();

                // Look backwards for comments that contain guidance
                StringBuilder guidance_text = new StringBuilder();
                for (int j = i - 1; j >= 0; j--) {
                    String previousLine = lines.get(j).trim();
                    if (previousLine.startsWith("#")
                            && (previousLine.contains("generate")
                                    || previousLine.contains("character")
                                    || previousLine.contains("SECURITY")
                                    || previousLine.contains("openssl"))) {
                        guidance_text.insert(0, previousLine + " ");
                    } else if (previousLine.startsWith("#")) {
                        // Continue looking at other comments
                        continue;
                    } else if (previousLine.isEmpty()) {
                        // Empty line - continue
                        continue;
                    } else if (previousLine.matches("^[A-Z_][A-Z0-9_]*\\s*=.*")) {
                        // Hit another variable definition - continue searching for section comments
                        continue;
                    } else {
                        // Hit a non-comment, non-empty, non-variable line, stop looking
                        break;
                    }
                }

                if (guidance_text.length() > 0) {
                    guidance.put(varName, guidance_text.toString());
                }
            }
        }

        return guidance;
    }
}

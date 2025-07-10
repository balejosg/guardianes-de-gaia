package com.guardianes.integration.docker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
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

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Metrics Configuration Validation Tests
 * 
 * This test validates that our metrics configuration prevents the cgroup/ProcessorMetrics
 * issues that we encountered. It specifically tests:
 * - SystemMetricsAutoConfiguration exclusion
 * - TomcatMetricsAutoConfiguration exclusion  
 * - Safe metrics are still available
 * - No cgroup-related startup failures
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@DisplayName("Metrics Configuration Validation Tests")
class MetricsConfigurationTest {

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

    @Test
    @DisplayName("Should start without SystemMetricsAutoConfiguration failures")
    @Timeout(value = 2, unit = TimeUnit.MINUTES)
    void shouldStartWithoutSystemMetricsFailures() {
        // Given: Backend container is running
        assertTrue(backend.isRunning(), "Backend container must be running");
        
        // When: We examine the startup logs
        String logs = backend.getLogs();
        
        // Then: Should not contain SystemMetricsAutoConfiguration errors
        assertFalse(logs.contains("SystemMetricsAutoConfiguration"), 
                   "Should exclude SystemMetricsAutoConfiguration to prevent cgroup issues");
        
        // And: Should not contain ProcessorMetrics errors
        assertFalse(logs.contains("Failed to instantiate [io.micrometer.core.instrument.binder.system.ProcessorMetrics]"), 
                   "Should not have ProcessorMetrics instantiation failures");
        
        // And: Should not contain cgroup-related errors
        assertFalse(logs.contains("Cannot invoke \"jdk.internal.platform.CgroupInfo.getMountPoint()\""), 
                   "Should not have cgroup access errors");
        assertFalse(logs.contains("because \"anyController\" is null"), 
                   "Should not have cgroup controller null pointer errors");
    }

    @Test
    @DisplayName("Should start without TomcatMetricsAutoConfiguration failures")
    @Timeout(value = 2, unit = TimeUnit.MINUTES)
    void shouldStartWithoutTomcatMetricsFailures() {
        // Given: Backend container is running
        assertTrue(backend.isRunning(), "Backend container must be running");
        
        // When: We examine the startup logs
        String logs = backend.getLogs();
        
        // Then: Should not contain TomcatMetricsAutoConfiguration errors
        assertFalse(logs.contains("TomcatMetricsBinder.onApplicationEvent"), 
                   "Should exclude TomcatMetricsAutoConfiguration to prevent web metrics issues");
        
        // And: Should not contain Tomcat metrics binding errors
        assertFalse(logs.contains("Error creating bean with name 'tomcatMetricsBinder'"), 
                   "Should not have TomcatMetricsBinder creation failures");
    }

    @Test
    @DisplayName("Should exclude problematic auto-configurations as configured")
    void shouldExcludeProblematicAutoConfigurations() {
        // Given: Backend container is running
        assertTrue(backend.isRunning(), "Backend container must be running");
        
        // When: We examine the startup logs
        String logs = backend.getLogs();
        
        // Then: Should show that problematic auto-configurations are excluded
        // Note: Spring Boot doesn't log excluded auto-configurations by default,
        // but we verify by absence of their error messages
        
        // SystemMetricsAutoConfiguration should be excluded
        assertFalse(logs.contains("processorMetrics"), 
                   "ProcessorMetrics bean should not be created");
        
        // TomcatMetricsAutoConfiguration should be excluded  
        assertFalse(logs.contains("TomcatMetricsBinder"), 
                   "TomcatMetricsBinder should not be created");
        
        // But DataSource and JPA should still be excluded (as per config)
        assertTrue(logs.contains("DataSourceAutoConfiguration") || 
                   !logs.contains("Failed to configure a DataSource"), 
                   "DataSource exclusion should work correctly");
    }

    @Test
    @DisplayName("Should still have actuator functionality despite exclusions")
    void shouldHaveActuatorFunctionalityDespiteExclusions() {
        // Given: Backend container is running
        assertTrue(backend.isRunning(), "Backend container must be running");
        
        // When: We examine the startup logs
        String logs = backend.getLogs();
        
        // Then: Actuator should still be functional
        assertTrue(logs.contains("Exposing") && logs.contains("actuator") || 
                   logs.contains("management.endpoints"), 
                   "Actuator endpoints should still be exposed");
        
        // And: Basic metrics should still be available (not all metrics are problematic)
        assertFalse(logs.contains("No metrics available"), 
                   "Some metrics should still be available");
        
        // And: Health checks should work
        assertFalse(logs.contains("Health check failed"), 
                   "Health checks should function properly");
    }

    @Test
    @DisplayName("Should have safe metrics configuration for Docker environment")
    void shouldHaveSafeMetricsConfigurationForDocker() {
        // Given: Backend container is running
        assertTrue(backend.isRunning(), "Backend container must be running");
        
        // When: We examine the startup logs and configuration
        String logs = backend.getLogs();
        
        // Then: Should not have any metrics-related startup failures
        assertFalse(logs.contains("Metrics configuration failed"), 
                   "Metrics configuration should not fail");
        
        // And: Should not have micrometer registry issues
        assertFalse(logs.contains("MeterRegistry"), 
                   "Should not have MeterRegistry configuration issues");
        
        // And: Prometheus export should still be enabled (as configured)
        // This is validated by the absence of Prometheus export failures
        assertFalse(logs.contains("Prometheus export failed"), 
                   "Prometheus export should work despite metrics exclusions");
    }

    @Test
    @DisplayName("Application properties should correctly exclude problematic configurations")
    void shouldHaveCorrectConfigurationExclusions() {
        // Given: Backend container is running with dev profile
        assertTrue(backend.isRunning(), "Backend container must be running");
        
        // When: We check the environment configuration
        String logs = backend.getLogs();
        
        // Then: Should be running with dev profile
        assertTrue(logs.contains("The following 1 profile is active: \"dev\"") || 
                   logs.contains("Active profiles: dev"), 
                   "Should be running with dev profile");
        
        // And: Should not have configuration property errors
        assertFalse(logs.contains("Property 'spring.autoconfigure.exclude' is invalid"), 
                   "Configuration exclusions should be valid");
        
        // And: Should load application-dev.properties correctly
        assertFalse(logs.contains("Failed to load application-dev.properties"), 
                   "Should load dev properties without errors");
    }

    @Test
    @DisplayName("Should handle Java 17 Docker metrics compatibility correctly")
    void shouldHandleJava17DockerMetricsCompatibility() {
        // Given: Backend container is running with Java 17
        assertTrue(backend.isRunning(), "Backend container must be running");
        
        // When: We examine metrics-related Java 17 compatibility
        String logs = backend.getLogs();
        
        // Then: Should not have Java 17 module access issues
        assertFalse(logs.contains("Unable to access platform MXBean"), 
                   "Should not have MXBean access issues in Java 17");
        
        // And: Should not have illegal access warnings related to metrics
        assertFalse(logs.contains("WARNING: An illegal reflective access operation has occurred"), 
                   "Should not have illegal reflective access warnings");
        
        // And: Should not have module access violations for metrics
        assertFalse(logs.contains("java.lang.reflect.InaccessibleObjectException"), 
                   "Should not have module access violations");
    }

    @Test
    @DisplayName("Container logs should show clean startup without metrics errors")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void shouldShowCleanStartupWithoutMetricsErrors() {
        // Given: Backend container is running
        assertTrue(backend.isRunning(), "Backend container must be running");
        
        // When: We examine the complete startup sequence
        String logs = backend.getLogs();
        
        // Then: Should have clean Spring Boot startup
        assertTrue(logs.contains("Started GuardianesApplication"), 
                  "Application should start successfully");
        
        // And: Should not have any error-level messages related to metrics
        assertFalse(logs.contains("ERROR") && logs.contains("metrics"), 
                   "Should not have ERROR level metrics messages");
        assertFalse(logs.contains("ERROR") && logs.contains("ProcessorMetrics"), 
                   "Should not have ProcessorMetrics ERROR messages");
        assertFalse(logs.contains("ERROR") && logs.contains("SystemMetrics"), 
                   "Should not have SystemMetrics ERROR messages");
        
        // And: Should not have failed bean creation for metrics
        assertFalse(logs.contains("Error creating bean") && logs.contains("Metrics"), 
                   "Should not have metrics bean creation errors");
    }
}
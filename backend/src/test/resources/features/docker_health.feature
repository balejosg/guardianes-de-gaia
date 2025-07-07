Feature: Docker Health Validation
  As a developer
  I want to ensure the application starts correctly in Docker containers
  So that production deployments are reliable and don't fail due to environment issues

  Background:
    Given a complete Docker environment with MySQL, Redis, and Backend services
    And all containers are running and healthy

  Scenario: Backend container starts without cgroup issues
    When the backend container starts up
    Then it should not have any cgroup-related errors in the logs
    And it should not have ProcessorMetrics instantiation failures
    And it should not have SystemMetricsAutoConfiguration errors
    And the application should start successfully

  Scenario: Health endpoints are accessible after container startup
    Given the backend container has started successfully
    When I call the health endpoint with valid credentials
    Then I should receive a 200 OK response
    And the response should indicate the service is UP
    And the response should include disk space health information
    And the response should include Redis connectivity status

  Scenario: All actuator endpoints are functional in Docker environment
    Given the backend container is running and healthy
    When I call each actuator endpoint with authentication
      | endpoint                |
      | /actuator/health        |
      | /actuator/info          |
      | /actuator/metrics       |
      | /actuator/prometheus    |
    Then all endpoints should return 200 OK
    And none should contain error messages about metrics configuration

  Scenario: Metrics configuration prevents Docker compatibility issues
    Given the backend container is running
    When I examine the application startup logs
    Then there should be no mentions of ProcessorMetrics failures
    And there should be no mentions of TomcatMetricsBinder failures  
    And there should be no mentions of cgroup access errors
    And the SystemMetricsAutoConfiguration should be excluded
    And the TomcatMetricsAutoConfiguration should be excluded

  Scenario: Application connects to all external services successfully
    Given all external services are running (MySQL, Redis)
    And the backend container has started
    When I check the health endpoint
    Then the Redis connection should be healthy
    And there should be no connection refused errors in the logs
    And there should be no connection timeout errors in the logs

  Scenario: API endpoints are fully functional in containerized environment
    Given the backend container is running and healthy
    When I call the steps tracking API endpoint
    Then I should receive valid step tracking data
    When I call the energy management API endpoint
    Then I should receive valid energy balance data
    And both responses should contain the expected JSON structure

  Scenario: Authentication is properly enforced in Docker environment
    Given the backend container is running
    When I call a protected endpoint without authentication
    Then I should receive a 401 Unauthorized response
    When I call a protected endpoint with invalid credentials
    Then I should receive a 401 Unauthorized response
    When I call a protected endpoint with valid credentials
    Then I should receive a successful response

  Scenario: Container handles Java 17 compatibility correctly
    Given the backend container is running with Java 17
    When I examine the startup logs
    Then there should be no illegal access errors
    And there should be no module access violations
    And there should be no reflection access issues
    And the JVM should be Java 17 or later

  Scenario: Production monitoring stack integration works
    Given the backend container is running
    And Prometheus metrics export is enabled
    When I call the Prometheus metrics endpoint
    Then I should receive metrics in Prometheus format
    And the metrics should include JVM information
    But the metrics should not include problematic system metrics
    And there should be no metrics collection errors

  Scenario: Container startup performance is acceptable
    Given I start the backend container
    When I measure the startup time
    Then the container should be healthy within 2 minutes
    And the health endpoint should respond within 5 seconds
    And the API endpoints should respond within 10 seconds

  Scenario: Container resilience to service dependencies
    Given all services are running and healthy
    When a non-critical service (Redis) is temporarily restarted
    Then the backend should continue to function
    And the health endpoint should still be accessible
    And the API endpoints should still work
    And there should be no fatal errors in the logs

  Scenario: Memory and resource usage is reasonable
    Given the backend container has been running for several minutes
    When I examine the application logs and metrics
    Then there should be no OutOfMemoryError messages
    And there should be no memory leak warnings
    And the JVM metrics should show reasonable memory usage
    And the container should not be consuming excessive CPU

  Scenario Outline: Different environment profiles work correctly
    Given I start the backend container with profile "<profile>"
    When the container starts up
    Then it should load the correct configuration
    And it should start without profile-specific errors
    And the actuator endpoints should be accessible

    Examples:
      | profile |
      | dev     |
      | local   |

  Scenario: Configuration exclusions work as expected
    Given the backend container is configured to exclude problematic auto-configurations
    When I examine the Spring Boot startup sequence
    Then the DataSourceAutoConfiguration should be excluded
    And the SystemMetricsAutoConfiguration should be excluded  
    And the TomcatMetricsAutoConfiguration should be excluded
    And other necessary configurations should still load correctly
    And the application should function normally despite exclusions
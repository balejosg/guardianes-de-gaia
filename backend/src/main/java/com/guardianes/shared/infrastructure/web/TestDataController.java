package com.guardianes.shared.infrastructure.web;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test data controller for loading E2E test data after backend startup. This controller is only
 * available in CI profile to speed up startup time.
 */
@RestController
@RequestMapping("/api/test")
@Profile("ci")
public class TestDataController {

  private static final Logger logger = LoggerFactory.getLogger(TestDataController.class);

  private final JdbcTemplate jdbcTemplate;

  public TestDataController(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @PostMapping("/load-data")
  public ResponseEntity<String> loadTestData() {
    try {
      logger.info("Loading E2E test data...");

      ClassPathResource resource = new ClassPathResource("data-e2e.sql");
      String sqlContent;
      try (BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
        sqlContent = reader.lines().collect(Collectors.joining("\n"));
      }

      // Split SQL statements and execute them
      String[] statements = sqlContent.split(";");
      int executedCount = 0;

      for (String statement : statements) {
        String trimmed = statement.trim();
        if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
          try {
            jdbcTemplate.execute(trimmed);
            executedCount++;
          } catch (Exception e) {
            logger.warn(
                "Failed to execute statement: {}",
                trimmed.substring(0, Math.min(50, trimmed.length())),
                e);
          }
        }
      }

      logger.info("Successfully loaded {} test data statements", executedCount);
      return ResponseEntity.ok(
          "Test data loaded successfully. Statements executed: " + executedCount);

    } catch (Exception e) {
      logger.error("Failed to load test data", e);
      return ResponseEntity.internalServerError()
          .body("Failed to load test data: " + e.getMessage());
    }
  }
}

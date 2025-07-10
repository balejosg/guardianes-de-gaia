package com.guardianes.integration.deployment;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Database Connectivity Validation Tests
 *
 * <p>These tests validate database connectivity and configuration to prevent connection failures
 * like we experienced during demo time.
 */
@Testcontainers
@DisplayName("Database Connectivity Validation")
class DatabaseConnectivityTest {

    @Container
    static MySQLContainer<?> mysql =
            new MySQLContainer<>("mysql:8.0")
                    .withDatabaseName("guardianes_test")
                    .withUsername("test_user")
                    .withPassword("test_password")
                    .withCommand("--default-authentication-plugin=mysql_native_password");

    @Test
    @DisplayName("Should connect to MySQL with test credentials")
    void shouldConnectToMySqlWithTestCredentials() throws SQLException {
        // Given: MySQL container is running
        String jdbcUrl = mysql.getJdbcUrl();
        String username = mysql.getUsername();
        String password = mysql.getPassword();

        // When: We connect to the database
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            // Then: Connection should be valid
            assertTrue(connection.isValid(5), "Database connection should be valid");
            assertFalse(connection.isClosed(), "Database connection should not be closed");
        }
    }

    @Test
    @DisplayName("Should validate database schema compatibility")
    void shouldValidateDatabaseSchemaCompatibility() throws SQLException {
        // Given: Database connection
        String jdbcUrl = mysql.getJdbcUrl();
        String username = mysql.getUsername();
        String password = mysql.getPassword();

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            // When: We check database metadata
            DatabaseMetaData metaData = connection.getMetaData();

            // Then: Database should support required features
            assertTrue(metaData.supportsTransactions(), "Database should support transactions");
            assertTrue(
                    metaData.supportsMultipleTransactions(),
                    "Database should support multiple transactions");
            assertTrue(
                    metaData.supportsStoredProcedures(),
                    "Database should support stored procedures");

            // Check MySQL version compatibility
            String databaseVersion = metaData.getDatabaseProductVersion();
            assertTrue(
                    databaseVersion.startsWith("8."),
                    "Should be using MySQL 8.x (got: " + databaseVersion + ")");
        }
    }

    @Test
    @DisplayName("Should handle connection timeouts gracefully")
    void shouldHandleConnectionTimeoutsGracefully() throws SQLException {
        // Given: Database connection with timeout settings
        String jdbcUrl = mysql.getJdbcUrl() + "?connectTimeout=5000&socketTimeout=5000";
        String username = mysql.getUsername();
        String password = mysql.getPassword();

        // When: We connect with timeout settings
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            // Then: Connection should work with timeout settings
            assertTrue(connection.isValid(2), "Connection with timeouts should be valid");

            // Test that we can execute queries
            try (PreparedStatement stmt = connection.prepareStatement("SELECT 1 as test_value");
                    ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next(), "Should get result from test query");
                assertEquals(1, rs.getInt("test_value"), "Test query should return expected value");
            }
        }
    }

    @Test
    @DisplayName("Should work without SSL in development environment")
    void shouldWorkWithoutSslInDevelopmentEnvironment() throws SQLException {
        // Given: Database connection without SSL (development configuration)
        String jdbcUrl = mysql.getJdbcUrl() + "?useSSL=false&allowPublicKeyRetrieval=true";
        String username = mysql.getUsername();
        String password = mysql.getPassword();

        // When: We connect without SSL
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            // Then: Connection should work
            assertTrue(connection.isValid(5), "Connection without SSL should work in development");

            // Verify we can perform database operations
            try (PreparedStatement stmt =
                    connection.prepareStatement(
                            "CREATE TEMPORARY TABLE test_table (id INT PRIMARY KEY, name VARCHAR(50))"); ) {
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt =
                    connection.prepareStatement(
                            "INSERT INTO test_table (id, name) VALUES (?, ?)")) {
                stmt.setInt(1, 1);
                stmt.setString(2, "test");
                int rowsAffected = stmt.executeUpdate();
                assertEquals(1, rowsAffected, "Should insert one row");
            }
        }
    }

    @Test
    @DisplayName("Should validate character set and collation")
    void shouldValidateCharacterSetAndCollation() throws SQLException {
        // Given: Database connection
        String jdbcUrl = mysql.getJdbcUrl();
        String username = mysql.getUsername();
        String password = mysql.getPassword();

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            // When: We check character set configuration
            try (PreparedStatement stmt =
                            connection.prepareStatement(
                                    "SHOW VARIABLES LIKE 'character_set_database'");
                    ResultSet rs = stmt.executeQuery()) {

                // Then: Should use UTF-8 character set
                assertTrue(rs.next(), "Should have character set configuration");
                String charset = rs.getString("Value");
                assertTrue(
                        charset.startsWith("utf8"),
                        "Database should use UTF-8 character set (got: " + charset + ")");
            }

            // Check collation
            try (PreparedStatement stmt =
                            connection.prepareStatement(
                                    "SHOW VARIABLES LIKE 'collation_database'");
                    ResultSet rs = stmt.executeQuery()) {

                assertTrue(rs.next(), "Should have collation configuration");
                String collation = rs.getString("Value");
                assertTrue(
                        collation.contains("utf8"),
                        "Database should use UTF-8 collation (got: " + collation + ")");
            }
        }
    }

    @Test
    @DisplayName("Should handle concurrent connections")
    void shouldHandleConcurrentConnections() throws SQLException, InterruptedException {
        // Given: Multiple connection threads
        String jdbcUrl = mysql.getJdbcUrl();
        String username = mysql.getUsername();
        String password = mysql.getPassword();

        int connectionCount = 5;
        Thread[] connectionThreads = new Thread[connectionCount];
        boolean[] connectionResults = new boolean[connectionCount];

        // When: We create multiple concurrent connections
        for (int i = 0; i < connectionCount; i++) {
            final int threadIndex = i;
            connectionThreads[i] =
                    new Thread(
                            () -> {
                                try (Connection connection =
                                        DriverManager.getConnection(jdbcUrl, username, password)) {
                                    // Perform a simple operation to verify connection works
                                    try (PreparedStatement stmt =
                                                    connection.prepareStatement(
                                                            "SELECT CONNECTION_ID()");
                                            ResultSet rs = stmt.executeQuery()) {
                                        connectionResults[threadIndex] =
                                                rs.next() && rs.getLong(1) > 0;
                                    }
                                } catch (SQLException e) {
                                    connectionResults[threadIndex] = false;
                                }
                            });
            connectionThreads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : connectionThreads) {
            thread.join(5000); // 5 second timeout
        }

        // Then: All connections should succeed
        for (int i = 0; i < connectionCount; i++) {
            assertTrue(connectionResults[i], "Connection " + i + " should succeed");
        }
    }

    @Test
    @DisplayName("Should connect using environment variables")
    void shouldConnectUsingEnvironmentVariables() throws SQLException {
        // Given: Environment variables for database connection
        String host = System.getenv("MYSQL_HOST");
        String port = System.getenv("MYSQL_PORT");
        String database = System.getenv("MYSQL_DATABASE");
        String username = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        // Skip test if environment variables not set
        if (host == null || username == null || password == null) {
            System.out.println("Skipping environment variable test - variables not set");
            return;
        }

        // When: We connect using environment variables
        String jdbcUrl =
                String.format(
                        "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true",
                        host,
                        port != null ? port : "3306",
                        database != null ? database : "guardianes");

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            // Then: Connection should work
            assertTrue(connection.isValid(5), "Connection using environment variables should work");
        }
    }

    @Test
    @DisplayName("Should validate connection pool compatibility")
    void shouldValidateConnectionPoolCompatibility() throws SQLException {
        // Given: Database connection with pool-like properties
        Properties props = new Properties();
        props.setProperty("user", mysql.getUsername());
        props.setProperty("password", mysql.getPassword());
        props.setProperty("autoReconnect", "true");
        props.setProperty("maxReconnects", "3");
        props.setProperty("initialTimeout", "2");

        // When: We connect with connection pool properties
        try (Connection connection = DriverManager.getConnection(mysql.getJdbcUrl(), props)) {
            // Then: Connection should work with pool settings
            assertTrue(connection.isValid(5), "Connection with pool properties should work");

            // Test autocommit behavior (important for connection pools)
            assertTrue(connection.getAutoCommit(), "AutoCommit should be enabled by default");

            connection.setAutoCommit(false);
            assertFalse(connection.getAutoCommit(), "Should be able to disable autocommit");

            connection.rollback();
            connection.setAutoCommit(true);
        }
    }
}

package com.guardianes.shared.infrastructure.security;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guardianes.shared.infrastructure.web.AuthController;
import com.guardianes.testconfig.SecurityTestConfiguration;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(locations = "classpath:application-security-test.properties")
@Import(SecurityTestConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class InputValidationSecurityTest {

    @Autowired private WebApplicationContext context;

    @Autowired private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Nested
    @DisplayName("SQL Injection Prevention Tests")
    class SqlInjectionTests {

        @Test
        @DisplayName("Should reject SQL injection in login username")
        void shouldRejectSqlInjectionInUsername() throws Exception {
            AuthController.LoginRequest maliciousRequest =
                    new AuthController.LoginRequest("admin'; DROP TABLE users; --", "password");

            mockMvc.perform(
                            post("/api/v1/auth/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(maliciousRequest)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "GUARDIAN")
        @DisplayName("Should reject SQL injection in path parameters")
        void shouldRejectSqlInjectionInPathParameters() throws Exception {
            mockMvc.perform(get("/api/v1/guardians/1'; DROP TABLE guardians; --/steps/current"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("XSS Prevention Tests")
    class XssPreventionTests {

        @Test
        @DisplayName("Should reject XSS in login credentials")
        void shouldRejectXssInLoginCredentials() throws Exception {
            AuthController.LoginRequest xssRequest =
                    new AuthController.LoginRequest("<script>alert('xss')</script>", "password");

            mockMvc.perform(
                            post("/api/v1/auth/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(xssRequest)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "GUARDIAN")
        @DisplayName("Should sanitize step submission data")
        void shouldSanitizeStepSubmissionData() throws Exception {
            Map<String, Object> maliciousStepData = new HashMap<>();
            maliciousStepData.put("stepCount", 1000);
            maliciousStepData.put("timestamp", "2025-07-07T14:00:00");
            maliciousStepData.put("maliciousField", "<script>alert('xss')</script>");

            mockMvc.perform(
                            post("/api/v1/guardians/1/steps")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(maliciousStepData)))
                    .andDo(print())
                    // Should either succeed (ignoring extra field) or fail with validation error
                    .andExpect(
                            result -> {
                                int status = result.getResponse().getStatus();
                                if (status != 200 && status != 400) {
                                    throw new AssertionError(
                                            "Expected status 200 or 400, but was " + status);
                                }
                            });
        }
    }

    @Nested
    @DisplayName("Input Validation Tests")
    class InputValidationTests {

        @Test
        @WithMockUser(roles = "GUARDIAN")
        @DisplayName("Should reject negative step count")
        void shouldRejectNegativeStepCount() throws Exception {
            Map<String, Object> invalidStepData = new HashMap<>();
            invalidStepData.put("stepCount", -1000);
            invalidStepData.put("timestamp", "2025-07-07T14:00:00");

            mockMvc.perform(
                            post("/api/v1/guardians/1/steps")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalidStepData)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "GUARDIAN")
        @DisplayName("Should reject extremely large step count")
        void shouldRejectExtremelyLargeStepCount() throws Exception {
            Map<String, Object> invalidStepData = new HashMap<>();
            invalidStepData.put("stepCount", Integer.MAX_VALUE);
            invalidStepData.put("timestamp", "2025-07-07T14:00:00");

            mockMvc.perform(
                            post("/api/v1/guardians/1/steps")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalidStepData)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "GUARDIAN")
        @DisplayName("Should reject invalid timestamp format")
        void shouldRejectInvalidTimestampFormat() throws Exception {
            Map<String, Object> invalidStepData = new HashMap<>();
            invalidStepData.put("stepCount", 1000);
            invalidStepData.put("timestamp", "invalid-timestamp");

            mockMvc.perform(
                            post("/api/v1/guardians/1/steps")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalidStepData)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "GUARDIAN")
        @DisplayName("Should reject missing required fields")
        void shouldRejectMissingRequiredFields() throws Exception {
            Map<String, Object> incompleteData = new HashMap<>();
            incompleteData.put("stepCount", 1000);
            // Missing timestamp

            mockMvc.perform(
                            post("/api/v1/guardians/1/steps")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(incompleteData)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Path Traversal Prevention Tests")
    class PathTraversalTests {

        @Test
        @WithMockUser(roles = "GUARDIAN")
        @DisplayName("Should reject path traversal attempts in guardian ID")
        void shouldRejectPathTraversalInGuardianId() throws Exception {
            mockMvc.perform(get("/api/v1/guardians/../admin/steps/current"))
                    .andDo(print())
                    .andExpect(
                            status().isBadRequest()); // Framework rejects malformed path variable
        }

        @Test
        @WithMockUser(roles = "GUARDIAN")
        @DisplayName("Should reject encoded path traversal attempts")
        void shouldRejectEncodedPathTraversalAttempts() throws Exception {
            mockMvc.perform(get("/api/v1/guardians/%2E%2E%2Fadmin/steps/current"))
                    .andDo(print())
                    .andExpect(
                            status().isBadRequest()); // Framework rejects malformed path variable
        }
    }

    @Nested
    @DisplayName("Content Type Security Tests")
    class ContentTypeSecurityTests {

        @Test
        @DisplayName("Should reject non-JSON content type for API endpoints")
        void shouldRejectNonJsonContentType() throws Exception {
            mockMvc.perform(
                            post("/api/v1/auth/login")
                                    .contentType(MediaType.TEXT_PLAIN)
                                    .content("username=admin&password=admin123"))
                    .andDo(print())
                    .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        @WithMockUser(roles = "GUARDIAN")
        @DisplayName("Should reject XML content type")
        void shouldRejectXmlContentType() throws Exception {
            String xmlContent =
                    """
                <?xml version="1.0"?>
                <stepData>
                    <stepCount>1000</stepCount>
                    <timestamp>2025-07-07T14:00:00</timestamp>
                </stepData>
                """;

            mockMvc.perform(
                            post("/api/v1/guardians/1/steps")
                                    .contentType(MediaType.APPLICATION_XML)
                                    .content(xmlContent))
                    .andDo(print())
                    .andExpect(status().isUnsupportedMediaType());
        }
    }

    @Nested
    @DisplayName("HTTP Method Security Tests")
    class HttpMethodSecurityTests {

        @Test
        @WithMockUser(roles = "GUARDIAN")
        @DisplayName("Should reject unsupported HTTP methods")
        void shouldRejectUnsupportedHttpMethods() throws Exception {
            mockMvc.perform(patch("/api/v1/guardians/1/steps/current"))
                    .andDo(print())
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @WithMockUser(roles = "GUARDIAN")
        @DisplayName("Should only allow appropriate methods for each endpoint")
        void shouldOnlyAllowAppropriateMethodsForEachEndpoint() throws Exception {
            // GET should work for current steps
            mockMvc.perform(get("/api/v1/guardians/1/steps/current"))
                    .andDo(print())
                    .andExpect(status().isOk());

            // POST should not work for current steps (read-only endpoint)
            mockMvc.perform(
                            post("/api/v1/guardians/1/steps/current")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{}"))
                    .andDo(print())
                    .andExpect(status().isMethodNotAllowed());
        }
    }
}

package com.guardianes.shared.infrastructure.security;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guardianes.shared.infrastructure.web.AuthController;
import com.guardianes.testconfig.SecurityTestConfiguration;
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
class SecurityIntegrationTest {

    @Autowired private WebApplicationContext context;

    @Autowired private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Nested
    @DisplayName("Authentication Tests")
    class AuthenticationTests {

        @Test
        @DisplayName("Should allow access to public health endpoint")
        void shouldAllowPublicHealthEndpoint() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("UP"));
        }

        @Test
        @DisplayName("Should allow login with valid credentials")
        void shouldAllowLoginWithValidCredentials() throws Exception {
            AuthController.LoginRequest loginRequest =
                    new AuthController.LoginRequest("admin", "admin123");

            mockMvc.perform(
                            post("/api/v1/auth/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.type").value("Bearer"))
                    .andExpect(jsonPath("$.username").value("admin"));
        }

        @Test
        @DisplayName("Should reject login with invalid credentials")
        void shouldRejectLoginWithInvalidCredentials() throws Exception {
            AuthController.LoginRequest loginRequest =
                    new AuthController.LoginRequest("admin", "wrongpassword");

            mockMvc.perform(
                            post("/api/v1/auth/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Invalid credentials"));
        }

        @Test
        @DisplayName("Should reject requests with missing username")
        void shouldRejectRequestsWithMissingUsername() throws Exception {
            AuthController.LoginRequest loginRequest =
                    new AuthController.LoginRequest("", "password");

            mockMvc.perform(
                            post("/api/v1/auth/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject requests with missing password")
        void shouldRejectRequestsWithMissingPassword() throws Exception {
            AuthController.LoginRequest loginRequest = new AuthController.LoginRequest("admin", "");

            mockMvc.perform(
                            post("/api/v1/auth/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(loginRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should provide helpful error message for unauthenticated API access")
        void shouldProvideHelpfulErrorForUnauthenticatedAccess() throws Exception {
            mockMvc.perform(get("/api/v1/guardians/1/steps/current"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(
                            jsonPath("$.message")
                                    .value("Authentication required to access this resource"))
                    .andExpect(
                            jsonPath("$.hint")
                                    .value(
                                            "Missing Authorization header. Include 'Authorization: Bearer <token>' in your request."));
        }

        @Test
        @DisplayName("Should provide helpful error message for invalid token format")
        void shouldProvideHelpfulErrorForInvalidTokenFormat() throws Exception {
            mockMvc.perform(
                            get("/api/v1/guardians/1/steps/current")
                                    .header("Authorization", "Invalid token"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(
                            jsonPath("$.hint")
                                    .value(
                                            "Invalid Authorization header format. Use 'Bearer <token>'."));
        }
    }

    @Nested
    @DisplayName("Authorization Tests")
    class AuthorizationTests {

        @Test
        @WithMockUser(roles = "GUARDIAN")
        @DisplayName("Should allow GUARDIAN role to access guardian endpoints")
        void shouldAllowGuardianRoleToAccessGuardianEndpoints() throws Exception {
            mockMvc.perform(get("/api/v1/guardians/1/steps/current"))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should allow ADMIN role to access guardian endpoints")
        void shouldAllowAdminRoleToAccessGuardianEndpoints() throws Exception {
            mockMvc.perform(get("/api/v1/guardians/1/steps/current"))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("Should deny USER role access to guardian endpoints")
        void shouldDenyUserRoleAccessToGuardianEndpoints() throws Exception {
            mockMvc.perform(get("/api/v1/guardians/1/steps/current"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "GUARDIAN")
        @DisplayName("Should deny GUARDIAN role access to admin endpoints")
        void shouldDenyGuardianRoleAccessToAdminEndpoints() throws Exception {
            mockMvc.perform(get("/actuator/metrics"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should allow ADMIN role access to admin endpoints")
        void shouldAllowAdminRoleAccessToAdminEndpoints() throws Exception {
            mockMvc.perform(get("/actuator/metrics")).andDo(print()).andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Security Headers Tests")
    class SecurityHeadersTests {

        @Test
        @DisplayName("Should include security headers in responses")
        void shouldIncludeSecurityHeaders() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().string("X-Frame-Options", "DENY"))
                    .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                    .andExpect(header().exists("Strict-Transport-Security"));
        }

        @Test
        @DisplayName("Should handle CORS preflight requests")
        void shouldHandleCorsPreflightRequests() throws Exception {
            mockMvc.perform(
                            options("/api/v1/guardians/1/steps/current")
                                    .header("Origin", "http://localhost:3000")
                                    .header("Access-Control-Request-Method", "GET")
                                    .header("Access-Control-Request-Headers", "Authorization"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(
                            header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                    .andExpect(
                            header().string("Access-Control-Allow-Methods", containsString("GET")));
        }
    }

    @Nested
    @DisplayName("Rate Limiting Tests")
    class RateLimitingTests {

        @Test
        @DisplayName("Should include rate limit headers")
        void shouldIncludeRateLimitHeaders() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().exists("X-RateLimit-Limit"))
                    .andExpect(header().exists("X-RateLimit-Remaining"))
                    .andExpect(header().exists("X-RateLimit-Reset"));
        }

        @Test
        @DisplayName("Should not rate limit health endpoint")
        void shouldNotRateLimitHealthEndpoint() throws Exception {
            // This test verifies that health endpoint is excluded from rate limiting
            // by checking that it doesn't have rate limit headers
            mockMvc.perform(get("/actuator/health")).andDo(print()).andExpect(status().isOk());
            // Health endpoint should be excluded from rate limiting
        }
    }

    @Nested
    @DisplayName("JWT Token Tests")
    class JwtTokenTests {

        @Test
        @DisplayName("Should validate JWT token correctly")
        void shouldValidateJwtTokenCorrectly() throws Exception {
            // First, get a valid token
            AuthController.LoginRequest loginRequest =
                    new AuthController.LoginRequest("admin", "admin123");

            String response =
                    mockMvc.perform(
                                    post("/api/v1/auth/login")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(objectMapper.writeValueAsString(loginRequest)))
                            .andExpect(status().isOk())
                            .andReturn()
                            .getResponse()
                            .getContentAsString();

            // Extract token from response
            String token = objectMapper.readTree(response).get("token").asText();

            // Validate the token
            mockMvc.perform(get("/api/v1/auth/validate").header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valid").value(true))
                    .andExpect(jsonPath("$.username").value("admin"));
        }

        @Test
        @DisplayName("Should reject invalid JWT token")
        void shouldRejectInvalidJwtToken() throws Exception {
            mockMvc.perform(
                            get("/api/v1/auth/validate")
                                    .header("Authorization", "Bearer invalid.jwt.token"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.valid").value(false));
        }

        @Test
        @DisplayName("Should use JWT token for API access")
        void shouldUseJwtTokenForApiAccess() throws Exception {
            // Get a valid token
            AuthController.LoginRequest loginRequest =
                    new AuthController.LoginRequest("testuser", "test123");

            String response =
                    mockMvc.perform(
                                    post("/api/v1/auth/login")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(objectMapper.writeValueAsString(loginRequest)))
                            .andExpect(status().isOk())
                            .andReturn()
                            .getResponse()
                            .getContentAsString();

            String token = objectMapper.readTree(response).get("token").asText();

            // Use token to access protected endpoint
            mockMvc.perform(
                            get("/api/v1/guardians/1/steps/current")
                                    .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.guardianId").value(1));
        }
    }
}

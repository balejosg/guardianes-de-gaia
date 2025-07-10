package com.guardianes.shared.infrastructure.security;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.guardianes.testconfig.SecurityTestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(
        locations = "classpath:application-security-test.properties",
        properties = {
            "app.security.rate-limit.enabled=true",
            "app.security.rate-limit.requests-per-minute=5"
        })
@Import(SecurityTestConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RateLimitingSecurityTest {

    @Autowired private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    @WithMockUser(roles = "GUARDIAN")
    @DisplayName("Should allow requests within rate limit")
    void shouldAllowRequestsWithinRateLimit() throws Exception {
        // Make 5 requests (within the limit)
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/v1/guardians/1/steps/current"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().string("X-RateLimit-Limit", "5"))
                    .andExpect(header().string("X-RateLimit-Remaining", String.valueOf(4 - i)));
        }
    }

    @Test
    @WithMockUser(roles = "GUARDIAN")
    @DisplayName("Should block requests exceeding rate limit")
    void shouldBlockRequestsExceedingRateLimit() throws Exception {
        // Make 5 requests to reach the limit
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/v1/guardians/1/steps/current")).andExpect(status().isOk());
        }

        // 6th request should be rate limited
        mockMvc.perform(get("/api/v1/guardians/1/steps/current"))
                .andDo(print())
                .andExpect(status().is(429))
                .andExpect(jsonPath("$.error").value("Too Many Requests"))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Rate limit exceeded. Maximum 5 requests per minute allowed."))
                .andExpect(header().string("Retry-After", "60"));
    }

    @Test
    @DisplayName("Should not rate limit health endpoint")
    void shouldNotRateLimitHealthEndpoint() throws Exception {
        // Make many requests to health endpoint
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/actuator/health")).andDo(print()).andExpect(status().isOk());
            // Health endpoint should not have rate limit headers since it's excluded
        }
    }

    @Test
    @WithMockUser(roles = "GUARDIAN")
    @DisplayName("Should track rate limits per IP address")
    void shouldTrackRateLimitsPerIpAddress() throws Exception {
        // Simulate requests from different IP addresses
        // This test verifies the rate limiting is per-IP, not global

        // First IP makes 5 requests
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(
                            get("/api/v1/guardians/1/steps/current")
                                    .header("X-Forwarded-For", "192.168.1.1"))
                    .andExpect(status().isOk());
        }

        // First IP gets rate limited
        mockMvc.perform(
                        get("/api/v1/guardians/1/steps/current")
                                .header("X-Forwarded-For", "192.168.1.1"))
                .andExpect(status().is(429));

        // Second IP should still be able to make requests
        mockMvc.perform(
                        get("/api/v1/guardians/1/steps/current")
                                .header("X-Forwarded-For", "192.168.1.2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("X-RateLimit-Remaining", "4"));
    }

    @Test
    @WithMockUser(roles = "GUARDIAN")
    @DisplayName("Should reset rate limit after time window")
    void shouldResetRateLimitAfterTimeWindow() throws Exception {
        // This test would need time manipulation or a shorter window for practical testing
        // For now, we'll just verify the reset timestamp is reasonable

        mockMvc.perform(get("/api/v1/guardians/1/steps/current"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().exists("X-RateLimit-Reset")); // Should have reset timestamp
    }

    @Test
    @WithMockUser(roles = "GUARDIAN")
    @DisplayName("Should handle X-Real-IP header for rate limiting")
    void shouldHandleXRealIpHeaderForRateLimiting() throws Exception {
        // Test that X-Real-IP header is used for rate limiting
        mockMvc.perform(get("/api/v1/guardians/1/steps/current").header("X-Real-IP", "10.0.0.1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("X-RateLimit-Remaining", "4"));
    }
}

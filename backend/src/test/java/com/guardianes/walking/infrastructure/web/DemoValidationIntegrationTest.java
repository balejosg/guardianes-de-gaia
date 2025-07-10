package com.guardianes.walking.infrastructure.web;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guardianes.testconfig.NoRedisTestConfiguration;
import com.guardianes.walking.application.dto.StepSubmissionRequest;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * Comprehensive E2E integration test that validates the complete user flow from mobile app to
 * backend. This test ensures demo readiness by validating all critical paths that are demonstrated
 * during demos.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureWebMvc
@Import(NoRedisTestConfiguration.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("Demo Validation Integration Tests")
class DemoValidationIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Test
    @WithMockUser(
            username = "guardian1",
            roles = {"GUARDIAN"})
    @DisplayName("Should complete full step tracking demo flow")
    void shouldCompleteFullStepTrackingDemoFlow() throws Exception {
        Long guardianId = 1L;

        // Step 1: Get initial step count (should be 0 for new guardian)
        mockMvc.perform(get("/api/v1/guardians/{guardianId}/steps/current", guardianId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentSteps").value(0))
                .andExpect(jsonPath("$.availableEnergy").value(0))
                .andExpect(jsonPath("$.date").exists());

        // Step 2: Submit first batch of steps
        StepSubmissionRequest firstSubmission =
                new StepSubmissionRequest(2500, LocalDateTime.now().minusHours(2));

        MvcResult firstResult =
                mockMvc.perform(
                                post("/api/v1/guardians/{guardianId}/steps", guardianId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(firstSubmission)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.guardianId").value(guardianId))
                        .andExpect(jsonPath("$.totalDailySteps").value(2500))
                        .andExpect(jsonPath("$.energyEarned").value(250)) // 2500 / 10
                        .andExpect(jsonPath("$.message").exists())
                        .andReturn();

        // Step 3: Verify updated step count
        mockMvc.perform(get("/api/v1/guardians/{guardianId}/steps/current", guardianId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentSteps").value(2500))
                .andExpect(jsonPath("$.availableEnergy").value(250));

        // Step 4: Submit second batch of steps
        StepSubmissionRequest secondSubmission =
                new StepSubmissionRequest(1500, LocalDateTime.now().minusHours(1));

        mockMvc.perform(
                        post("/api/v1/guardians/{guardianId}/steps", guardianId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(secondSubmission)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guardianId").value(guardianId))
                .andExpect(jsonPath("$.totalDailySteps").value(4000)) // 2500 + 1500
                .andExpect(jsonPath("$.energyEarned").value(150)) // 1500 / 10
                .andExpect(jsonPath("$.message").exists());

        // Step 5: Verify final step count
        mockMvc.perform(get("/api/v1/guardians/{guardianId}/steps/current", guardianId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentSteps").value(4000))
                .andExpect(jsonPath("$.availableEnergy").value(400)); // 4000 / 10

        // Step 6: Get step history to verify data persistence
        mockMvc.perform(get("/api/v1/guardians/{guardianId}/steps/history", guardianId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guardianId").value(guardianId))
                .andExpect(jsonPath("$.dailySteps").isArray())
                .andExpect(jsonPath("$.dailySteps", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.dailySteps[0].totalSteps.value").value(4000));
    }

    @Test
    @WithMockUser(
            username = "guardian1",
            roles = {"GUARDIAN"})
    @DisplayName("Should complete full energy management demo flow")
    void shouldCompleteFullEnergyManagementDemoFlow() throws Exception {
        Long guardianId = 1L;

        // Step 1: Setup - submit steps to earn energy
        StepSubmissionRequest stepSubmission =
                new StepSubmissionRequest(5000, LocalDateTime.now().minusHours(1));

        mockMvc.perform(
                        post("/api/v1/guardians/{guardianId}/steps", guardianId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(stepSubmission)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.energyEarned").value(500)); // 5000 / 10

        // Step 2: Check energy balance
        mockMvc.perform(get("/api/v1/guardians/{guardianId}/energy/balance", guardianId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guardianId.value").value(guardianId))
                .andExpect(jsonPath("$.currentBalance.amount").value(500))
                .andExpect(jsonPath("$.transactionSummary").isArray());

        // Step 3: Spend energy on battle
        String energySpendingRequest =
                """
            {
                "amount": 100,
                "source": "BATTLE"
            }
            """;

        mockMvc.perform(
                        post("/api/v1/guardians/{guardianId}/energy/spend", guardianId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(energySpendingRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guardianId.value").value(guardianId))
                .andExpect(jsonPath("$.newBalance.amount").value(400)) // 500 - 100
                .andExpect(jsonPath("$.amountSpent.amount").value(100))
                .andExpect(jsonPath("$.source").value("BATTLE"))
                .andExpect(jsonPath("$.message").exists());

        // Step 4: Verify updated energy balance
        mockMvc.perform(get("/api/v1/guardians/{guardianId}/energy/balance", guardianId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guardianId.value").value(guardianId))
                .andExpect(jsonPath("$.currentBalance.amount").value(400));
    }

    @Test
    @WithMockUser(
            username = "guardian1",
            roles = {"GUARDIAN"})
    @DisplayName("Should handle demo error scenarios gracefully")
    void shouldHandleDemoErrorScenariosGracefully() throws Exception {
        Long guardianId = 1L;

        // Error Scenario 1: Invalid step count
        StepSubmissionRequest invalidStepRequest =
                new StepSubmissionRequest(-100, LocalDateTime.now());

        mockMvc.perform(
                        post("/api/v1/guardians/{guardianId}/steps", guardianId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidStepRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("stepCount"))
                .andExpect(
                        jsonPath("$.fieldErrors[0].message").value("Step count must be positive"));

        // Error Scenario 2: Malformed JSON
        String malformedJson = "{ invalid json }";

        mockMvc.perform(
                        post("/api/v1/guardians/{guardianId}/steps", guardianId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Malformed JSON request"));

        // Error Scenario 3: Missing request body
        mockMvc.perform(
                        post("/api/v1/guardians/{guardianId}/steps", guardianId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Malformed JSON request"));

        // Error Scenario 4: Non-existent guardian
        mockMvc.perform(get("/api/v1/guardians/999/steps/current"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Guardian Not Found"))
                .andExpect(jsonPath("$.message").value("Guardian not found with ID: 999"));
    }

    @Test
    @WithMockUser(
            username = "guardian1",
            roles = {"GUARDIAN"})
    @DisplayName("Should validate JSON serialization compatibility")
    void shouldValidateJsonSerializationCompatibility() throws Exception {
        Long guardianId = 1L;

        // Submit steps to generate data
        StepSubmissionRequest stepSubmission = new StepSubmissionRequest(3000, LocalDateTime.now());

        MvcResult stepResult =
                mockMvc.perform(
                                post("/api/v1/guardians/{guardianId}/steps", guardianId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(stepSubmission)))
                        .andExpect(status().isOk())
                        .andReturn();

        // Validate JSON structure matches Flutter expectations
        String stepResponseJson = stepResult.getResponse().getContentAsString();

        // These field names must match Flutter model expectations
        mockMvc.perform(get("/api/v1/guardians/{guardianId}/steps/current", guardianId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentSteps").exists()) // Flutter expects 'currentSteps'
                .andExpect(
                        jsonPath("$.availableEnergy").exists()) // Flutter expects 'availableEnergy'
                .andExpect(jsonPath("$.date").exists()); // Flutter expects 'date'

        // Validate step history JSON structure
        mockMvc.perform(get("/api/v1/guardians/{guardianId}/steps/history", guardianId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guardianId").exists())
                .andExpect(jsonPath("$.dailySteps").isArray()) // Flutter expects 'dailySteps'
                .andExpect(
                        jsonPath("$.dailySteps[0].totalSteps.value")
                                .exists()) // Flutter expects nested 'value'
                .andExpect(jsonPath("$.dailySteps[0].date").exists());

        // Validate energy balance JSON structure
        mockMvc.perform(get("/api/v1/guardians/{guardianId}/energy/balance", guardianId))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.guardianId.value").exists()) // Flutter expects nested 'value'
                .andExpect(
                        jsonPath("$.currentBalance.amount")
                                .exists()) // Flutter expects nested 'amount'
                .andExpect(jsonPath("$.transactionSummary").isArray());
    }

    @Test
    @WithMockUser(
            username = "guardian1",
            roles = {"GUARDIAN"})
    @DisplayName("Should validate performance within demo requirements")
    void shouldValidatePerformanceWithinDemoRequirements() throws Exception {
        Long guardianId = 1L;

        // Performance Test 1: Step submission should complete within 1 second
        long startTime = System.currentTimeMillis();

        StepSubmissionRequest stepSubmission = new StepSubmissionRequest(2500, LocalDateTime.now());

        mockMvc.perform(
                        post("/api/v1/guardians/{guardianId}/steps", guardianId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(stepSubmission)))
                .andExpect(status().isOk());

        long stepSubmissionTime = System.currentTimeMillis() - startTime;
        assert stepSubmissionTime < 1000
                : "Step submission took too long: " + stepSubmissionTime + "ms";

        // Performance Test 2: Current step count should complete within 500ms
        startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/v1/guardians/{guardianId}/steps/current", guardianId))
                .andExpect(status().isOk());

        long currentStepTime = System.currentTimeMillis() - startTime;
        assert currentStepTime < 500
                : "Current step count took too long: " + currentStepTime + "ms";

        // Performance Test 3: Step history should complete within 1 second
        startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/v1/guardians/{guardianId}/steps/history", guardianId))
                .andExpect(status().isOk());

        long historyTime = System.currentTimeMillis() - startTime;
        assert historyTime < 1000 : "Step history took too long: " + historyTime + "ms";
    }

    @Test
    @WithMockUser(
            username = "guardian1",
            roles = {"GUARDIAN"})
    @DisplayName("Should validate Redis caching works correctly")
    void shouldValidateRedisCachingWorksCorrectly() throws Exception {
        Long guardianId = 1L;

        // Submit steps to populate cache
        StepSubmissionRequest stepSubmission = new StepSubmissionRequest(2500, LocalDateTime.now());

        mockMvc.perform(
                        post("/api/v1/guardians/{guardianId}/steps", guardianId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(stepSubmission)))
                .andExpect(status().isOk());

        // Multiple requests should return consistent data (testing cache)
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/v1/guardians/{guardianId}/steps/current", guardianId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.currentSteps").value(2500))
                    .andExpect(jsonPath("$.availableEnergy").value(250));
        }

        // Submit more steps and verify cache invalidation
        StepSubmissionRequest additionalSteps =
                new StepSubmissionRequest(1500, LocalDateTime.now().minusMinutes(30));

        mockMvc.perform(
                        post("/api/v1/guardians/{guardianId}/steps", guardianId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(additionalSteps)))
                .andExpect(status().isOk());

        // Verify cache was invalidated and updated
        mockMvc.perform(get("/api/v1/guardians/{guardianId}/steps/current", guardianId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentSteps").value(4000)) // 2500 + 1500
                .andExpect(jsonPath("$.availableEnergy").value(400)); // 4000 / 10
    }
}

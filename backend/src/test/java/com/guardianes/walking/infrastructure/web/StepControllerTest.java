package com.guardianes.walking.infrastructure.web;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.testconfig.TestSecurityConfig;
import com.guardianes.walking.application.dto.CurrentStepCountResponse;
import com.guardianes.walking.application.dto.StepHistoryResponse;
import com.guardianes.walking.application.dto.StepSubmissionRequest;
import com.guardianes.walking.application.dto.StepSubmissionResponse;
import com.guardianes.walking.application.service.StepTrackingApplicationService;
import com.guardianes.walking.domain.exception.GuardianNotFoundException;
import com.guardianes.walking.domain.exception.RateLimitExceededException;
import com.guardianes.walking.domain.model.DailyStepAggregate;
import com.guardianes.walking.domain.model.StepCount;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StepController.class)
@Import(TestSecurityConfig.class)
@WithMockUser(
        username = "admin",
        roles = {"ADMIN"})
@DisplayName("Step Controller Tests")
class StepControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockBean private StepTrackingApplicationService stepTrackingService;

    // Mock security dependencies to avoid ApplicationContext issues
    @MockBean
    private com.guardianes.shared.infrastructure.security.JwtTokenProvider jwtTokenProvider;

    @MockBean private com.guardianes.shared.infrastructure.validation.InputSanitizer inputSanitizer;

    private StepSubmissionRequest validStepRequest;
    private StepSubmissionResponse stepResponse;

    @BeforeEach
    void setUp() {
        validStepRequest = new StepSubmissionRequest(2500, LocalDateTime.of(2025, 7, 4, 14, 30));
        stepResponse = new StepSubmissionResponse(1L, 3500, 350, "Steps submitted successfully");

        // Mock input sanitizer to return requests as-is
        when(inputSanitizer.sanitizeStepSubmission(any(StepSubmissionRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(inputSanitizer.sanitizeGuardianId(any(Long.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Should submit steps successfully")
    void shouldSubmitStepsSuccessfully() throws Exception {
        // Given
        Long guardianId = 1L;
        when(stepTrackingService.submitSteps(eq(guardianId), any(StepSubmissionRequest.class)))
                .thenReturn(stepResponse);

        // When & Then
        mockMvc.perform(
                        post("/api/v1/guardians/{guardianId}/steps", guardianId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validStepRequest))
                                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guardianId").value(1))
                .andExpect(jsonPath("$.totalDailySteps").value(3500))
                .andExpect(jsonPath("$.energyEarned").value(350))
                .andExpect(jsonPath("$.message").value("Steps submitted successfully"));

        verify(stepTrackingService, times(1)).submitSteps(guardianId, validStepRequest);
    }

    @Test
    @DisplayName("Should return 400 for invalid step count")
    void shouldReturn400ForInvalidStepCount() throws Exception {
        // Given
        Long guardianId = 1L;
        StepSubmissionRequest invalidRequest = new StepSubmissionRequest(-100, LocalDateTime.now());

        // When & Then
        mockMvc.perform(
                        post("/api/v1/guardians/{guardianId}/steps", guardianId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest))
                                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[*].field").value(hasItem("stepCount")))
                .andExpect(
                        jsonPath("$.fieldErrors[*].message")
                                .value(hasItem("Step count must be positive")));

        // Service should not be called due to validation failure
        verify(stepTrackingService, never()).submitSteps(any(), any());
    }

    @Test
    @DisplayName("Should return 429 for rate limiting")
    void shouldReturn429ForRateLimiting() throws Exception {
        // Given
        Long guardianId = 1L;

        when(stepTrackingService.submitSteps(eq(guardianId), any(StepSubmissionRequest.class)))
                .thenThrow(new RateLimitExceededException("Too many step submissions"));

        // When & Then
        mockMvc.perform(
                        post("/api/v1/guardians/{guardianId}/steps", guardianId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validStepRequest))
                                .with(csrf()))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("Rate Limit Exceeded"))
                .andExpect(jsonPath("$.message").value("Too many step submissions"))
                .andExpect(header().exists("X-RateLimit-Remaining"));

        verify(stepTrackingService, times(1)).submitSteps(guardianId, validStepRequest);
    }

    @Test
    @DisplayName("Should get current step count")
    void shouldGetCurrentStepCount() throws Exception {
        // Given
        Long guardianId = 1L;
        CurrentStepCountResponse response =
                new CurrentStepCountResponse(guardianId, 4200, 420, LocalDate.now());

        when(stepTrackingService.getCurrentStepCount(guardianId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/guardians/{guardianId}/steps/current", guardianId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guardianId").value(1))
                .andExpect(jsonPath("$.currentSteps").value(4200))
                .andExpect(jsonPath("$.availableEnergy").value(420))
                .andExpect(jsonPath("$.date").exists());

        verify(stepTrackingService, times(1)).getCurrentStepCount(guardianId);
    }

    @Test
    @DisplayName("Should get step history")
    void shouldGetStepHistory() throws Exception {
        // Given
        Long guardianId = 1L;
        LocalDate fromDate = LocalDate.of(2025, 7, 1);
        LocalDate toDate = LocalDate.of(2025, 7, 3);

        StepHistoryResponse response =
                new StepHistoryResponse(
                        guardianId,
                        Arrays.asList(
                                DailyStepAggregate.create(
                                        GuardianId.of(guardianId),
                                        LocalDate.of(2025, 7, 1),
                                        StepCount.of(2000)),
                                DailyStepAggregate.create(
                                        GuardianId.of(guardianId),
                                        LocalDate.of(2025, 7, 2),
                                        StepCount.of(3000)),
                                DailyStepAggregate.create(
                                        GuardianId.of(guardianId),
                                        LocalDate.of(2025, 7, 3),
                                        StepCount.of(4000))));

        when(stepTrackingService.getStepHistory(guardianId, fromDate, toDate)).thenReturn(response);

        // When & Then
        mockMvc.perform(
                        get("/api/v1/guardians/{guardianId}/steps/history", guardianId)
                                .param("from", "2025-07-01")
                                .param("to", "2025-07-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guardianId").value(1))
                .andExpect(jsonPath("$.dailySteps").isArray())
                .andExpect(jsonPath("$.dailySteps.length()").value(3))
                .andExpect(jsonPath("$.dailySteps[0].totalSteps.value").value(2000))
                .andExpect(jsonPath("$.dailySteps[1].totalSteps.value").value(3000))
                .andExpect(jsonPath("$.dailySteps[2].totalSteps.value").value(4000));

        verify(stepTrackingService, times(1)).getStepHistory(guardianId, fromDate, toDate);
    }

    @Test
    @DisplayName("Should return 400 for invalid date range")
    void shouldReturn400ForInvalidDateRange() throws Exception {
        // Given
        Long guardianId = 1L;

        // When & Then
        mockMvc.perform(
                        get("/api/v1/guardians/{guardianId}/steps/history", guardianId)
                                .param("from", "2025-07-10")
                                .param("to", "2025-07-01")) // Invalid: from > to
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Request"))
                .andExpect(
                        jsonPath("$.message")
                                .value("Invalid date range: from date cannot be after to date"));

        verify(stepTrackingService, never()).getStepHistory(any(), any(), any());
    }

    @Test
    @DisplayName("Should return 404 for non-existent guardian")
    void shouldReturn404ForNonExistentGuardian() throws Exception {
        // Given
        Long guardianId = 999L;

        when(stepTrackingService.getCurrentStepCount(guardianId))
                .thenThrow(
                        new GuardianNotFoundException("Guardian not found with ID: " + guardianId));

        // When & Then
        mockMvc.perform(get("/api/v1/guardians/{guardianId}/steps/current", guardianId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Guardian Not Found"))
                .andExpect(jsonPath("$.message").value("Guardian not found with ID: 999"));

        verify(stepTrackingService, times(1)).getCurrentStepCount(guardianId);
    }

    @Test
    @DisplayName("Should handle missing request body")
    void shouldHandleMissingRequestBody() throws Exception {
        // Given
        Long guardianId = 1L;

        // When & Then
        mockMvc.perform(
                        post("/api/v1/guardians/{guardianId}/steps", guardianId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Malformed JSON request"));

        verify(stepTrackingService, never()).submitSteps(any(), any());
    }

    @Test
    @DisplayName("Should handle malformed JSON")
    void shouldHandleMalformedJson() throws Exception {
        // Given
        Long guardianId = 1L;
        String malformedJson = "{ invalid json }";

        // When & Then
        mockMvc.perform(
                        post("/api/v1/guardians/{guardianId}/steps", guardianId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(malformedJson)
                                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Malformed JSON request"));

        verify(stepTrackingService, never()).submitSteps(any(), any());
    }

    @Test
    @DisplayName("Should validate required fields")
    void shouldValidateRequiredFields() throws Exception {
        // Given
        Long guardianId = 1L;
        String requestWithMissingFields = "{}";

        // When & Then
        mockMvc.perform(
                        post("/api/v1/guardians/{guardianId}/steps", guardianId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestWithMissingFields)
                                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[*].field").value(hasItem("stepCount")))
                .andExpect(jsonPath("$.fieldErrors[*].field").value(hasItem("timestamp")));

        verify(stepTrackingService, never()).submitSteps(any(), any());
    }
}

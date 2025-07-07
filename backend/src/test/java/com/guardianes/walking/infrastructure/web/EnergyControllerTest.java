package com.guardianes.walking.infrastructure.web;

import com.guardianes.walking.application.dto.EnergyBalanceResponse;
import com.guardianes.walking.application.dto.EnergySpendingRequest;
import com.guardianes.walking.application.dto.EnergySpendingResponse;
import com.guardianes.walking.application.service.EnergyManagementApplicationService;
import com.guardianes.walking.domain.EnergySpendingSource;
import com.guardianes.walking.domain.InsufficientEnergyException;
import com.guardianes.walking.domain.GuardianNotFoundException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.hamcrest.Matchers.hasItem;

@WebMvcTest(EnergyController.class)
@WithMockUser(username = "admin", roles = {"ADMIN"})
@DisplayName("Energy Controller Tests")
class EnergyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EnergyManagementApplicationService energyService;

    private EnergySpendingRequest validSpendingRequest;
    private EnergyBalanceResponse balanceResponse;
    private EnergySpendingResponse spendingResponse;

    @BeforeEach
    void setUp() {
        validSpendingRequest = new EnergySpendingRequest(100, EnergySpendingSource.BATTLE);
        balanceResponse = new EnergyBalanceResponse(1L, 750, Arrays.asList());
        spendingResponse = new EnergySpendingResponse(1L, 400, 100, EnergySpendingSource.BATTLE, "Energy spent successfully");
    }

    @Test
    @DisplayName("Should get energy balance successfully")
    void shouldGetEnergyBalanceSuccessfully() throws Exception {
        // Given
        Long guardianId = 1L;
        when(energyService.getEnergyBalance(guardianId))
            .thenReturn(balanceResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/guardians/{guardianId}/energy/balance", guardianId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guardianId").value(1))
                .andExpect(jsonPath("$.currentBalance").value(750))
                .andExpect(jsonPath("$.transactionSummary").isArray());

        verify(energyService, times(1)).getEnergyBalance(guardianId);
    }

    @Test
    @DisplayName("Should spend energy successfully")
    void shouldSpendEnergySuccessfully() throws Exception {
        // Given
        Long guardianId = 1L;
        when(energyService.spendEnergy(eq(guardianId), any(EnergySpendingRequest.class)))
            .thenReturn(spendingResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/guardians/{guardianId}/energy/spend", guardianId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validSpendingRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guardianId").value(1))
                .andExpect(jsonPath("$.newBalance").value(400))
                .andExpect(jsonPath("$.amountSpent").value(100))
                .andExpect(jsonPath("$.source").value("BATTLE"))
                .andExpect(jsonPath("$.message").value("Energy spent successfully"));

        verify(energyService, times(1)).spendEnergy(guardianId, validSpendingRequest);
    }

    @Test
    @DisplayName("Should return 400 for insufficient energy")
    void shouldReturn400ForInsufficientEnergy() throws Exception {
        // Given
        Long guardianId = 1L;
        when(energyService.spendEnergy(eq(guardianId), any(EnergySpendingRequest.class)))
            .thenThrow(new InsufficientEnergyException("Not enough energy available"));

        // When & Then
        mockMvc.perform(post("/api/v1/guardians/{guardianId}/energy/spend", guardianId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validSpendingRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Insufficient Energy"))
                .andExpect(jsonPath("$.message").value("Not enough energy available"));

        verify(energyService, times(1)).spendEnergy(guardianId, validSpendingRequest);
    }

    @Test
    @DisplayName("Should return 400 for invalid spending amount")
    void shouldReturn400ForInvalidSpendingAmount() throws Exception {
        // Given
        Long guardianId = 1L;
        EnergySpendingRequest invalidRequest = new EnergySpendingRequest(-50, EnergySpendingSource.BATTLE);

        // When & Then
        mockMvc.perform(post("/api/v1/guardians/{guardianId}/energy/spend", guardianId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[*].field").value(hasItem("amount")))
                .andExpect(jsonPath("$.fieldErrors[*].message").value(hasItem("Amount must be positive")));

        verify(energyService, never()).spendEnergy(any(), any());
    }

    @Test
    @DisplayName("Should return 400 for missing source")
    void shouldReturn400ForMissingSource() throws Exception {
        // Given
        Long guardianId = 1L;
        EnergySpendingRequest requestWithoutSource = new EnergySpendingRequest(100, null);

        // When & Then
        mockMvc.perform(post("/api/v1/guardians/{guardianId}/energy/spend", guardianId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestWithoutSource))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[*].field").value(hasItem("source")))
                .andExpect(jsonPath("$.fieldErrors[*].message").value(hasItem("Source is required")));

        verify(energyService, never()).spendEnergy(any(), any());
    }

    @Test
    @DisplayName("Should return 404 for non-existent guardian")
    void shouldReturn404ForNonExistentGuardian() throws Exception {
        // Given
        Long guardianId = 999L;
        when(energyService.getEnergyBalance(guardianId))
            .thenThrow(new GuardianNotFoundException("Guardian not found with ID: " + guardianId));

        // When & Then
        mockMvc.perform(get("/api/v1/guardians/{guardianId}/energy/balance", guardianId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Guardian Not Found"))
                .andExpect(jsonPath("$.message").value("Guardian not found with ID: 999"));

        verify(energyService, times(1)).getEnergyBalance(guardianId);
    }

    @Test
    @DisplayName("Should handle zero energy spending")
    void shouldHandleZeroEnergySpending() throws Exception {
        // Given
        Long guardianId = 1L;
        EnergySpendingRequest zeroRequest = new EnergySpendingRequest(0, EnergySpendingSource.BATTLE);

        // When & Then
        mockMvc.perform(post("/api/v1/guardians/{guardianId}/energy/spend", guardianId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zeroRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[*].field").value(hasItem("amount")))
                .andExpect(jsonPath("$.fieldErrors[*].message").value(hasItem("Amount must be positive")));

        verify(energyService, never()).spendEnergy(any(), any());
    }

    @Test
    @DisplayName("Should validate source enum values")
    void shouldValidateSourceEnumValues() throws Exception {
        // Given
        Long guardianId = 1L;
        String requestWithInvalidSource = """
            {
                "amount": 100,
                "source": "INVALID_SOURCE"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/v1/guardians/{guardianId}/energy/spend", guardianId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestWithInvalidSource)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid energy spending source"));

        verify(energyService, never()).spendEnergy(any(), any());
    }

    @Test
    @DisplayName("Should handle large energy amounts")
    void shouldHandleLargeEnergyAmounts() throws Exception {
        // Given
        Long guardianId = 1L;
        EnergySpendingRequest largeRequest = new EnergySpendingRequest(1000000, EnergySpendingSource.BATTLE);

        when(energyService.spendEnergy(eq(guardianId), any(EnergySpendingRequest.class)))
            .thenThrow(new InsufficientEnergyException("Not enough energy available"));

        // When & Then
        mockMvc.perform(post("/api/v1/guardians/{guardianId}/energy/spend", guardianId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(largeRequest))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Insufficient Energy"))
                .andExpect(jsonPath("$.message").value("Not enough energy available"));

        verify(energyService, times(1)).spendEnergy(guardianId, largeRequest);
    }
}
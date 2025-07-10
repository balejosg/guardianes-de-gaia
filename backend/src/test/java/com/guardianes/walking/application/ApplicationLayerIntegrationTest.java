package com.guardianes.walking.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.shared.infrastructure.metrics.BusinessMetricsService;
import com.guardianes.walking.application.dto.EnergyBalanceResponse;
import com.guardianes.walking.application.dto.EnergySpendingRequest;
import com.guardianes.walking.application.dto.EnergySpendingResponse;
import com.guardianes.walking.application.service.impl.EnergyManagementApplicationServiceImpl;
import com.guardianes.walking.domain.model.*;
import com.guardianes.walking.domain.service.EnergyManagementDomainService;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Integration test for the refactored application layer. Tests that application services correctly
 * orchestrate domain services.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Application Layer Integration Tests")
class ApplicationLayerIntegrationTest {

    @Mock private EnergyManagementDomainService energyManagementDomainService;

    @Mock private BusinessMetricsService metricsService;

    private EnergyManagementApplicationServiceImpl applicationService;

    @BeforeEach
    void setUp() {
        applicationService =
                new EnergyManagementApplicationServiceImpl(
                        energyManagementDomainService, metricsService);
    }

    @Test
    @DisplayName("Should get energy balance and return proper response")
    void shouldGetEnergyBalanceAndReturnProperResponse() {
        // Given
        Long guardianId = 123L;
        GuardianId domainGuardianId = GuardianId.of(guardianId);
        Energy currentBalance = Energy.of(250);

        EnergyTransaction transaction1 =
                EnergyTransaction.earned(domainGuardianId, Energy.of(100), EnergySource.steps());
        EnergyTransaction transaction2 =
                EnergyTransaction.spent(domainGuardianId, Energy.of(50), EnergySource.battle());

        when(energyManagementDomainService.calculateCurrentBalance(domainGuardianId))
                .thenReturn(currentBalance);
        when(energyManagementDomainService.getRecentTransactions(domainGuardianId, 10))
                .thenReturn(Arrays.asList(transaction1, transaction2));

        // When
        EnergyBalanceResponse response = applicationService.getEnergyBalance(guardianId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.guardianId()).isEqualTo(domainGuardianId);
        assertThat(response.currentBalance()).isEqualTo(currentBalance);
        assertThat(response.transactionSummary()).hasSize(2);
        assertThat(response.transactionSummary()).containsExactly(transaction1, transaction2);

        verify(energyManagementDomainService).calculateCurrentBalance(domainGuardianId);
        verify(energyManagementDomainService).getRecentTransactions(domainGuardianId, 10);
    }

    @Test
    @DisplayName("Should process energy spending successfully")
    void shouldProcessEnergySpendingSuccessfully() {
        // Given
        Long guardianId = 456L;
        GuardianId domainGuardianId = GuardianId.of(guardianId);
        Energy spendingAmount = Energy.of(75);
        EnergySpendingSource spendingSource = EnergySpendingSource.BATTLE;
        EnergySpendingRequest request =
                new EnergySpendingRequest(spendingAmount.amount(), spendingSource);

        Energy currentBalance = Energy.of(200);
        Energy newBalance = Energy.of(125); // 200 - 75

        EnergyTransaction spentTransaction =
                EnergyTransaction.spent(
                        domainGuardianId, spendingAmount, EnergySource.of(spendingSource.name()));

        when(energyManagementDomainService.calculateCurrentBalance(domainGuardianId))
                .thenReturn(currentBalance);
        when(energyManagementDomainService.spendEnergy(
                        domainGuardianId, spendingAmount, EnergySource.of(spendingSource.name())))
                .thenReturn(spentTransaction);

        // When
        EnergySpendingResponse response = applicationService.spendEnergy(guardianId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.guardianId()).isEqualTo(domainGuardianId);
        assertThat(response.newBalance()).isEqualTo(newBalance);
        assertThat(response.amountSpent()).isEqualTo(spendingAmount);
        assertThat(response.source()).isEqualTo(spendingSource);
        assertThat(response.message()).isEqualTo("Energy spent successfully");

        verify(energyManagementDomainService).calculateCurrentBalance(domainGuardianId);
        verify(energyManagementDomainService)
                .spendEnergy(
                        domainGuardianId, spendingAmount, EnergySource.of(spendingSource.name()));
        verify(metricsService)
                .recordEnergySpent(guardianId, spendingAmount.amount(), spendingSource.name());
    }

    @Test
    @DisplayName("Should handle insufficient energy exception")
    void shouldHandleInsufficientEnergyException() {
        // Given
        Long guardianId = 789L;
        GuardianId domainGuardianId = GuardianId.of(guardianId);
        Energy spendingAmount = Energy.of(150);
        EnergySpendingSource spendingSource = EnergySpendingSource.BATTLE;
        EnergySpendingRequest request =
                new EnergySpendingRequest(spendingAmount.amount(), spendingSource);

        Energy currentBalance = Energy.of(50); // Insufficient

        when(energyManagementDomainService.calculateCurrentBalance(domainGuardianId))
                .thenReturn(currentBalance);
        when(energyManagementDomainService.spendEnergy(
                        domainGuardianId, spendingAmount, EnergySource.of(spendingSource.name())))
                .thenThrow(new InsufficientEnergyException("Not enough energy available"));

        // When & Then
        InsufficientEnergyException exception =
                assertThrows(
                        InsufficientEnergyException.class,
                        () -> applicationService.spendEnergy(guardianId, request));

        assertThat(exception.getMessage()).contains("Not enough energy available");

        verify(energyManagementDomainService, times(2)).calculateCurrentBalance(domainGuardianId);
        verify(energyManagementDomainService)
                .spendEnergy(
                        domainGuardianId, spendingAmount, EnergySource.of(spendingSource.name()));
        verify(metricsService)
                .recordInsufficientEnergy(
                        guardianId, spendingAmount.amount(), currentBalance.amount());
    }

    @Test
    @DisplayName("Should handle empty transaction history")
    void shouldHandleEmptyTransactionHistory() {
        // Given
        Long guardianId = 999L;
        GuardianId domainGuardianId = GuardianId.of(guardianId);
        Energy zeroBalance = Energy.of(0);

        when(energyManagementDomainService.calculateCurrentBalance(domainGuardianId))
                .thenReturn(zeroBalance);
        when(energyManagementDomainService.getRecentTransactions(domainGuardianId, 10))
                .thenReturn(Collections.emptyList());

        // When
        EnergyBalanceResponse response = applicationService.getEnergyBalance(guardianId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.guardianId()).isEqualTo(domainGuardianId);
        assertThat(response.currentBalance().amount()).isEqualTo(0);
        assertThat(response.transactionSummary()).isEmpty();
    }

    @Test
    @DisplayName("Should convert between application and domain types correctly")
    void shouldConvertBetweenApplicationAndDomainTypesCorrectly() {
        // Given
        Long guardianId = 111L;
        GuardianId domainGuardianId = GuardianId.of(guardianId);
        Energy spendingAmount = Energy.of(25);
        EnergySpendingSource applicationSource = EnergySpendingSource.SHOP;
        EnergySource domainSource = EnergySource.of(applicationSource.name());

        EnergySpendingRequest request =
                new EnergySpendingRequest(spendingAmount.amount(), applicationSource);

        Energy currentBalance = Energy.of(100);
        EnergyTransaction spentTransaction =
                EnergyTransaction.spent(domainGuardianId, spendingAmount, domainSource);

        when(energyManagementDomainService.calculateCurrentBalance(domainGuardianId))
                .thenReturn(currentBalance);
        when(energyManagementDomainService.spendEnergy(
                        domainGuardianId, spendingAmount, domainSource))
                .thenReturn(spentTransaction);

        // When
        EnergySpendingResponse response = applicationService.spendEnergy(guardianId, request);

        // Then
        // Verify proper type conversions
        assertThat(response.guardianId()).isEqualTo(domainGuardianId); // Long -> GuardianId
        assertThat(response.amountSpent()).isEqualTo(spendingAmount); // Energy preserved
        assertThat(response.source()).isEqualTo(applicationSource); // Domain -> Application enum

        verify(energyManagementDomainService)
                .spendEnergy(domainGuardianId, spendingAmount, domainSource);
    }

    @Test
    @DisplayName("Should record metrics for all operations")
    void shouldRecordMetricsForAllOperations() {
        // Given
        Long guardianId = 222L;
        GuardianId domainGuardianId = GuardianId.of(guardianId);
        Energy spendingAmount = Energy.of(40);
        EnergySpendingSource spendingSource = EnergySpendingSource.CHALLENGE;
        EnergySpendingRequest request =
                new EnergySpendingRequest(spendingAmount.amount(), spendingSource);

        Energy currentBalance = Energy.of(100);
        EnergyTransaction spentTransaction =
                EnergyTransaction.spent(
                        domainGuardianId, spendingAmount, EnergySource.of(spendingSource.name()));

        when(energyManagementDomainService.calculateCurrentBalance(domainGuardianId))
                .thenReturn(currentBalance);
        when(energyManagementDomainService.spendEnergy(any(), any(), any()))
                .thenReturn(spentTransaction);

        // When
        applicationService.spendEnergy(guardianId, request);

        // Then
        verify(metricsService)
                .recordEnergySpent(guardianId, spendingAmount.amount(), spendingSource.name());
    }

    @Test
    @DisplayName("Should validate all EnergySpendingSource enum values")
    void shouldValidateAllEnergySpendingSourceEnumValues() {
        // Given
        Long guardianId = 333L;
        GuardianId domainGuardianId = GuardianId.of(guardianId);
        Energy spendingAmount = Energy.of(10);
        Energy currentBalance = Energy.of(100);

        when(energyManagementDomainService.calculateCurrentBalance(domainGuardianId))
                .thenReturn(currentBalance);
        when(energyManagementDomainService.spendEnergy(any(), any(), any()))
                .thenReturn(mock(EnergyTransaction.class));

        // Test all enum values
        for (EnergySpendingSource source : EnergySpendingSource.values()) {
            // When
            EnergySpendingRequest request =
                    new EnergySpendingRequest(spendingAmount.amount(), source);

            // Then - Should not throw exception
            assertDoesNotThrow(() -> applicationService.spendEnergy(guardianId, request));

            // Verify domain service called with correct converted source
            verify(energyManagementDomainService)
                    .spendEnergy(domainGuardianId, spendingAmount, EnergySource.of(source.name()));
        }
    }
}

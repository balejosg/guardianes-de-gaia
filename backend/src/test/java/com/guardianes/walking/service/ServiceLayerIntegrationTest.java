package com.guardianes.walking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.walking.domain.model.*;
import com.guardianes.walking.domain.repository.EnergyRepository;
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
 * Integration test for the refactored service layer. Tests that domain services work correctly with
 * the new architecture.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Service Layer Integration Tests")
class ServiceLayerIntegrationTest {

    @Mock private EnergyRepository energyRepository;

    private EnergyManagementDomainService energyManagementService;

    @BeforeEach
    void setUp() {
        energyManagementService = new EnergyManagementDomainService(energyRepository);
    }

    @Test
    @DisplayName("Should calculate current energy balance correctly")
    void shouldCalculateCurrentEnergyBalanceCorrectly() {
        // Given
        GuardianId guardianId = GuardianId.of(1L);

        EnergyTransaction earnedTransaction1 =
                EnergyTransaction.earned(guardianId, Energy.of(100), EnergySource.steps());
        EnergyTransaction earnedTransaction2 =
                EnergyTransaction.earned(guardianId, Energy.of(50), EnergySource.of("CHALLENGE"));
        EnergyTransaction spentTransaction =
                EnergyTransaction.spent(guardianId, Energy.of(30), EnergySource.battle());

        when(energyRepository.findTransactionsByGuardianId(guardianId))
                .thenReturn(
                        Arrays.asList(earnedTransaction1, earnedTransaction2, spentTransaction));

        // When
        Energy currentBalance = energyManagementService.calculateCurrentBalance(guardianId);

        // Then
        assertThat(currentBalance.amount()).isEqualTo(120); // 100 + 50 - 30 = 120
        verify(energyRepository).findTransactionsByGuardianId(guardianId);
    }

    @Test
    @DisplayName("Should handle negative balance by returning zero")
    void shouldHandleNegativeBalanceByReturningZero() {
        // Given
        GuardianId guardianId = GuardianId.of(2L);

        EnergyTransaction earnedTransaction =
                EnergyTransaction.earned(guardianId, Energy.of(50), EnergySource.steps());
        EnergyTransaction spentTransaction =
                EnergyTransaction.spent(guardianId, Energy.of(100), EnergySource.battle());

        when(energyRepository.findTransactionsByGuardianId(guardianId))
                .thenReturn(Arrays.asList(earnedTransaction, spentTransaction));

        // When
        Energy currentBalance = energyManagementService.calculateCurrentBalance(guardianId);

        // Then
        assertThat(currentBalance.amount()).isEqualTo(0); // Should not go negative
    }

    @Test
    @DisplayName("Should process energy spending with validation")
    void shouldProcessEnergySpendingWithValidation() {
        // Given
        GuardianId guardianId = GuardianId.of(3L);
        Energy currentBalance = Energy.of(200);
        Energy spendingAmount = Energy.of(50);
        EnergySource battleSource = EnergySource.battle();

        // Mock current balance calculation
        EnergyTransaction existingTransaction =
                EnergyTransaction.earned(guardianId, currentBalance, EnergySource.steps());
        when(energyRepository.findTransactionsByGuardianId(guardianId))
                .thenReturn(Collections.singletonList(existingTransaction));

        // Mock transaction saving
        EnergyTransaction expectedSpentTransaction =
                EnergyTransaction.spent(guardianId, spendingAmount, battleSource);
        when(energyRepository.saveTransaction(any(EnergyTransaction.class)))
                .thenReturn(expectedSpentTransaction);

        // When
        EnergyTransaction result =
                energyManagementService.spendEnergy(guardianId, spendingAmount, battleSource);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getGuardianId()).isEqualTo(guardianId);
        assertThat(result.getAmount()).isEqualTo(spendingAmount);
        assertThat(result.getSource()).isEqualTo(battleSource);
        assertThat(result.isSpending()).isTrue();

        verify(energyRepository).findTransactionsByGuardianId(guardianId);
        verify(energyRepository).saveTransaction(any(EnergyTransaction.class));
    }

    @Test
    @DisplayName("Should reject spending when insufficient energy")
    void shouldRejectSpendingWhenInsufficientEnergy() {
        // Given
        GuardianId guardianId = GuardianId.of(4L);
        Energy currentBalance = Energy.of(30);
        Energy spendingAmount = Energy.of(50);
        EnergySource battleSource = EnergySource.battle();

        // Mock current balance calculation
        EnergyTransaction existingTransaction =
                EnergyTransaction.earned(guardianId, currentBalance, EnergySource.steps());
        when(energyRepository.findTransactionsByGuardianId(guardianId))
                .thenReturn(Collections.singletonList(existingTransaction));

        // When & Then
        InsufficientEnergyException exception =
                assertThrows(
                        InsufficientEnergyException.class,
                        () ->
                                energyManagementService.spendEnergy(
                                        guardianId, spendingAmount, battleSource));

        assertThat(exception.getMessage()).contains("Insufficient energy");
        verify(energyRepository).findTransactionsByGuardianId(guardianId);
        verify(energyRepository, never()).saveTransaction(any());
    }

    @Test
    @DisplayName("Should check if guardian has sufficient energy")
    void shouldCheckIfGuardianHasSufficientEnergy() {
        // Given
        GuardianId guardianId = GuardianId.of(5L);
        Energy currentBalance = Energy.of(100);

        EnergyTransaction existingTransaction =
                EnergyTransaction.earned(guardianId, currentBalance, EnergySource.steps());
        when(energyRepository.findTransactionsByGuardianId(guardianId))
                .thenReturn(Collections.singletonList(existingTransaction));

        // When & Then
        assertTrue(energyManagementService.hasSufficientEnergy(guardianId, Energy.of(50)));
        assertTrue(energyManagementService.hasSufficientEnergy(guardianId, Energy.of(100)));
        assertFalse(energyManagementService.hasSufficientEnergy(guardianId, Energy.of(150)));
    }

    @Test
    @DisplayName("Should get recent transactions with limit")
    void shouldGetRecentTransactionsWithLimit() {
        // Given
        GuardianId guardianId = GuardianId.of(6L);
        int limit = 5;

        EnergyTransaction transaction1 =
                EnergyTransaction.earned(guardianId, Energy.of(100), EnergySource.steps());
        EnergyTransaction transaction2 =
                EnergyTransaction.spent(guardianId, Energy.of(50), EnergySource.battle());

        when(energyRepository.getRecentTransactions(guardianId, limit))
                .thenReturn(Arrays.asList(transaction1, transaction2));

        // When
        var recentTransactions = energyManagementService.getRecentTransactions(guardianId, limit);

        // Then
        assertThat(recentTransactions).hasSize(2);
        assertThat(recentTransactions).containsExactly(transaction1, transaction2);
        verify(energyRepository).getRecentTransactions(guardianId, limit);
    }

    @Test
    @DisplayName("Should calculate energy statistics")
    void shouldCalculateEnergyStatistics() {
        // Given
        GuardianId guardianId = GuardianId.of(7L);

        EnergyTransaction earnedFromSteps =
                EnergyTransaction.earned(guardianId, Energy.of(100), EnergySource.steps());
        EnergyTransaction earnedFromChallenge =
                EnergyTransaction.earned(guardianId, Energy.of(50), EnergySource.of("CHALLENGE"));
        EnergyTransaction spentOnBattle =
                EnergyTransaction.spent(guardianId, Energy.of(30), EnergySource.battle());

        when(energyRepository.findTransactionsByGuardianId(guardianId))
                .thenReturn(Arrays.asList(earnedFromSteps, earnedFromChallenge, spentOnBattle));

        // When
        var statistics = energyManagementService.calculateEnergyStatistics(guardianId);

        // Then
        assertThat(statistics.totalEarned().amount()).isEqualTo(150); // 100 + 50
        assertThat(statistics.totalSpent().amount()).isEqualTo(30);
        assertThat(statistics.currentBalance().amount()).isEqualTo(120); // 150 - 30
        assertThat(statistics.totalTransactions()).isEqualTo(3);
        assertThat(statistics.stepBasedTransactions()).isEqualTo(1); // Only steps transaction
        assertThat(statistics.getStepTransactionPercentage()).isEqualTo(33.33, within(0.01));
    }

    @Test
    @DisplayName("Should validate null parameters")
    void shouldValidateNullParameters() {
        // Given
        GuardianId guardianId = GuardianId.of(1L);
        Energy energy = Energy.of(50);
        EnergySource source = EnergySource.battle();

        // When & Then
        assertThrows(
                NullPointerException.class,
                () -> energyManagementService.calculateCurrentBalance(null));

        assertThrows(
                NullPointerException.class,
                () -> energyManagementService.spendEnergy(null, energy, source));

        assertThrows(
                NullPointerException.class,
                () -> energyManagementService.spendEnergy(guardianId, null, source));

        assertThrows(
                NullPointerException.class,
                () -> energyManagementService.spendEnergy(guardianId, energy, null));

        assertThrows(
                IllegalArgumentException.class,
                () -> energyManagementService.getRecentTransactions(guardianId, 0));

        assertThrows(
                IllegalArgumentException.class,
                () -> energyManagementService.getRecentTransactions(guardianId, -1));
    }

    private static org.assertj.core.data.Offset<Double> within(double offset) {
        return org.assertj.core.data.Offset.offset(offset);
    }
}

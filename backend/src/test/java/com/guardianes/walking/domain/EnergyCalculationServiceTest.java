package com.guardianes.walking.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Energy Calculation Service Tests")
class EnergyCalculationServiceTest {

    @Mock
    private EnergyRepository energyRepository;

    @Mock
    private StepAggregationService stepAggregationService;

    private EnergyCalculationService energyCalculationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        energyCalculationService = new EnergyCalculationService(energyRepository, stepAggregationService);
    }

    @Test
    @DisplayName("Should calculate energy from steps (1 energy = 10 steps)")
    void shouldCalculateEnergyFromSteps() {
        // Given
        int steps = 1000;
        int expectedEnergy = 100;

        // When
        int actualEnergy = energyCalculationService.calculateEnergyFromSteps(steps);

        // Then
        assertEquals(expectedEnergy, actualEnergy);
    }

    @Test
    @DisplayName("Should handle partial steps correctly")
    void shouldHandlePartialStepsCorrectly() {
        // Given
        int steps = 1005;
        int expectedEnergy = 100; // Should floor the result

        // When
        int actualEnergy = energyCalculationService.calculateEnergyFromSteps(steps);

        // Then
        assertEquals(expectedEnergy, actualEnergy);
    }

    @Test
    @DisplayName("Should return zero energy for zero steps")
    void shouldReturnZeroEnergyForZeroSteps() {
        // Given
        int steps = 0;
        int expectedEnergy = 0;

        // When
        int actualEnergy = energyCalculationService.calculateEnergyFromSteps(steps);

        // Then
        assertEquals(expectedEnergy, actualEnergy);
    }

    @Test
    @DisplayName("Should convert daily steps to energy and create transaction")
    void shouldConvertDailyStepsToEnergyAndCreateTransaction() {
        // Given
        Long guardianId = 1L;
        LocalDate date = LocalDate.now();
        int dailySteps = 5000;
        int expectedEnergy = 500;
        
        DailyStepAggregate stepAggregate = new DailyStepAggregate(guardianId, date, dailySteps);
        when(stepAggregationService.aggregateDailySteps(guardianId, date))
            .thenReturn(stepAggregate);

        EnergyTransaction expectedTransaction = new EnergyTransaction(
            guardianId, 
            EnergyTransactionType.EARNED, 
            expectedEnergy, 
            "DAILY_STEPS", 
            LocalDateTime.now()
        );
        when(energyRepository.saveTransaction(any(EnergyTransaction.class)))
            .thenReturn(expectedTransaction);

        // When
        EnergyTransaction result = energyCalculationService.convertDailyStepsToEnergy(guardianId, date);

        // Then
        assertNotNull(result);
        assertEquals(expectedEnergy, result.getAmount());
        assertEquals(EnergyTransactionType.EARNED, result.getType());
        assertEquals("DAILY_STEPS", result.getSource());
        assertEquals(guardianId, result.getGuardianId());
        
        verify(stepAggregationService, times(1)).aggregateDailySteps(guardianId, date);
        verify(energyRepository, times(1)).saveTransaction(any(EnergyTransaction.class));
    }

    @Test
    @DisplayName("Should get current energy balance for guardian")
    void shouldGetCurrentEnergyBalanceForGuardian() {
        // Given
        Long guardianId = 1L;
        List<EnergyTransaction> transactions = Arrays.asList(
            new EnergyTransaction(guardianId, EnergyTransactionType.EARNED, 100, "DAILY_STEPS", LocalDateTime.now()),
            new EnergyTransaction(guardianId, EnergyTransactionType.EARNED, 200, "DAILY_STEPS", LocalDateTime.now()),
            new EnergyTransaction(guardianId, EnergyTransactionType.SPENT, 50, "BATTLE", LocalDateTime.now())
        );
        
        when(energyRepository.findTransactionsByGuardianId(guardianId))
            .thenReturn(transactions);

        // When
        int currentBalance = energyCalculationService.getCurrentEnergyBalance(guardianId);

        // Then
        assertEquals(250, currentBalance); // 100 + 200 - 50
        verify(energyRepository, times(1)).findTransactionsByGuardianId(guardianId);
    }

    @Test
    @DisplayName("Should handle negative balance correctly")
    void shouldHandleNegativeBalanceCorrectly() {
        // Given
        Long guardianId = 1L;
        List<EnergyTransaction> transactions = Arrays.asList(
            new EnergyTransaction(guardianId, EnergyTransactionType.EARNED, 100, "DAILY_STEPS", LocalDateTime.now()),
            new EnergyTransaction(guardianId, EnergyTransactionType.SPENT, 150, "BATTLE", LocalDateTime.now())
        );
        
        when(energyRepository.findTransactionsByGuardianId(guardianId))
            .thenReturn(transactions);

        // When
        int currentBalance = energyCalculationService.getCurrentEnergyBalance(guardianId);

        // Then
        assertEquals(0, currentBalance); // Should not go negative
        verify(energyRepository, times(1)).findTransactionsByGuardianId(guardianId);
    }

    @Test
    @DisplayName("Should spend energy and create transaction")
    void shouldSpendEnergyAndCreateTransaction() {
        // Given
        Long guardianId = 1L;
        int energyToSpend = 50;
        String source = "BATTLE";
        
        // Mock current balance
        when(energyRepository.findTransactionsByGuardianId(guardianId))
            .thenReturn(Arrays.asList(
                new EnergyTransaction(guardianId, EnergyTransactionType.EARNED, 100, "DAILY_STEPS", LocalDateTime.now())
            ));

        EnergyTransaction expectedTransaction = new EnergyTransaction(
            guardianId, 
            EnergyTransactionType.SPENT, 
            energyToSpend, 
            source, 
            LocalDateTime.now()
        );
        when(energyRepository.saveTransaction(any(EnergyTransaction.class)))
            .thenReturn(expectedTransaction);

        // When
        EnergyTransaction result = energyCalculationService.spendEnergy(guardianId, energyToSpend, source);

        // Then
        assertNotNull(result);
        assertEquals(energyToSpend, result.getAmount());
        assertEquals(EnergyTransactionType.SPENT, result.getType());
        assertEquals(source, result.getSource());
        assertEquals(guardianId, result.getGuardianId());
        
        verify(energyRepository, times(1)).findTransactionsByGuardianId(guardianId);
        verify(energyRepository, times(1)).saveTransaction(any(EnergyTransaction.class));
    }

    @Test
    @DisplayName("Should throw exception when trying to spend more energy than available")
    void shouldThrowExceptionWhenTryingToSpendMoreEnergyThanAvailable() {
        // Given
        Long guardianId = 1L;
        int energyToSpend = 150;
        String source = "BATTLE";
        
        // Mock current balance (only 100 energy available)
        when(energyRepository.findTransactionsByGuardianId(guardianId))
            .thenReturn(Arrays.asList(
                new EnergyTransaction(guardianId, EnergyTransactionType.EARNED, 100, "DAILY_STEPS", LocalDateTime.now())
            ));

        // When & Then
        assertThrows(InsufficientEnergyException.class, () -> {
            energyCalculationService.spendEnergy(guardianId, energyToSpend, source);
        });
        
        verify(energyRepository, times(1)).findTransactionsByGuardianId(guardianId);
        verify(energyRepository, never()).saveTransaction(any(EnergyTransaction.class));
    }

    @Test
    @DisplayName("Should get energy transaction history for guardian")
    void shouldGetEnergyTransactionHistoryForGuardian() {
        // Given
        Long guardianId = 1L;
        LocalDate fromDate = LocalDate.now().minusDays(7);
        LocalDate toDate = LocalDate.now();
        
        List<EnergyTransaction> expectedTransactions = Arrays.asList(
            new EnergyTransaction(guardianId, EnergyTransactionType.EARNED, 100, "DAILY_STEPS", fromDate.atTime(10, 0)),
            new EnergyTransaction(guardianId, EnergyTransactionType.SPENT, 50, "BATTLE", fromDate.plusDays(1).atTime(14, 0))
        );
        
        when(energyRepository.findTransactionsByGuardianIdAndDateRange(guardianId, fromDate, toDate))
            .thenReturn(expectedTransactions);

        // When
        List<EnergyTransaction> result = energyCalculationService.getEnergyHistory(guardianId, fromDate, toDate);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(100, result.get(0).getAmount());
        assertEquals(EnergyTransactionType.EARNED, result.get(0).getType());
        assertEquals(50, result.get(1).getAmount());
        assertEquals(EnergyTransactionType.SPENT, result.get(1).getType());
        
        verify(energyRepository, times(1)).findTransactionsByGuardianIdAndDateRange(guardianId, fromDate, toDate);
    }
}
package com.guardianes.walking.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Energy Value Object Tests")
class EnergyTest {

    @Test
    @DisplayName("Should create valid energy amount")
    void shouldCreateValidEnergyAmount() {
        // Given
        int validAmount = 100;

        // When
        Energy energy = Energy.of(validAmount);

        // Then
        assertEquals(validAmount, energy.amount());
    }

    @Test
    @DisplayName("Should create zero energy")
    void shouldCreateZeroEnergy() {
        // When
        Energy energy = Energy.zero();

        // Then
        assertEquals(0, energy.amount());
        assertTrue(energy.isZero());
    }

    @Test
    @DisplayName("Should reject negative energy amount")
    void shouldRejectNegativeEnergyAmount() {
        // Given
        int negativeAmount = -50;

        // When & Then
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> Energy.of(negativeAmount));

        assertTrue(exception.getMessage().contains("cannot be negative"));
    }

    @Test
    @DisplayName("Should convert steps to energy correctly")
    void shouldConvertStepsToEnergyCorrectly() {
        // Given
        StepCount steps = StepCount.of(100); // 100 steps = 10 energy

        // When
        Energy energy = Energy.fromSteps(steps);

        // Then
        assertEquals(10, energy.amount());
    }

    @Test
    @DisplayName("Should handle step to energy conversion with remainder")
    void shouldHandleStepToEnergyConversionWithRemainder() {
        // Given
        StepCount steps = StepCount.of(105); // 105 steps = 10 energy (5 steps remainder)

        // When
        Energy energy = Energy.fromSteps(steps);

        // Then
        assertEquals(10, energy.amount());
    }

    @Test
    @DisplayName("Should add energy amounts correctly")
    void shouldAddEnergyAmountsCorrectly() {
        // Given
        Energy first = Energy.of(50);
        Energy second = Energy.of(30);

        // When
        Energy result = first.add(second);

        // Then
        assertEquals(80, result.amount());
    }

    @Test
    @DisplayName("Should subtract energy amounts correctly")
    void shouldSubtractEnergyAmountsCorrectly() {
        // Given
        Energy first = Energy.of(100);
        Energy second = Energy.of(30);

        // When
        Energy result = first.subtract(second);

        // Then
        assertEquals(70, result.amount());
    }

    @Test
    @DisplayName("Should throw exception when subtracting more energy than available")
    void shouldThrowExceptionWhenSubtractingMoreEnergyThanAvailable() {
        // Given
        Energy first = Energy.of(50);
        Energy second = Energy.of(100);

        // When & Then
        InsufficientEnergyException exception =
                assertThrows(InsufficientEnergyException.class, () -> first.subtract(second));

        assertTrue(exception.getMessage().contains("insufficient energy"));
    }

    @Test
    @DisplayName("Should check if energy is sufficient correctly")
    void shouldCheckIfEnergyIsSufficientCorrectly() {
        // Given
        Energy available = Energy.of(100);
        Energy required = Energy.of(50);
        Energy excessive = Energy.of(150);

        // When & Then
        assertTrue(available.isSufficientFor(required));
        assertTrue(available.isGreaterThanOrEqual(required));
        assertFalse(available.isSufficientFor(excessive));
        assertFalse(available.isGreaterThanOrEqual(excessive));
    }

    @Test
    @DisplayName("Should handle equality correctly")
    void shouldHandleEqualityCorrectly() {
        // Given
        Energy first = Energy.of(100);
        Energy second = Energy.of(100);
        Energy different = Energy.of(200);

        // When & Then
        assertEquals(first, second);
        assertNotEquals(first, different);
        assertEquals(first.hashCode(), second.hashCode());
    }
}

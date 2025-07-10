package com.guardianes.walking.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.shared.domain.model.Timestamp;
import com.guardianes.walking.domain.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration test for the refactored domain model. Tests the core value objects and domain
 * entities work together correctly.
 */
@DisplayName("Domain Model Integration Tests")
class DomainModelIntegrationTest {

    @Test
    @DisplayName("Should create and validate GuardianId value object")
    void shouldCreateAndValidateGuardianId() {
        // When
        GuardianId guardianId = GuardianId.of(123L);

        // Then
        assertThat(guardianId.value()).isEqualTo(123L);
        assertThat(guardianId).isNotNull();
    }

    @Test
    @DisplayName("Should create and validate StepCount value object")
    void shouldCreateAndValidateStepCount() {
        // When
        StepCount stepCount = StepCount.of(1000);

        // Then
        assertThat(stepCount.value()).isEqualTo(1000);

        // Test validation
        assertThrows(IllegalArgumentException.class, () -> StepCount.of(-1));
        assertThrows(IllegalArgumentException.class, () -> StepCount.of(60000));
    }

    @Test
    @DisplayName("Should create and validate Energy value object")
    void shouldCreateAndValidateEnergy() {
        // When
        Energy energy = Energy.of(100);

        // Then
        assertThat(energy.amount()).isEqualTo(100);

        // Test arithmetic operations
        Energy doubled = energy.add(energy);
        assertThat(doubled.amount()).isEqualTo(200);

        Energy subtracted = doubled.subtract(energy);
        assertThat(subtracted.amount()).isEqualTo(100);
    }

    @Test
    @DisplayName("Should create StepRecord with business logic")
    void shouldCreateStepRecordWithBusinessLogic() {
        // Given
        GuardianId guardianId = GuardianId.of(1L);
        StepCount stepCount = StepCount.of(1000);

        // When
        StepRecord stepRecord = StepRecord.create(guardianId, stepCount);

        // Then
        assertThat(stepRecord.getGuardianId()).isEqualTo(guardianId);
        assertThat(stepRecord.getStepCount()).isEqualTo(stepCount);
        assertThat(stepRecord.getRecordedAt()).isNotNull();

        // Test business logic
        Energy energyGenerated = stepRecord.calculateEnergyGenerated();
        assertThat(energyGenerated.amount()).isEqualTo(100); // 1000 steps / 10 = 100 energy
    }

    @Test
    @DisplayName("Should create EnergyTransaction with factory methods")
    void shouldCreateEnergyTransactionWithFactoryMethods() {
        // Given
        GuardianId guardianId = GuardianId.of(1L);
        Energy energy = Energy.of(50);
        EnergySource source = EnergySource.steps();

        // When - Create earned transaction
        EnergyTransaction earnedTransaction = EnergyTransaction.earned(guardianId, energy, source);

        // Then
        assertThat(earnedTransaction.getGuardianId()).isEqualTo(guardianId);
        assertThat(earnedTransaction.getAmount()).isEqualTo(energy);
        assertThat(earnedTransaction.getSource()).isEqualTo(source);
        assertThat(earnedTransaction.getType()).isEqualTo(EnergyTransactionType.EARNED);
        assertThat(earnedTransaction.isEarning()).isTrue();
        assertThat(earnedTransaction.isSpending()).isFalse();

        // When - Create spent transaction
        EnergySource battleSource = EnergySource.of("BATTLE");
        EnergyTransaction spentTransaction =
                EnergyTransaction.spent(guardianId, energy, battleSource);

        // Then
        assertThat(spentTransaction.getType()).isEqualTo(EnergyTransactionType.SPENT);
        assertThat(spentTransaction.isSpending()).isTrue();
        assertThat(spentTransaction.isEarning()).isFalse();
    }

    @Test
    @DisplayName("Should validate EnergySource value object")
    void shouldValidateEnergySource() {
        // When
        EnergySource stepsSource = EnergySource.steps();
        EnergySource battleSource = EnergySource.battle();
        EnergySource customSource = EnergySource.of("CUSTOM");

        // Then
        assertThat(stepsSource.name()).isEqualTo("STEPS");
        assertThat(battleSource.name()).isEqualTo("BATTLE");
        assertThat(customSource.name()).isEqualTo("CUSTOM");

        // Test validation
        assertThrows(IllegalArgumentException.class, () -> EnergySource.of(""));
        assertThrows(IllegalArgumentException.class, () -> EnergySource.of(null));
    }

    @Test
    @DisplayName("Should validate Timestamp value object")
    void shouldValidateTimestamp() {
        // When
        Timestamp now = Timestamp.now();
        Timestamp specific = Timestamp.of(java.time.LocalDateTime.now().minusHours(1));

        // Then
        assertThat(now.value()).isNotNull();
        assertThat(specific.value()).isNotNull();
        assertThat(now.isAfter(specific)).isTrue();

        // Test validation - future timestamps should be rejected
        assertThrows(
                IllegalArgumentException.class,
                () -> Timestamp.of(java.time.LocalDateTime.now().plusDays(1)));
    }

    @Test
    @DisplayName("Should demonstrate end-to-end domain workflow")
    void shouldDemonstrateEndToEndDomainWorkflow() {
        // Given - A guardian submits steps
        GuardianId guardianId = GuardianId.of(42L);
        StepCount dailySteps = StepCount.of(5000);

        // When - Create step record
        StepRecord stepRecord = StepRecord.create(guardianId, dailySteps);

        // Then - Calculate energy earned
        Energy energyEarned = stepRecord.calculateEnergyGenerated();
        assertThat(energyEarned.amount()).isEqualTo(500); // 5000 / 10 = 500

        // When - Create energy transaction
        EnergyTransaction earnedTransaction =
                EnergyTransaction.earned(guardianId, energyEarned, EnergySource.steps());

        // Then - Verify transaction
        assertThat(earnedTransaction.getGuardianId()).isEqualTo(guardianId);
        assertThat(earnedTransaction.getAmount()).isEqualTo(energyEarned);
        assertThat(earnedTransaction.isEarning()).isTrue();

        // When - Spend some energy in battle
        Energy battleCost = Energy.of(100);
        EnergyTransaction spentTransaction =
                EnergyTransaction.spent(guardianId, battleCost, EnergySource.battle());

        // Then - Verify spending transaction
        assertThat(spentTransaction.getAmount()).isEqualTo(battleCost);
        assertThat(spentTransaction.isSpending()).isTrue();

        // Verify net energy calculation
        Energy netEnergy = energyEarned.subtract(battleCost);
        assertThat(netEnergy.amount()).isEqualTo(400); // 500 - 100 = 400
    }

    @Test
    @DisplayName("Should validate business rules across domain objects")
    void shouldValidateBusinessRulesAcrossDomainObjects() {
        // Given
        GuardianId guardianId = GuardianId.of(1L);

        // Test step count limits
        assertDoesNotThrow(() -> StepCount.of(50000)); // Max allowed
        assertThrows(IllegalArgumentException.class, () -> StepCount.of(50001)); // Over limit

        // Test energy calculations
        StepCount maxSteps = StepCount.of(50000);
        StepRecord maxStepRecord = StepRecord.create(guardianId, maxSteps);
        Energy maxEnergy = maxStepRecord.calculateEnergyGenerated();
        assertThat(maxEnergy.amount()).isEqualTo(5000); // 50000 / 10 = 5000

        // Test energy operations
        Energy halfEnergy = Energy.of(2500);
        Energy combinedEnergy = maxEnergy.subtract(halfEnergy);
        assertThat(combinedEnergy.amount()).isEqualTo(2500);

        // Test energy source validation
        assertDoesNotThrow(() -> EnergySource.of("VALID_SOURCE"));
        assertThrows(
                IllegalArgumentException.class, () -> EnergySource.of("A".repeat(51))); // Too long
    }
}

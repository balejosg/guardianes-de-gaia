package com.guardianes.battle.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BattleMoveType Enum Tests")
class BattleMoveTypeTest {

  @DisplayName("Battle Move Type Properties Tests")
  class BattleMoveTypePropertiesTests {

    @Test
    @DisplayName("should have correct properties for ATTACK")
    void shouldHaveCorrectPropertiesForAttack() {
      // Given
      BattleMoveType attack = BattleMoveType.ATTACK;

      // Then
      assertEquals("Attack", attack.getDisplayName());
      assertEquals("Aggressive move that deals damage to opponents", attack.getDescription());
      assertTrue(attack.canTarget());
      assertTrue(attack.canDealDamage());
      assertFalse(attack.canHeal());
      assertEquals(15, attack.getBaseEnergyCost());
    }

    @Test
    @DisplayName("should have correct properties for DEFENSE")
    void shouldHaveCorrectPropertiesForDefense() {
      // Given
      BattleMoveType defense = BattleMoveType.DEFENSE;

      // Then
      assertEquals("Defense", defense.getDisplayName());
      assertEquals("Defensive move that reduces incoming damage", defense.getDescription());
      assertFalse(defense.canTarget());
      assertFalse(defense.canDealDamage());
      assertFalse(defense.canHeal());
      assertEquals(8, defense.getBaseEnergyCost());
    }

    @Test
    @DisplayName("should have correct properties for SUPPORT")
    void shouldHaveCorrectPropertiesForSupport() {
      // Given
      BattleMoveType support = BattleMoveType.SUPPORT;

      // Then
      assertEquals("Support", support.getDisplayName());
      assertEquals("Supportive move that heals or buffs allies", support.getDescription());
      assertTrue(support.canTarget());
      assertFalse(support.canDealDamage());
      assertTrue(support.canHeal());
      assertEquals(12, support.getBaseEnergyCost());
    }

    @Test
    @DisplayName("should have correct properties for SPECIAL")
    void shouldHaveCorrectPropertiesForSpecial() {
      // Given
      BattleMoveType special = BattleMoveType.SPECIAL;

      // Then
      assertEquals("Special", special.getDisplayName());
      assertEquals("Unique move with special effects and abilities", special.getDescription());
      assertTrue(special.canTarget());
      assertTrue(special.canDealDamage());
      assertTrue(special.canHeal());
      assertEquals(25, special.getBaseEnergyCost());
    }
  }

  @DisplayName("Battle Move Type Classification Tests")
  class BattleMoveTypeClassificationTests {

    @Test
    @DisplayName("should correctly identify targeting capabilities")
    void shouldCorrectlyIdentifyTargetingCapabilities() {
      // Then
      assertTrue(BattleMoveType.ATTACK.canTarget());
      assertFalse(BattleMoveType.DEFENSE.canTarget());
      assertTrue(BattleMoveType.SUPPORT.canTarget());
      assertTrue(BattleMoveType.SPECIAL.canTarget());
    }

    @Test
    @DisplayName("should correctly identify damage capabilities")
    void shouldCorrectlyIdentifyDamageCapabilities() {
      // Then
      assertTrue(BattleMoveType.ATTACK.canDealDamage());
      assertFalse(BattleMoveType.DEFENSE.canDealDamage());
      assertFalse(BattleMoveType.SUPPORT.canDealDamage());
      assertTrue(BattleMoveType.SPECIAL.canDealDamage());
    }

    @Test
    @DisplayName("should correctly identify healing capabilities")
    void shouldCorrectlyIdentifyHealingCapabilities() {
      // Then
      assertFalse(BattleMoveType.ATTACK.canHeal());
      assertFalse(BattleMoveType.DEFENSE.canHeal());
      assertTrue(BattleMoveType.SUPPORT.canHeal());
      assertTrue(BattleMoveType.SPECIAL.canHeal());
    }

    @Test
    @DisplayName("should identify offensive move types")
    void shouldIdentifyOffensiveMoveTypes() {
      // Then
      assertTrue(BattleMoveType.ATTACK.isOffensive());
      assertFalse(BattleMoveType.DEFENSE.isOffensive());
      assertFalse(BattleMoveType.SUPPORT.isOffensive());
      assertTrue(BattleMoveType.SPECIAL.isOffensive());
    }

    @Test
    @DisplayName("should identify defensive move types")
    void shouldIdentifyDefensiveMoveTypes() {
      // Then
      assertFalse(BattleMoveType.ATTACK.isDefensive());
      assertTrue(BattleMoveType.DEFENSE.isDefensive());
      assertFalse(BattleMoveType.SUPPORT.isDefensive());
      assertFalse(BattleMoveType.SPECIAL.isDefensive());
    }

    @Test
    @DisplayName("should identify supportive move types")
    void shouldIdentifySupportiveMoveTypes() {
      // Then
      assertFalse(BattleMoveType.ATTACK.isSupportive());
      assertFalse(BattleMoveType.DEFENSE.isSupportive());
      assertTrue(BattleMoveType.SUPPORT.isSupportive());
      assertTrue(BattleMoveType.SPECIAL.isSupportive());
    }
  }

  @DisplayName("Energy Cost Validation Tests")
  class EnergyCostValidationTests {

    @Test
    @DisplayName("should validate energy cost for card compatibility")
    void shouldValidateEnergyCostForCardCompatibility() {
      // Then
      assertTrue(BattleMoveType.ATTACK.isValidEnergyCost(20));
      assertTrue(BattleMoveType.DEFENSE.isValidEnergyCost(10));
      assertTrue(BattleMoveType.SUPPORT.isValidEnergyCost(15));
      assertTrue(BattleMoveType.SPECIAL.isValidEnergyCost(30));
    }

    @Test
    @DisplayName("should reject energy costs below minimum threshold")
    void shouldRejectEnergyCostsBelowMinimumThreshold() {
      // Then
      assertFalse(BattleMoveType.ATTACK.isValidEnergyCost(5));
      assertFalse(BattleMoveType.DEFENSE.isValidEnergyCost(2));
      assertFalse(BattleMoveType.SUPPORT.isValidEnergyCost(3));
      assertFalse(BattleMoveType.SPECIAL.isValidEnergyCost(10));
    }

    @Test
    @DisplayName("should accept energy costs at base cost")
    void shouldAcceptEnergyCostsAtBaseCost() {
      // Then
      assertTrue(BattleMoveType.ATTACK.isValidEnergyCost(15));
      assertTrue(BattleMoveType.DEFENSE.isValidEnergyCost(8));
      assertTrue(BattleMoveType.SUPPORT.isValidEnergyCost(12));
      assertTrue(BattleMoveType.SPECIAL.isValidEnergyCost(25));
    }

    @Test
    @DisplayName("should reject excessive energy costs")
    void shouldRejectExcessiveEnergyCosts() {
      // Then
      assertFalse(BattleMoveType.ATTACK.isValidEnergyCost(100));
      assertFalse(BattleMoveType.DEFENSE.isValidEnergyCost(80));
      assertFalse(BattleMoveType.SUPPORT.isValidEnergyCost(90));
      assertFalse(BattleMoveType.SPECIAL.isValidEnergyCost(150));
    }
  }

  @DisplayName("Base Energy Cost Validation Tests")
  class BaseEnergyCostValidationTests {

    @Test
    @DisplayName("should have increasing energy costs by complexity")
    void shouldHaveIncreasingEnergyCostsByComplexity() {
      // Then
      assertTrue(
          BattleMoveType.DEFENSE.getBaseEnergyCost() < BattleMoveType.SUPPORT.getBaseEnergyCost());
      assertTrue(
          BattleMoveType.SUPPORT.getBaseEnergyCost() < BattleMoveType.ATTACK.getBaseEnergyCost());
      assertTrue(
          BattleMoveType.ATTACK.getBaseEnergyCost() < BattleMoveType.SPECIAL.getBaseEnergyCost());
    }

    @Test
    @DisplayName("should have all positive base energy costs")
    void shouldHaveAllPositiveBaseEnergyCosts() {
      // Then
      assertTrue(BattleMoveType.ATTACK.getBaseEnergyCost() > 0);
      assertTrue(BattleMoveType.DEFENSE.getBaseEnergyCost() > 0);
      assertTrue(BattleMoveType.SUPPORT.getBaseEnergyCost() > 0);
      assertTrue(BattleMoveType.SPECIAL.getBaseEnergyCost() > 0);
    }

    @Test
    @DisplayName("should have reasonable base energy cost ranges")
    void shouldHaveReasonableBaseEnergyCostRanges() {
      // Then
      assertTrue(BattleMoveType.DEFENSE.getBaseEnergyCost() <= 10);
      assertTrue(BattleMoveType.SUPPORT.getBaseEnergyCost() <= 15);
      assertTrue(BattleMoveType.ATTACK.getBaseEnergyCost() <= 20);
      assertTrue(BattleMoveType.SPECIAL.getBaseEnergyCost() <= 30);
    }
  }

  @DisplayName("Battle Move Type String Representation Tests")
  class BattleMoveTypeStringRepresentationTests {

    @Test
    @DisplayName("should return display name for toString")
    void shouldReturnDisplayNameForToString() {
      // Given
      BattleMoveType attack = BattleMoveType.ATTACK;

      // Then
      assertEquals("Attack", attack.toString());
      assertEquals(attack.getDisplayName(), attack.toString());
    }

    @Test
    @DisplayName("should have non-null display names")
    void shouldHaveNonNullDisplayNames() {
      // Then
      assertNotNull(BattleMoveType.ATTACK.getDisplayName());
      assertNotNull(BattleMoveType.DEFENSE.getDisplayName());
      assertNotNull(BattleMoveType.SUPPORT.getDisplayName());
      assertNotNull(BattleMoveType.SPECIAL.getDisplayName());

      assertFalse(BattleMoveType.ATTACK.getDisplayName().trim().isEmpty());
      assertFalse(BattleMoveType.DEFENSE.getDisplayName().trim().isEmpty());
      assertFalse(BattleMoveType.SUPPORT.getDisplayName().trim().isEmpty());
      assertFalse(BattleMoveType.SPECIAL.getDisplayName().trim().isEmpty());
    }

    @Test
    @DisplayName("should have non-null descriptions")
    void shouldHaveNonNullDescriptions() {
      // Then
      assertNotNull(BattleMoveType.ATTACK.getDescription());
      assertNotNull(BattleMoveType.DEFENSE.getDescription());
      assertNotNull(BattleMoveType.SUPPORT.getDescription());
      assertNotNull(BattleMoveType.SPECIAL.getDescription());

      assertFalse(BattleMoveType.ATTACK.getDescription().trim().isEmpty());
      assertFalse(BattleMoveType.DEFENSE.getDescription().trim().isEmpty());
      assertFalse(BattleMoveType.SUPPORT.getDescription().trim().isEmpty());
      assertFalse(BattleMoveType.SPECIAL.getDescription().trim().isEmpty());
    }
  }

  @DisplayName("Battle Move Type Enum Coverage Tests")
  class BattleMoveTypeEnumCoverageTests {

    @Test
    @DisplayName("should have exactly four battle move types defined")
    void shouldHaveExactlyFourBattleMoveTypesDefined() {
      // When
      BattleMoveType[] battleMoveTypes = BattleMoveType.values();

      // Then
      assertEquals(4, battleMoveTypes.length);
      assertEquals(BattleMoveType.ATTACK, battleMoveTypes[0]);
      assertEquals(BattleMoveType.DEFENSE, battleMoveTypes[1]);
      assertEquals(BattleMoveType.SUPPORT, battleMoveTypes[2]);
      assertEquals(BattleMoveType.SPECIAL, battleMoveTypes[3]);
    }

    @Test
    @DisplayName("should be able to convert from string")
    void shouldBeAbleToConvertFromString() {
      // When & Then
      assertEquals(BattleMoveType.ATTACK, BattleMoveType.valueOf("ATTACK"));
      assertEquals(BattleMoveType.DEFENSE, BattleMoveType.valueOf("DEFENSE"));
      assertEquals(BattleMoveType.SUPPORT, BattleMoveType.valueOf("SUPPORT"));
      assertEquals(BattleMoveType.SPECIAL, BattleMoveType.valueOf("SPECIAL"));
    }

    @Test
    @DisplayName("should throw exception for invalid enum value")
    void shouldThrowExceptionForInvalidEnumValue() {
      // When & Then
      assertThrows(IllegalArgumentException.class, () -> BattleMoveType.valueOf("INVALID_TYPE"));
    }
  }

  @DisplayName("Battle Move Type Compatibility Tests")
  class BattleMoveTypeCompatibilityTests {

    @Test
    @DisplayName("should determine compatibility with battle situations")
    void shouldDetermineCompatibilityWithBattleSituations() {
      // Then - All move types are compatible with PvP battles
      assertTrue(BattleMoveType.ATTACK.isCompatibleWithPvP());
      assertTrue(BattleMoveType.DEFENSE.isCompatibleWithPvP());
      assertTrue(BattleMoveType.SUPPORT.isCompatibleWithPvP());
      assertTrue(BattleMoveType.SPECIAL.isCompatibleWithPvP());
    }

    @Test
    @DisplayName("should determine compatibility with solo battles")
    void shouldDetermineCompatibilityWithSoloBattles() {
      // Then - Attack and Special are primarily for solo battles
      assertTrue(BattleMoveType.ATTACK.isCompatibleWithSolo());
      assertTrue(BattleMoveType.DEFENSE.isCompatibleWithSolo());
      assertFalse(BattleMoveType.SUPPORT.isCompatibleWithSolo()); // Support less useful in solo
      assertTrue(BattleMoveType.SPECIAL.isCompatibleWithSolo());
    }

    @Test
    @DisplayName("should determine compatibility with cooperative battles")
    void shouldDetermineCompatibilityWithCooperativeBattles() {
      // Then - Support and Special are excellent for cooperative battles
      assertTrue(BattleMoveType.ATTACK.isCompatibleWithCooperative());
      assertTrue(BattleMoveType.DEFENSE.isCompatibleWithCooperative());
      assertTrue(BattleMoveType.SUPPORT.isCompatibleWithCooperative());
      assertTrue(BattleMoveType.SPECIAL.isCompatibleWithCooperative());
    }
  }

  @DisplayName("Battle Move Type Strategic Value Tests")
  class BattleMoveTypeStrategicValueTests {

    @Test
    @DisplayName("should calculate strategic value for different battle contexts")
    void shouldCalculateStrategicValueForDifferentBattleContexts() {
      // Then - Strategic values should reflect move type effectiveness
      assertEquals(8, BattleMoveType.ATTACK.getStrategicValue()); // High damage potential
      assertEquals(6, BattleMoveType.DEFENSE.getStrategicValue()); // Moderate strategic value
      assertEquals(7, BattleMoveType.SUPPORT.getStrategicValue()); // Good team value
      assertEquals(9, BattleMoveType.SPECIAL.getStrategicValue()); // Highest versatility
    }

    @Test
    @DisplayName("should have strategic values within expected range")
    void shouldHaveStrategicValuesWithinExpectedRange() {
      // Then - All strategic values should be between 1-10
      assertTrue(
          BattleMoveType.ATTACK.getStrategicValue() >= 1
              && BattleMoveType.ATTACK.getStrategicValue() <= 10);
      assertTrue(
          BattleMoveType.DEFENSE.getStrategicValue() >= 1
              && BattleMoveType.DEFENSE.getStrategicValue() <= 10);
      assertTrue(
          BattleMoveType.SUPPORT.getStrategicValue() >= 1
              && BattleMoveType.SUPPORT.getStrategicValue() <= 10);
      assertTrue(
          BattleMoveType.SPECIAL.getStrategicValue() >= 1
              && BattleMoveType.SPECIAL.getStrategicValue() <= 10);
    }
  }
}

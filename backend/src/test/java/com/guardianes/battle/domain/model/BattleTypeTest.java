package com.guardianes.battle.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BattleType Enum Tests")
class BattleTypeTest {

  @DisplayName("Battle Type Properties Tests")
  class BattleTypePropertiesTests {

    @Test
    @DisplayName("should have correct properties for SOLO_CHALLENGE")
    void shouldHaveCorrectPropertiesForSoloChallenge() {
      // Given
      BattleType soloChallenge = BattleType.SOLO_CHALLENGE;

      // Then
      assertEquals("Solo Challenge", soloChallenge.getDisplayName());
      assertEquals(1, soloChallenge.getRequiredParticipants());
      assertEquals(50, soloChallenge.getBaseXpReward());
      assertTrue(soloChallenge.isSolo());
      assertFalse(soloChallenge.isMultiplayer());
      assertEquals(10, soloChallenge.getMinimumEnergyCost());
      assertEquals(15, soloChallenge.getMaxDurationMinutes());
    }

    @Test
    @DisplayName("should have correct properties for PVP_DUEL")
    void shouldHaveCorrectPropertiesForPvpDuel() {
      // Given
      BattleType pvpDuel = BattleType.PVP_DUEL;

      // Then
      assertEquals("PvP Duel", pvpDuel.getDisplayName());
      assertEquals(2, pvpDuel.getRequiredParticipants());
      assertEquals(100, pvpDuel.getBaseXpReward());
      assertFalse(pvpDuel.isSolo());
      assertTrue(pvpDuel.isMultiplayer());
      assertEquals(15, pvpDuel.getMinimumEnergyCost());
      assertEquals(30, pvpDuel.getMaxDurationMinutes());
    }

    @Test
    @DisplayName("should have correct properties for COOPERATIVE_BATTLE")
    void shouldHaveCorrectPropertiesForCooperativeBattle() {
      // Given
      BattleType cooperativeBattle = BattleType.COOPERATIVE_BATTLE;

      // Then
      assertEquals("Cooperative Battle", cooperativeBattle.getDisplayName());
      assertEquals(2, cooperativeBattle.getRequiredParticipants());
      assertEquals(150, cooperativeBattle.getBaseXpReward());
      assertFalse(cooperativeBattle.isSolo());
      assertTrue(cooperativeBattle.isMultiplayer());
      assertEquals(20, cooperativeBattle.getMinimumEnergyCost());
      assertEquals(45, cooperativeBattle.getMaxDurationMinutes());
    }
  }

  @DisplayName("XP Reward Calculation Tests")
  class XpRewardCalculationTests {

    @Test
    @DisplayName("should calculate XP reward correctly for winner in PvP")
    void shouldCalculateXpRewardCorrectlyForWinnerInPvp() {
      // Given
      BattleType pvpDuel = BattleType.PVP_DUEL;
      int energySpent = 30;
      boolean isWinner = true;
      double performanceMultiplier = 1.2;

      // When
      int xpReward = pvpDuel.calculateXpReward(energySpent, isWinner, performanceMultiplier);

      // Then
      // Expected: (100 + 30) * 1.5 (winner bonus) * 1.2 (performance) = 234
      assertEquals(234, xpReward);
    }

    @Test
    @DisplayName("should calculate XP reward correctly for loser in PvP")
    void shouldCalculateXpRewardCorrectlyForLoserInPvp() {
      // Given
      BattleType pvpDuel = BattleType.PVP_DUEL;
      int energySpent = 30;
      boolean isWinner = false;
      double performanceMultiplier = 1.0;

      // When
      int xpReward = pvpDuel.calculateXpReward(energySpent, isWinner, performanceMultiplier);

      // Then
      // Expected: (100 + 30) * 1.0 (no winner bonus) * 1.0 (performance) = 130
      assertEquals(130, xpReward);
    }

    @Test
    @DisplayName("should not apply winner bonus for cooperative battles")
    void shouldNotApplyWinnerBonusForCooperativeBattles() {
      // Given
      BattleType cooperativeBattle = BattleType.COOPERATIVE_BATTLE;
      int energySpent = 40;
      boolean isWinner = true; // Should be ignored for cooperative battles
      double performanceMultiplier = 1.0;

      // When
      int xpReward =
          cooperativeBattle.calculateXpReward(energySpent, isWinner, performanceMultiplier);

      // Then
      // Expected: (150 + 40) * 1.0 (no winner bonus for cooperative) * 1.0 = 190
      assertEquals(190, xpReward);
    }

    @Test
    @DisplayName("should apply performance multiplier correctly")
    void shouldApplyPerformanceMultiplierCorrectly() {
      // Given
      BattleType soloChallenge = BattleType.SOLO_CHALLENGE;
      int energySpent = 20;
      boolean isWinner = false;
      double highPerformanceMultiplier = 1.8;

      // When
      int xpReward =
          soloChallenge.calculateXpReward(energySpent, isWinner, highPerformanceMultiplier);

      // Then
      // Expected: (50 + 20) * 1.0 (no winner bonus) * 1.8 (performance) = 126
      assertEquals(126, xpReward);
    }

    @Test
    @DisplayName("should cap performance multiplier at maximum")
    void shouldCapPerformanceMultiplierAtMaximum() {
      // Given
      BattleType soloChallenge = BattleType.SOLO_CHALLENGE;
      int energySpent = 10;
      boolean isWinner = false;
      double excessivePerformanceMultiplier = 3.0; // Should be capped at 2.0

      // When
      int xpReward =
          soloChallenge.calculateXpReward(energySpent, isWinner, excessivePerformanceMultiplier);

      // Then
      // Expected: (50 + 10) * 1.0 * 2.0 (capped) = 120
      assertEquals(120, xpReward);
    }

    @Test
    @DisplayName("should cap performance multiplier at minimum")
    void shouldCapPerformanceMultiplierAtMinimum() {
      // Given
      BattleType soloChallenge = BattleType.SOLO_CHALLENGE;
      int energySpent = 10;
      boolean isWinner = false;
      double poorPerformanceMultiplier = 0.2; // Should be capped at 0.5

      // When
      int xpReward =
          soloChallenge.calculateXpReward(energySpent, isWinner, poorPerformanceMultiplier);

      // Then
      // Expected: (50 + 10) * 1.0 * 0.5 (capped) = 30
      assertEquals(30, xpReward);
    }

    @Test
    @DisplayName("should handle zero energy spent")
    void shouldHandleZeroEnergySpent() {
      // Given
      BattleType pvpDuel = BattleType.PVP_DUEL;
      int energySpent = 0;
      boolean isWinner = true;
      double performanceMultiplier = 1.0;

      // When
      int xpReward = pvpDuel.calculateXpReward(energySpent, isWinner, performanceMultiplier);

      // Then
      // Expected: (100 + 0) * 1.5 (winner bonus) * 1.0 = 150
      assertEquals(150, xpReward);
    }
  }

  @DisplayName("Battle Type Classification Tests")
  class BattleTypeClassificationTests {

    @Test
    @DisplayName("should correctly identify solo battle types")
    void shouldCorrectlyIdentifySoloBattleTypes() {
      // Given
      BattleType soloChallenge = BattleType.SOLO_CHALLENGE;

      // Then
      assertTrue(soloChallenge.isSolo());
      assertFalse(soloChallenge.isMultiplayer());
      assertEquals(1, soloChallenge.getRequiredParticipants());
    }

    @Test
    @DisplayName("should correctly identify multiplayer battle types")
    void shouldCorrectlyIdentifyMultiplayerBattleTypes() {
      // Given
      BattleType pvpDuel = BattleType.PVP_DUEL;
      BattleType cooperativeBattle = BattleType.COOPERATIVE_BATTLE;

      // Then
      assertFalse(pvpDuel.isSolo());
      assertTrue(pvpDuel.isMultiplayer());
      assertEquals(2, pvpDuel.getRequiredParticipants());

      assertFalse(cooperativeBattle.isSolo());
      assertTrue(cooperativeBattle.isMultiplayer());
      assertEquals(2, cooperativeBattle.getRequiredParticipants());
    }
  }

  @DisplayName("Battle Type Constraints Tests")
  class BattleTypeConstraintsTests {

    @Test
    @DisplayName("should have appropriate minimum energy costs")
    void shouldHaveAppropriateMinimumEnergyCosts() {
      // Then
      assertTrue(
          BattleType.SOLO_CHALLENGE.getMinimumEnergyCost()
              <= BattleType.PVP_DUEL.getMinimumEnergyCost());
      assertTrue(
          BattleType.PVP_DUEL.getMinimumEnergyCost()
              <= BattleType.COOPERATIVE_BATTLE.getMinimumEnergyCost());

      // All costs should be positive
      assertTrue(BattleType.SOLO_CHALLENGE.getMinimumEnergyCost() > 0);
      assertTrue(BattleType.PVP_DUEL.getMinimumEnergyCost() > 0);
      assertTrue(BattleType.COOPERATIVE_BATTLE.getMinimumEnergyCost() > 0);
    }

    @Test
    @DisplayName("should have appropriate maximum durations")
    void shouldHaveAppropriateMaximumDurations() {
      // Then
      assertTrue(
          BattleType.SOLO_CHALLENGE.getMaxDurationMinutes()
              <= BattleType.PVP_DUEL.getMaxDurationMinutes());
      assertTrue(
          BattleType.PVP_DUEL.getMaxDurationMinutes()
              <= BattleType.COOPERATIVE_BATTLE.getMaxDurationMinutes());

      // All durations should be positive
      assertTrue(BattleType.SOLO_CHALLENGE.getMaxDurationMinutes() > 0);
      assertTrue(BattleType.PVP_DUEL.getMaxDurationMinutes() > 0);
      assertTrue(BattleType.COOPERATIVE_BATTLE.getMaxDurationMinutes() > 0);
    }

    @Test
    @DisplayName("should have appropriate base XP rewards")
    void shouldHaveAppropriateBaseXpRewards() {
      // Then
      assertTrue(
          BattleType.SOLO_CHALLENGE.getBaseXpReward() <= BattleType.PVP_DUEL.getBaseXpReward());
      assertTrue(
          BattleType.PVP_DUEL.getBaseXpReward() <= BattleType.COOPERATIVE_BATTLE.getBaseXpReward());

      // All rewards should be positive
      assertTrue(BattleType.SOLO_CHALLENGE.getBaseXpReward() > 0);
      assertTrue(BattleType.PVP_DUEL.getBaseXpReward() > 0);
      assertTrue(BattleType.COOPERATIVE_BATTLE.getBaseXpReward() > 0);
    }
  }

  @DisplayName("Battle Type String Representation Tests")
  class BattleTypeStringRepresentationTests {

    @Test
    @DisplayName("should return display name for toString")
    void shouldReturnDisplayNameForToString() {
      // Given
      BattleType pvpDuel = BattleType.PVP_DUEL;

      // Then
      assertEquals("PvP Duel", pvpDuel.toString());
      assertEquals(pvpDuel.getDisplayName(), pvpDuel.toString());
    }

    @Test
    @DisplayName("should have non-null display names")
    void shouldHaveNonNullDisplayNames() {
      // Then
      assertNotNull(BattleType.SOLO_CHALLENGE.getDisplayName());
      assertNotNull(BattleType.PVP_DUEL.getDisplayName());
      assertNotNull(BattleType.COOPERATIVE_BATTLE.getDisplayName());

      assertFalse(BattleType.SOLO_CHALLENGE.getDisplayName().trim().isEmpty());
      assertFalse(BattleType.PVP_DUEL.getDisplayName().trim().isEmpty());
      assertFalse(BattleType.COOPERATIVE_BATTLE.getDisplayName().trim().isEmpty());
    }
  }

  @DisplayName("Battle Type Enum Coverage Tests")
  class BattleTypeEnumCoverageTests {

    @Test
    @DisplayName("should have exactly three battle types defined")
    void shouldHaveExactlyThreeBattleTypesDefined() {
      // When
      BattleType[] battleTypes = BattleType.values();

      // Then
      assertEquals(3, battleTypes.length);
      assertEquals(BattleType.SOLO_CHALLENGE, battleTypes[0]);
      assertEquals(BattleType.PVP_DUEL, battleTypes[1]);
      assertEquals(BattleType.COOPERATIVE_BATTLE, battleTypes[2]);
    }

    @Test
    @DisplayName("should be able to convert from string")
    void shouldBeAbleToConvertFromString() {
      // When & Then
      assertEquals(BattleType.SOLO_CHALLENGE, BattleType.valueOf("SOLO_CHALLENGE"));
      assertEquals(BattleType.PVP_DUEL, BattleType.valueOf("PVP_DUEL"));
      assertEquals(BattleType.COOPERATIVE_BATTLE, BattleType.valueOf("COOPERATIVE_BATTLE"));
    }

    @Test
    @DisplayName("should throw exception for invalid enum value")
    void shouldThrowExceptionForInvalidEnumValue() {
      // When & Then
      assertThrows(IllegalArgumentException.class, () -> BattleType.valueOf("INVALID_TYPE"));
    }
  }
}

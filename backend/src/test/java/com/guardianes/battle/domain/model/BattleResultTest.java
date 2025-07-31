package com.guardianes.battle.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import com.guardianes.battle.domain.service.BattleEngine.XpReward;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BattleResult Domain Model Tests")
class BattleResultTest {

  private Long battleId;
  private Long winnerId;
  private LocalDateTime startedAt;
  private LocalDateTime completedAt;
  private List<XpReward> xpRewards;

  @BeforeEach
  void setUp() {
    battleId = 1L;
    winnerId = 2L;
    startedAt = LocalDateTime.now().minusMinutes(30);
    completedAt = LocalDateTime.now();
    xpRewards =
        Arrays.asList(
            new XpReward(2L, 150), // Winner
            new XpReward(3L, 100)); // Participant
  }

  @DisplayName("BattleResult Creation Tests")
  class BattleResultCreationTests {

    @Test
    @DisplayName("should create battle result successfully")
    void shouldCreateBattleResultSuccessfully() {
      // When
      BattleResult result =
          new BattleResult(battleId, winnerId, 10, 75, startedAt, completedAt, xpRewards);

      // Then
      assertNotNull(result);
      assertEquals(battleId, result.battleId());
      assertEquals(winnerId, result.winnerId());
      assertEquals(10, result.totalMoves());
      assertEquals(75, result.totalEnergySpent());
      assertEquals(startedAt, result.startedAt());
      assertEquals(completedAt, result.completedAt());
      assertEquals(xpRewards, result.xpRewards());
    }

    @Test
    @DisplayName("should create battle result with no winner")
    void shouldCreateBattleResultWithNoWinner() {
      // When
      BattleResult result =
          new BattleResult(battleId, null, 5, 30, startedAt, completedAt, xpRewards);

      // Then
      assertNotNull(result);
      assertNull(result.winnerId());
      assertEquals(battleId, result.battleId());
    }

    @Test
    @DisplayName("should create battle result with empty XP rewards")
    void shouldCreateBattleResultWithEmptyXpRewards() {
      // Given
      List<XpReward> emptyRewards = Collections.emptyList();

      // When
      BattleResult result =
          new BattleResult(battleId, winnerId, 3, 20, startedAt, completedAt, emptyRewards);

      // Then
      assertNotNull(result);
      assertTrue(result.xpRewards().isEmpty());
    }

    @Test
    @DisplayName("should throw exception when battle ID is null")
    void shouldThrowExceptionWhenBattleIdIsNull() {
      // When & Then
      assertThrows(
          NullPointerException.class,
          () -> new BattleResult(null, winnerId, 10, 75, startedAt, completedAt, xpRewards),
          "Battle ID cannot be null");
    }

    @Test
    @DisplayName("should throw exception when started at is null")
    void shouldThrowExceptionWhenStartedAtIsNull() {
      // When & Then
      assertThrows(
          NullPointerException.class,
          () -> new BattleResult(battleId, winnerId, 10, 75, null, completedAt, xpRewards),
          "Started at cannot be null");
    }

    @Test
    @DisplayName("should throw exception when completed at is null")
    void shouldThrowExceptionWhenCompletedAtIsNull() {
      // When & Then
      assertThrows(
          NullPointerException.class,
          () -> new BattleResult(battleId, winnerId, 10, 75, startedAt, null, xpRewards),
          "Completed at cannot be null");
    }

    @Test
    @DisplayName("should throw exception when XP rewards is null")
    void shouldThrowExceptionWhenXpRewardsIsNull() {
      // When & Then
      assertThrows(
          NullPointerException.class,
          () -> new BattleResult(battleId, winnerId, 10, 75, startedAt, completedAt, null),
          "XP rewards cannot be null");
    }
  }

  @DisplayName("BattleResult Validation Tests")
  class BattleResultValidationTests {

    @Test
    @DisplayName("should throw exception when total moves is negative")
    void shouldThrowExceptionWhenTotalMovesIsNegative() {
      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () -> new BattleResult(battleId, winnerId, -5, 75, startedAt, completedAt, xpRewards),
          "Total moves cannot be negative");
    }

    @Test
    @DisplayName("should throw exception when total energy spent is negative")
    void shouldThrowExceptionWhenTotalEnergySpentIsNegative() {
      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () -> new BattleResult(battleId, winnerId, 10, -20, startedAt, completedAt, xpRewards),
          "Total energy spent cannot be negative");
    }

    @Test
    @DisplayName("should throw exception when completed at is before started at")
    void shouldThrowExceptionWhenCompletedAtIsBeforeStartedAt() {
      // Given
      LocalDateTime invalidCompletedAt = startedAt.minusMinutes(10);

      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () ->
              new BattleResult(
                  battleId, winnerId, 10, 75, startedAt, invalidCompletedAt, xpRewards),
          "Completed at cannot be before started at");
    }

    @Test
    @DisplayName("should allow zero values for moves and energy")
    void shouldAllowZeroValuesForMovesAndEnergy() {
      // When
      BattleResult result =
          new BattleResult(battleId, winnerId, 0, 0, startedAt, completedAt, xpRewards);

      // Then
      assertNotNull(result);
      assertEquals(0, result.totalMoves());
      assertEquals(0, result.totalEnergySpent());
    }

    @Test
    @DisplayName("should allow same started and completed time")
    void shouldAllowSameStartedAndCompletedTime() {
      // Given
      LocalDateTime sameTime = LocalDateTime.now();

      // When
      BattleResult result =
          new BattleResult(battleId, winnerId, 1, 10, sameTime, sameTime, xpRewards);

      // Then
      assertNotNull(result);
      assertEquals(sameTime, result.startedAt());
      assertEquals(sameTime, result.completedAt());
    }
  }

  @DisplayName("BattleResult Business Logic Tests")
  class BattleResultBusinessLogicTests {

    private BattleResult battleResult;

    @BeforeEach
    void setUp() {
      battleResult =
          new BattleResult(battleId, winnerId, 12, 85, startedAt, completedAt, xpRewards);
    }

    @Test
    @DisplayName("should calculate battle duration correctly")
    void shouldCalculateBattleDurationCorrectly() {
      // When
      long durationMinutes = battleResult.getBattleDurationMinutes();

      // Then
      assertEquals(30, durationMinutes); // completedAt - startedAt = 30 minutes
    }

    @Test
    @DisplayName("should calculate average energy per move")
    void shouldCalculateAverageEnergyPerMove() {
      // When
      double averageEnergy = battleResult.getAverageEnergyPerMove();

      // Then
      assertEquals(7.08, averageEnergy, 0.01); // 85 energy / 12 moves = 7.083...
    }

    @Test
    @DisplayName("should handle zero moves for average energy calculation")
    void shouldHandleZeroMovesForAverageEnergyCalculation() {
      // Given
      BattleResult zeroMovesResult =
          new BattleResult(battleId, winnerId, 0, 50, startedAt, completedAt, xpRewards);

      // When
      double averageEnergy = zeroMovesResult.getAverageEnergyPerMove();

      // Then
      assertEquals(0.0, averageEnergy, 0.01);
    }

    @Test
    @DisplayName("should calculate total XP awarded")
    void shouldCalculateTotalXpAwarded() {
      // When
      int totalXp = battleResult.getTotalXpAwarded();

      // Then
      assertEquals(250, totalXp); // 150 + 100 = 250
    }

    @Test
    @DisplayName("should calculate total XP awarded with empty rewards")
    void shouldCalculateTotalXpAwardedWithEmptyRewards() {
      // Given
      BattleResult noRewardsResult =
          new BattleResult(
              battleId, winnerId, 5, 30, startedAt, completedAt, Collections.emptyList());

      // When
      int totalXp = noRewardsResult.getTotalXpAwarded();

      // Then
      assertEquals(0, totalXp);
    }

    @Test
    @DisplayName("should identify if battle had winner")
    void shouldIdentifyIfBattleHadWinner() {
      // Given
      BattleResult withWinner =
          new BattleResult(battleId, winnerId, 10, 75, startedAt, completedAt, xpRewards);
      BattleResult withoutWinner =
          new BattleResult(battleId, null, 10, 75, startedAt, completedAt, xpRewards);

      // Then
      assertTrue(withWinner.hasWinner());
      assertFalse(withoutWinner.hasWinner());
    }

    @Test
    @DisplayName("should get winner XP reward")
    void shouldGetWinnerXpReward() {
      // When
      XpReward winnerReward = battleResult.getWinnerXpReward();

      // Then
      assertNotNull(winnerReward);
      assertEquals(winnerId, winnerReward.guardianId());
      assertEquals(150, winnerReward.amount());
    }

    @Test
    @DisplayName("should return null for winner XP when no winner")
    void shouldReturnNullForWinnerXpWhenNoWinner() {
      // Given
      BattleResult noWinnerResult =
          new BattleResult(battleId, null, 10, 75, startedAt, completedAt, xpRewards);

      // When
      XpReward winnerReward = noWinnerResult.getWinnerXpReward();

      // Then
      assertNull(winnerReward);
    }

    @Test
    @DisplayName("should get participant count")
    void shouldGetParticipantCount() {
      // When
      int participantCount = battleResult.getParticipantCount();

      // Then
      assertEquals(2, participantCount);
    }

    @Test
    @DisplayName("should determine if battle was efficient")
    void shouldDetermineIfBattleWasEfficient() {
      // Given - Battle with high energy efficiency (moves < energy/5)
      BattleResult efficientBattle =
          new BattleResult(battleId, winnerId, 5, 75, startedAt, completedAt, xpRewards);
      BattleResult inefficientBattle =
          new BattleResult(battleId, winnerId, 20, 75, startedAt, completedAt, xpRewards);

      // Then
      assertTrue(efficientBattle.wasEfficient());
      assertFalse(inefficientBattle.wasEfficient());
    }

    @Test
    @DisplayName("should determine if battle was quick")
    void shouldDetermineIfBattleWasQuick() {
      // Given - Quick battle (< 15 minutes)
      LocalDateTime quickStartTime = completedAt.minusMinutes(10);
      LocalDateTime slowStartTime = completedAt.minusMinutes(25);

      BattleResult quickBattle =
          new BattleResult(battleId, winnerId, 10, 75, quickStartTime, completedAt, xpRewards);
      BattleResult slowBattle =
          new BattleResult(battleId, winnerId, 10, 75, slowStartTime, completedAt, xpRewards);

      // Then
      assertTrue(quickBattle.wasQuick());
      assertFalse(slowBattle.wasQuick());
    }
  }

  @DisplayName("BattleResult XP Reward Analysis Tests")
  class BattleResultXpRewardAnalysisTests {

    private BattleResult battleResult;

    @BeforeEach
    void setUp() {
      List<XpReward> multipleRewards =
          Arrays.asList(
              new XpReward(1L, 200), // High performer
              new XpReward(2L, 150), // Winner
              new XpReward(3L, 100), // Regular participant
              new XpReward(4L, 80)); // Lower performer
      battleResult =
          new BattleResult(battleId, 2L, 15, 90, startedAt, completedAt, multipleRewards);
    }

    @Test
    @DisplayName("should get highest XP reward")
    void shouldGetHighestXpReward() {
      // When
      XpReward highestReward = battleResult.getHighestXpReward();

      // Then
      assertNotNull(highestReward);
      assertEquals(1L, highestReward.guardianId());
      assertEquals(200, highestReward.amount());
    }

    @Test
    @DisplayName("should get lowest XP reward")
    void shouldGetLowestXpReward() {
      // When
      XpReward lowestReward = battleResult.getLowestXpReward();

      // Then
      assertNotNull(lowestReward);
      assertEquals(4L, lowestReward.guardianId());
      assertEquals(80, lowestReward.amount());
    }

    @Test
    @DisplayName("should calculate average XP reward")
    void shouldCalculateAverageXpReward() {
      // When
      double averageXp = battleResult.getAverageXpReward();

      // Then
      assertEquals(132.5, averageXp, 0.01); // (200 + 150 + 100 + 80) / 4 = 132.5
    }

    @Test
    @DisplayName("should get XP reward for specific guardian")
    void shouldGetXpRewardForSpecificGuardian() {
      // When
      XpReward specificReward = battleResult.getXpRewardForGuardian(3L);

      // Then
      assertNotNull(specificReward);
      assertEquals(3L, specificReward.guardianId());
      assertEquals(100, specificReward.amount());
    }

    @Test
    @DisplayName("should return null for non-existent guardian")
    void shouldReturnNullForNonExistentGuardian() {
      // When
      XpReward nonExistentReward = battleResult.getXpRewardForGuardian(999L);

      // Then
      assertNull(nonExistentReward);
    }
  }

  @DisplayName("BattleResult Equality Tests")
  class BattleResultEqualityTests {

    @Test
    @DisplayName("should be equal when all fields match")
    void shouldBeEqualWhenAllFieldsMatch() {
      // Given
      BattleResult result1 =
          new BattleResult(battleId, winnerId, 10, 75, startedAt, completedAt, xpRewards);
      BattleResult result2 =
          new BattleResult(battleId, winnerId, 10, 75, startedAt, completedAt, xpRewards);

      // Then
      assertEquals(result1, result2);
      assertEquals(result1.hashCode(), result2.hashCode());
    }

    @Test
    @DisplayName("should not be equal when battle IDs differ")
    void shouldNotBeEqualWhenBattleIdsDiffer() {
      // Given
      BattleResult result1 =
          new BattleResult(1L, winnerId, 10, 75, startedAt, completedAt, xpRewards);
      BattleResult result2 =
          new BattleResult(2L, winnerId, 10, 75, startedAt, completedAt, xpRewards);

      // Then
      assertNotEquals(result1, result2);
    }

    @Test
    @DisplayName("should not be equal when winner IDs differ")
    void shouldNotBeEqualWhenWinnerIdsDiffer() {
      // Given
      BattleResult result1 =
          new BattleResult(battleId, 1L, 10, 75, startedAt, completedAt, xpRewards);
      BattleResult result2 =
          new BattleResult(battleId, 2L, 10, 75, startedAt, completedAt, xpRewards);

      // Then
      assertNotEquals(result1, result2);
    }

    @Test
    @DisplayName("should handle null winner IDs in equality")
    void shouldHandleNullWinnerIdsInEquality() {
      // Given
      BattleResult result1 =
          new BattleResult(battleId, null, 10, 75, startedAt, completedAt, xpRewards);
      BattleResult result2 =
          new BattleResult(battleId, null, 10, 75, startedAt, completedAt, xpRewards);
      BattleResult result3 =
          new BattleResult(battleId, winnerId, 10, 75, startedAt, completedAt, xpRewards);

      // Then
      assertEquals(result1, result2);
      assertNotEquals(result1, result3);
    }
  }

  @DisplayName("BattleResult String Representation Tests")
  class BattleResultStringRepresentationTests {

    @Test
    @DisplayName("should have meaningful toString representation")
    void shouldHaveMeaningfulToStringRepresentation() {
      // Given
      BattleResult result =
          new BattleResult(battleId, winnerId, 10, 75, startedAt, completedAt, xpRewards);

      // When
      String resultString = result.toString();

      // Then
      assertNotNull(resultString);
      assertTrue(resultString.contains("BattleResult"));
      assertTrue(resultString.contains("battleId=" + battleId));
      assertTrue(resultString.contains("winnerId=" + winnerId));
      assertTrue(resultString.contains("totalMoves=10"));
      assertTrue(resultString.contains("totalEnergySpent=75"));
      assertTrue(resultString.contains("xpRewards=" + xpRewards.size()));
    }

    @Test
    @DisplayName("should handle null winner in toString")
    void shouldHandleNullWinnerInToString() {
      // Given
      BattleResult result =
          new BattleResult(battleId, null, 10, 75, startedAt, completedAt, xpRewards);

      // When
      String resultString = result.toString();

      // Then
      assertNotNull(resultString);
      assertTrue(resultString.contains("winnerId=null"));
    }
  }
}

package com.guardianes.battle.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.guardianes.battle.domain.model.*;
import com.guardianes.battle.domain.service.BattleEngine.InsufficientEnergyException;
import com.guardianes.cards.domain.model.Card;
import com.guardianes.cards.domain.model.CardElement;
import com.guardianes.cards.domain.model.CardRarity;
import com.guardianes.guardian.domain.model.Guardian;
import com.guardianes.guardian.domain.model.GuardianLevel;
import com.guardianes.walking.domain.EnergyCalculationService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("BattleEngine Service Tests")
@ExtendWith(MockitoExtension.class)
class BattleEngineTest {

  @Mock private EnergyCalculationService energyService;

  private BattleEngine battleEngine;
  private Battle testBattle;
  private Guardian challenger;
  private Guardian defender;
  private Card testCard;
  private BattleMove testMove;

  @BeforeEach
  void setUp() {
    battleEngine = new BattleEngine(energyService);

    // Create test guardians
    challenger =
        new Guardian(
            1L,
            "challenger",
            "challenger@test.com",
            "hashedpassword",
            "TestChallenger",
            LocalDate.of(2000, 1, 1),
            GuardianLevel.INITIATE,
            0,
            0,
            0,
            LocalDateTime.now(),
            LocalDateTime.now(),
            true);
    defender =
        new Guardian(
            2L,
            "defender",
            "defender@test.com",
            "hashedpassword",
            "TestDefender",
            LocalDate.of(2000, 1, 1),
            GuardianLevel.INITIATE,
            0,
            0,
            0,
            LocalDateTime.now(),
            LocalDateTime.now(),
            true);

    // Create test battle
    // Create test battle with simulated ID
    testBattle =
        new Battle(
            100L, // Simulated ID
            challenger.getId(),
            defender.getId(),
            BattleType.PVP_DUEL,
            BattleStatus.IN_PROGRESS,
            new ArrayList<>(),
            0,
            null,
            null,
            LocalDateTime.now(),
            null,
            LocalDateTime.now());

    // Create test card
    testCard =
        new Card(
            50L, // Simulated ID
            "Fire Strike",
            "A powerful fire attack",
            CardElement.FIRE,
            CardRarity.COMMON,
            50, // attack power
            30, // defense power
            5, // energy cost (max 10)
            null,
            "FIRESTRIKE000001", // 16 chars
            null,
            LocalDateTime.now(),
            true);

    // Create test move
    testMove =
        BattleMove.createAttack(
            testBattle.getId(),
            challenger.getId(),
            testCard.getId(),
            defender.getId(),
            5, // energy cost matches card
            50,
            "Fire strike attack");
  }

  @Nested
  @DisplayName("BattleEngine Constructor Tests")
  class BattleEngineConstructorTests {

    @Test
    @DisplayName("should create battle engine with valid energy service")
    void shouldCreateBattleEngineWithValidEnergyService() {
      // When
      BattleEngine engine = new BattleEngine(energyService);

      // Then
      assertNotNull(engine);
    }

    @Test
    @DisplayName("should throw exception when energy service is null")
    void shouldThrowExceptionWhenEnergyServiceIsNull() {
      // When & Then
      assertThrows(
          NullPointerException.class,
          () -> new BattleEngine(null),
          "Energy service cannot be null");
    }
  }

  @DisplayName("Move Execution Tests")
  class MoveExecutionTests {

    @BeforeEach
    void setUp() {
      when(energyService.getCurrentEnergyBalance(challenger.getId())).thenReturn(100);
    }

    @Test
    @DisplayName("should execute move successfully with sufficient energy")
    void shouldExecuteMoveSuccessfullyWithSufficientEnergy() {
      // When
      Battle result =
          battleEngine.executeMove(testBattle, testMove, testCard, challenger, defender);

      // Then
      assertNotNull(result);
      assertEquals(1, result.getTotalMoves());
      assertEquals(15, result.getTotalEnergySpent());
      verify(energyService).getCurrentEnergyBalance(challenger.getId());
    }

    @Test
    @DisplayName("should throw exception when battle is null")
    void shouldThrowExceptionWhenBattleIsNull() {
      // When & Then
      assertThrows(
          NullPointerException.class,
          () -> battleEngine.executeMove(null, testMove, testCard, challenger, defender),
          "Battle cannot be null");
    }

    @Test
    @DisplayName("should throw exception when move is null")
    void shouldThrowExceptionWhenMoveIsNull() {
      // When & Then
      assertThrows(
          NullPointerException.class,
          () -> battleEngine.executeMove(testBattle, null, testCard, challenger, defender),
          "Battle move cannot be null");
    }

    @Test
    @DisplayName("should throw exception when card is null")
    void shouldThrowExceptionWhenCardIsNull() {
      // When & Then
      assertThrows(
          NullPointerException.class,
          () -> battleEngine.executeMove(testBattle, testMove, null, challenger, defender),
          "Card cannot be null");
    }

    @Test
    @DisplayName("should throw exception when challenger is null")
    void shouldThrowExceptionWhenChallengerIsNull() {
      // When & Then
      assertThrows(
          NullPointerException.class,
          () -> battleEngine.executeMove(testBattle, testMove, testCard, null, defender),
          "Challenger cannot be null");
    }

    @Test
    @DisplayName("should throw exception when battle is not active")
    void shouldThrowExceptionWhenBattleIsNotActive() {
      // Given
      Battle inactiveBattle =
          Battle.createChallenge(challenger.getId(), defender.getId(), BattleType.PVP_DUEL);

      // When & Then
      assertThrows(
          IllegalStateException.class,
          () -> battleEngine.executeMove(inactiveBattle, testMove, testCard, challenger, defender),
          "Cannot execute moves on inactive battle");
    }

    @Test
    @DisplayName("should throw exception when challenger is not participant")
    void shouldThrowExceptionWhenChallengerIsNotParticipant() {
      // Given
      Guardian nonParticipant =
          new Guardian(
              999L,
              "nonparticipant",
              "non@test.com",
              "hashedpassword",
              "NonParticipant",
              LocalDate.of(2000, 1, 1),
              GuardianLevel.INITIATE,
              0,
              0,
              0,
              LocalDateTime.now(),
              LocalDateTime.now(),
              true);

      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () -> battleEngine.executeMove(testBattle, testMove, testCard, nonParticipant, defender),
          "Guardian is not a participant in this battle");
    }

    @Test
    @DisplayName("should throw exception when insufficient energy")
    void shouldThrowExceptionWhenInsufficientEnergy() {
      // Given
      when(energyService.getCurrentEnergyBalance(challenger.getId()))
          .thenReturn(5); // Less than card cost

      // When & Then
      assertThrows(
          InsufficientEnergyException.class,
          () -> battleEngine.executeMove(testBattle, testMove, testCard, challenger, defender),
          "Guardian does not have enough energy to play this card");
    }

    @Test
    @DisplayName("should handle moves without target for solo battles")
    void shouldHandleMovesWithoutTargetForSoloBattles() {
      // Given
      Battle soloBattle = Battle.createSoloChallenge(challenger.getId()).start();
      BattleMove soloMove =
          BattleMove.createAttack(
              soloBattle.getId(),
              challenger.getId(),
              testCard.getId(),
              null,
              15,
              50,
              "Solo attack");

      // When
      Battle result = battleEngine.executeMove(soloBattle, soloMove, testCard, challenger, null);

      // Then
      assertNotNull(result);
      assertEquals(1, result.getTotalMoves());
    }
  }

  @DisplayName("Battle Resolution Tests")
  class BattleResolutionTests {

    @Test
    @DisplayName("should resolve completed battle successfully")
    void shouldResolveCompletedBattleSuccessfully() {
      // Given
      Battle completedBattle = testBattle.complete(challenger.getId(), 150);
      List<Guardian> participants = Arrays.asList(challenger, defender);

      // When
      BattleResult result = battleEngine.resolveBattle(completedBattle, participants);

      // Then
      assertNotNull(result);
      assertEquals(completedBattle.getId(), result.battleId());
      assertEquals(challenger.getId(), result.winnerId());
      assertEquals(2, result.xpRewards().size());
    }

    @Test
    @DisplayName("should throw exception when battle is null")
    void shouldThrowExceptionWhenBattleIsNullForResolution() {
      // Given
      List<Guardian> participants = Arrays.asList(challenger, defender);

      // When & Then
      assertThrows(
          NullPointerException.class,
          () -> battleEngine.resolveBattle(null, participants),
          "Battle cannot be null");
    }

    @Test
    @DisplayName("should throw exception when participants is null")
    void shouldThrowExceptionWhenParticipantsIsNull() {
      // Given
      Battle completedBattle = testBattle.complete(challenger.getId(), 150);

      // When & Then
      assertThrows(
          NullPointerException.class,
          () -> battleEngine.resolveBattle(completedBattle, null),
          "Participants cannot be null");
    }

    @Test
    @DisplayName("should throw exception when battle is not completed")
    void shouldThrowExceptionWhenBattleIsNotCompleted() {
      // Given
      List<Guardian> participants = Arrays.asList(challenger, defender);

      // When & Then
      assertThrows(
          IllegalStateException.class,
          () -> battleEngine.resolveBattle(testBattle, participants),
          "Cannot resolve incomplete battle");
    }

    @Test
    @DisplayName("should resolve solo battle with single participant")
    void shouldResolveSoloBattleWithSingleParticipant() {
      // Given
      Battle soloBattle =
          Battle.createSoloChallenge(challenger.getId()).start().complete(challenger.getId(), 100);
      List<Guardian> participants = Collections.singletonList(challenger);

      // When
      BattleResult result = battleEngine.resolveBattle(soloBattle, participants);

      // Then
      assertNotNull(result);
      assertEquals(challenger.getId(), result.winnerId());
      assertEquals(1, result.xpRewards().size());
    }
  }

  @DisplayName("Battle Start Validation Tests")
  class BattleStartValidationTests {

    @Test
    @DisplayName("should allow battle start with sufficient energy")
    void shouldAllowBattleStartWithSufficientEnergy() {
      // Given
      when(energyService.getCurrentEnergyBalance(challenger.getId())).thenReturn(50);

      // When
      boolean canStart = battleEngine.canStartBattle(challenger, BattleType.PVP_DUEL);

      // Then
      assertTrue(canStart);
      verify(energyService).getCurrentEnergyBalance(challenger.getId());
    }

    @Test
    @DisplayName("should prevent battle start with insufficient energy")
    void shouldPreventBattleStartWithInsufficientEnergy() {
      // Given
      when(energyService.getCurrentEnergyBalance(challenger.getId())).thenReturn(5);

      // When
      boolean canStart = battleEngine.canStartBattle(challenger, BattleType.PVP_DUEL);

      // Then
      assertFalse(canStart);
      verify(energyService).getCurrentEnergyBalance(challenger.getId());
    }

    @Test
    @DisplayName("should throw exception when guardian is null")
    void shouldThrowExceptionWhenGuardianIsNullForBattleStart() {
      // When & Then
      assertThrows(
          NullPointerException.class,
          () -> battleEngine.canStartBattle(null, BattleType.PVP_DUEL),
          "Guardian cannot be null");
    }

    @Test
    @DisplayName("should throw exception when battle type is null")
    void shouldThrowExceptionWhenBattleTypeIsNullForBattleStart() {
      // When & Then
      assertThrows(
          NullPointerException.class,
          () -> battleEngine.canStartBattle(challenger, null),
          "Battle type cannot be null");
    }

    @Test
    @DisplayName("should validate different battle types correctly")
    void shouldValidateDifferentBattleTypesCorrectly() {
      // Given
      when(energyService.getCurrentEnergyBalance(challenger.getId())).thenReturn(25);

      // When & Then
      assertTrue(battleEngine.canStartBattle(challenger, BattleType.SOLO_CHALLENGE)); // 10 min
      assertTrue(battleEngine.canStartBattle(challenger, BattleType.PVP_DUEL)); // 15 min
      assertTrue(battleEngine.canStartBattle(challenger, BattleType.COOPERATIVE_BATTLE)); // 20 min
    }
  }

  @DisplayName("Move Calculation Tests")
  class MoveCalculationTests {

    @Test
    @DisplayName("should calculate max possible moves with available cards")
    void shouldCalculateMaxPossibleMovesWithAvailableCards() {
      // Given
      when(energyService.getCurrentEnergyBalance(challenger.getId())).thenReturn(50);
      Card cheapCard =
          new Card(
              51L, // Simulated ID
              "Cheap Strike",
              "Low cost attack",
              CardElement.EARTH,
              CardRarity.COMMON,
              20,
              15,
              5,
              null,
              "CHEAPSTRIKE00001", // 16 chars (Alphanumeric)
              null,
              LocalDateTime.now(),
              true);
      List<Card> availableCards = Arrays.asList(testCard, cheapCard);

      // When
      int maxMoves = battleEngine.calculateMaxPossibleMoves(challenger, availableCards);

      // Then
      assertEquals(10, maxMoves); // 50 energy / 5 energy per cheapest card
      verify(energyService).getCurrentEnergyBalance(challenger.getId());
    }

    @Test
    @DisplayName("should return zero moves when no affordable cards")
    void shouldReturnZeroMovesWhenNoAffordableCards() {
      // Given
      when(energyService.getCurrentEnergyBalance(challenger.getId())).thenReturn(5);
      Card expensiveCard =
          new Card(
              52L, // Simulated ID
              "Expensive Strike",
              "High cost attack",
              CardElement.FIRE,
              CardRarity.LEGENDARY,
              100,
              80,
              10, // Max allowed energy
              null,
              "EXPENSIVESTRIK01", // 16 chars
              null,
              LocalDateTime.now(),
              true);
      List<Card> availableCards = Collections.singletonList(expensiveCard);

      // When
      int maxMoves = battleEngine.calculateMaxPossibleMoves(challenger, availableCards);

      // Then
      assertEquals(0, maxMoves);
    }

    @Test
    @DisplayName("should throw exception when guardian is null")
    void shouldThrowExceptionWhenGuardianIsNullForMoveCalculation() {
      // Given
      List<Card> availableCards = Collections.singletonList(testCard);

      // When & Then
      assertThrows(
          NullPointerException.class,
          () -> battleEngine.calculateMaxPossibleMoves(null, availableCards),
          "Guardian cannot be null");
    }

    @Test
    @DisplayName("should throw exception when available cards is null")
    void shouldThrowExceptionWhenAvailableCardsIsNull() {
      // When & Then
      assertThrows(
          NullPointerException.class,
          () -> battleEngine.calculateMaxPossibleMoves(challenger, null),
          "Available cards cannot be null");
    }

    @Test
    @DisplayName("should handle empty card list")
    void shouldHandleEmptyCardList() {
      // Given
      when(energyService.getCurrentEnergyBalance(challenger.getId())).thenReturn(100);
      List<Card> emptyCards = Collections.emptyList();

      // When
      int maxMoves = battleEngine.calculateMaxPossibleMoves(challenger, emptyCards);

      // Then
      assertEquals(0, maxMoves);
    }
  }

  @DisplayName("Target Validation Tests")
  class TargetValidationTests {

    @Test
    @DisplayName("should validate targeted moves require valid targets")
    void shouldValidateTargetedMovesRequireValidTargets() {
      // Given
      when(energyService.getCurrentEnergyBalance(challenger.getId())).thenReturn(100);
      BattleMove targetedMove =
          BattleMove.createAttack(
              testBattle.getId(),
              challenger.getId(),
              testCard.getId(),
              999L, // Invalid target ID
              15,
              50,
              "Attack with invalid target");

      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () -> battleEngine.executeMove(testBattle, targetedMove, testCard, challenger, null),
          "Target Guardian is required for this move type");
    }

    @Test
    @DisplayName("should allow targeting in solo battles")
    void shouldAllowTargetingInSoloBattles() {
      // Given
      Battle soloBattle = Battle.createSoloChallenge(challenger.getId()).start();
      when(energyService.getCurrentEnergyBalance(challenger.getId())).thenReturn(100);
      BattleMove soloMove =
          BattleMove.createAttack(
              soloBattle.getId(),
              challenger.getId(),
              testCard.getId(),
              999L, // AI target
              15,
              50,
              "Solo battle attack");

      // When
      Battle result = battleEngine.executeMove(soloBattle, soloMove, testCard, challenger, null);

      // Then
      assertNotNull(result);
      assertEquals(1, result.getTotalMoves());
    }

    @Test
    @DisplayName("should validate targets are battle participants")
    void shouldValidateTargetsAreBattleParticipants() {
      // Given
      when(energyService.getCurrentEnergyBalance(challenger.getId())).thenReturn(100);
      Guardian nonParticipant =
          new Guardian(
              999L,
              "nonparticipant2",
              "non2@test.com",
              "hashedpassword",
              "NonParticipant2",
              LocalDate.of(2000, 1, 1),
              GuardianLevel.INITIATE,
              0,
              0,
              0,
              LocalDateTime.now(),
              LocalDateTime.now(),
              true);
      BattleMove invalidTargetMove =
          BattleMove.createAttack(
              testBattle.getId(),
              challenger.getId(),
              testCard.getId(),
              nonParticipant.getId(),
              15,
              50,
              "Attack on non-participant");

      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () ->
              battleEngine.executeMove(
                  testBattle, invalidTargetMove, testCard, challenger, nonParticipant),
          "Target must be a battle participant");
    }
  }

  @Nested
  @DisplayName("Battle Completion Logic Tests")
  class BattleCompletionLogicTests {

    @Test
    @DisplayName("should complete battle when energy threshold is reached")
    void shouldCompleteBattleWhenEnergyThresholdIsReached() {
      // Given
      when(energyService.getCurrentEnergyBalance(challenger.getId())).thenReturn(100);
      Battle highEnergyBattle = testBattle;

      // Simulate adding enough moves to reach energy threshold
      for (int i = 0; i < 21; i++) { // 21 * 5 = 105 energy (exceeds 100 threshold)
        BattleMove move =
            BattleMove.createAttack(
                testBattle.getId(),
                challenger.getId(),
                testCard.getId(),
                defender.getId(),
                5,
                50,
                "Energy threshold move " + i);
        highEnergyBattle =
            battleEngine.executeMove(highEnergyBattle, move, testCard, challenger, defender);
        if (highEnergyBattle.isCompleted()) {
          break;
        }
      }

      // Then
      assertTrue(highEnergyBattle.isCompleted());
      assertEquals(challenger.getId(), highEnergyBattle.getWinnerId());
    }

    @Test
    @DisplayName("should not complete battle prematurely")
    void shouldNotCompleteBattlePrematurely() {
      // Given
      when(energyService.getCurrentEnergyBalance(challenger.getId())).thenReturn(100);

      // When - Execute single move with low energy
      Battle result =
          battleEngine.executeMove(testBattle, testMove, testCard, challenger, defender);

      // Then
      assertFalse(result.isCompleted());
      assertTrue(result.isActive());
    }
  }

  @DisplayName("XP Reward Calculation Tests")
  class XpRewardCalculationTests {

    @Test
    @DisplayName("should calculate XP rewards for all participants")
    void shouldCalculateXpRewardsForAllParticipants() {
      // Given
      Battle completedBattle = testBattle.complete(challenger.getId(), 150);
      List<Guardian> participants = Arrays.asList(challenger, defender);

      // When
      BattleResult result = battleEngine.resolveBattle(completedBattle, participants);

      // Then
      assertEquals(2, result.xpRewards().size());

      // Verify winner gets appropriate XP
      BattleEngine.XpReward challengerReward =
          result.xpRewards().stream()
              .filter(reward -> reward.guardianId().equals(challenger.getId()))
              .findFirst()
              .orElse(null);
      assertNotNull(challengerReward);
      assertTrue(challengerReward.amount() > 0);

      // Verify defender gets participation XP
      BattleEngine.XpReward defenderReward =
          result.xpRewards().stream()
              .filter(reward -> reward.guardianId().equals(defender.getId()))
              .findFirst()
              .orElse(null);
      assertNotNull(defenderReward);
      assertTrue(defenderReward.amount() > 0);
    }

    @Test
    @DisplayName("should give winner bonus in PvP battles")
    void shouldGiveWinnerBonusInPvpBattles() {
      // Given
      Battle pvpBattle = testBattle.complete(challenger.getId(), 150);
      List<Guardian> participants = Arrays.asList(challenger, defender);

      // When
      BattleResult result = battleEngine.resolveBattle(pvpBattle, participants);

      // Then
      BattleEngine.XpReward challengerReward =
          result.xpRewards().stream()
              .filter(reward -> reward.guardianId().equals(challenger.getId()))
              .findFirst()
              .orElse(null);
      BattleEngine.XpReward defenderReward =
          result.xpRewards().stream()
              .filter(reward -> reward.guardianId().equals(defender.getId()))
              .findFirst()
              .orElse(null);

      // Winner should get more XP than loser in PvP
      assertTrue(challengerReward.amount() > defenderReward.amount());
    }

    @Test
    @DisplayName("should handle solo battle XP calculation")
    void shouldHandleSoloBattleXpCalculation() {
      // Given
      Battle soloBattle =
          Battle.createSoloChallenge(challenger.getId()).start().complete(challenger.getId(), 75);
      List<Guardian> participants = Collections.singletonList(challenger);

      // When
      BattleResult result = battleEngine.resolveBattle(soloBattle, participants);

      // Then
      assertEquals(1, result.xpRewards().size());
      BattleEngine.XpReward soloReward = result.xpRewards().get(0);
      assertEquals(challenger.getId(), soloReward.guardianId());
      assertTrue(soloReward.amount() > 0);
    }
  }

  @Nested
  @DisplayName("Battle Statistics Tests")
  class BattleStatisticsTests {

    @Test
    @DisplayName("should calculate battle statistics correctly")
    void shouldCalculateBattleStatisticsCorrectly() {
      // Given
      when(energyService.getCurrentEnergyBalance(challenger.getId())).thenReturn(100);

      // Execute multiple moves
      Battle evolvedBattle = testBattle;
      for (int i = 0; i < 3; i++) {
        BattleMove move =
            BattleMove.createAttack(
                testBattle.getId(),
                challenger.getId(),
                testCard.getId(),
                defender.getId(),
                5,
                50,
                "Test move " + i);
        evolvedBattle =
            battleEngine.executeMove(evolvedBattle, move, testCard, challenger, defender);
      }

      Battle completedBattle = evolvedBattle.complete(challenger.getId(), 150);
      List<Guardian> participants = Arrays.asList(challenger, defender);

      // When
      BattleResult result = battleEngine.resolveBattle(completedBattle, participants);

      // Then
      assertEquals(3, result.totalMoves());
      assertEquals(15, result.totalEnergySpent()); // 3 moves * 5 energy each
      assertNotNull(result.startedAt());
      assertNotNull(result.completedAt());
    }
  }
}

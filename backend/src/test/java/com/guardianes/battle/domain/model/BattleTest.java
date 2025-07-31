package com.guardianes.battle.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Battle Domain Model Tests")
class BattleTest {

  private Long challengerId;
  private Long defenderId;
  private BattleType battleType;
  private LocalDateTime now;

  @BeforeEach
  void setUp() {
    challengerId = 1L;
    defenderId = 2L;
    battleType = BattleType.PVP_DUEL;
    now = LocalDateTime.now();
  }

  @DisplayName("Battle Creation Tests")
  class BattleCreationTests {

    @Test
    @DisplayName("should create battle challenge successfully")
    void shouldCreateBattleChallengeSuccessfully() {
      // When
      Battle battle = Battle.createChallenge(challengerId, defenderId, battleType);

      // Then
      assertNotNull(battle);
      assertEquals(challengerId, battle.getChallengerId());
      assertEquals(defenderId, battle.getDefenderId());
      assertEquals(battleType, battle.getType());
      assertEquals(BattleStatus.PENDING, battle.getStatus());
      assertTrue(battle.getMoves().isEmpty());
      assertEquals(0, battle.getTotalEnergySpent());
      assertNull(battle.getWinnerId());
      assertNull(battle.getXpReward());
      assertNotNull(battle.getCreatedAt());
    }

    @Test
    @DisplayName("should create solo challenge successfully")
    void shouldCreateSoloChallengeSuccessfully() {
      // When
      Battle battle = Battle.createSoloChallenge(challengerId);

      // Then
      assertNotNull(battle);
      assertEquals(challengerId, battle.getChallengerId());
      assertNull(battle.getDefenderId());
      assertEquals(BattleType.SOLO_CHALLENGE, battle.getType());
      assertEquals(BattleStatus.PENDING, battle.getStatus());
      assertTrue(battle.isSoloBattle());
      assertFalse(battle.isPvPBattle());
    }

    @Test
    @DisplayName("should throw exception when challenger ID is null")
    void shouldThrowExceptionWhenChallengerIdIsNull() {
      // When & Then
      assertThrows(
          NullPointerException.class,
          () -> Battle.createChallenge(null, defenderId, battleType),
          "Challenger ID cannot be null");
    }

    @Test
    @DisplayName("should throw exception when challenger equals defender")
    void shouldThrowExceptionWhenChallengerEqualsDefender() {
      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () -> Battle.createChallenge(challengerId, challengerId, battleType),
          "Challenger and defender cannot be the same Guardian");
    }

    @Test
    @DisplayName("should throw exception when battle type is null")
    void shouldThrowExceptionWhenBattleTypeIsNull() {
      // When & Then
      assertThrows(
          NullPointerException.class,
          () -> Battle.createChallenge(challengerId, defenderId, null),
          "Battle type cannot be null");
    }
  }

  @DisplayName("Battle State Management Tests")
  class BattleStateManagementTests {

    private Battle pendingBattle;

    @BeforeEach
    void setUp() {
      pendingBattle = Battle.createChallenge(challengerId, defenderId, battleType);
    }

    @Test
    @DisplayName("should start pending battle successfully")
    void shouldStartPendingBattleSuccessfully() {
      // When
      Battle startedBattle = pendingBattle.start();

      // Then
      assertEquals(BattleStatus.IN_PROGRESS, startedBattle.getStatus());
      assertNotNull(startedBattle.getStartedAt());
      assertTrue(startedBattle.isActive());
      assertFalse(startedBattle.canStart());
    }

    @Test
    @DisplayName("should throw exception when starting non-pending battle")
    void shouldThrowExceptionWhenStartingNonPendingBattle() {
      // Given
      Battle activeBattle = pendingBattle.start();

      // When & Then
      assertThrows(
          IllegalStateException.class,
          activeBattle::start,
          "Battle cannot be started in current status: IN_PROGRESS");
    }

    @Test
    @DisplayName("should complete active battle successfully")
    void shouldCompleteActiveBattleSuccessfully() {
      // Given
      Battle activeBattle = pendingBattle.start();
      Long winnerId = challengerId;
      int xpReward = 100;

      // When
      Battle completedBattle = activeBattle.complete(winnerId, xpReward);

      // Then
      assertEquals(BattleStatus.COMPLETED, completedBattle.getStatus());
      assertEquals(winnerId, completedBattle.getWinnerId());
      assertEquals(xpReward, completedBattle.getXpReward());
      assertNotNull(completedBattle.getCompletedAt());
      assertTrue(completedBattle.isCompleted());
      assertFalse(completedBattle.isActive());
    }

    @Test
    @DisplayName("should throw exception when completing inactive battle")
    void shouldThrowExceptionWhenCompletingInactiveBattle() {
      // When & Then
      assertThrows(
          IllegalStateException.class,
          () -> pendingBattle.complete(challengerId, 100),
          "Can only complete active battles");
    }

    @Test
    @DisplayName("should throw exception when winner is not a participant")
    void shouldThrowExceptionWhenWinnerIsNotParticipant() {
      // Given
      Battle activeBattle = pendingBattle.start();
      Long invalidWinnerId = 999L;

      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () -> activeBattle.complete(invalidWinnerId, 100),
          "Winner must be a battle participant");
    }

    @Test
    @DisplayName("should abandon battle successfully")
    void shouldAbandonBattleSuccessfully() {
      // Given
      Battle activeBattle = pendingBattle.start();

      // When
      Battle abandonedBattle = activeBattle.abandon();

      // Then
      assertEquals(BattleStatus.ABANDONED, abandonedBattle.getStatus());
      assertNull(abandonedBattle.getWinnerId());
      assertEquals(0, abandonedBattle.getXpReward());
      assertNotNull(abandonedBattle.getCompletedAt());
      assertTrue(abandonedBattle.isCompleted());
    }

    @Test
    @DisplayName("should throw exception when abandoning completed battle")
    void shouldThrowExceptionWhenAbandoningCompletedBattle() {
      // Given
      Battle completedBattle = pendingBattle.start().complete(challengerId, 100);

      // When & Then
      assertThrows(
          IllegalStateException.class, completedBattle::abandon, "Cannot abandon completed battle");
    }
  }

  @DisplayName("Battle Move Management Tests")
  class BattleMoveManagementTests {

    private Battle activeBattle;
    private BattleMove testMove;

    @BeforeEach
    void setUp() {
      activeBattle = Battle.createChallenge(challengerId, defenderId, battleType).start();
      testMove =
          BattleMove.createAttack(
              activeBattle.getId(), challengerId, 1L, defenderId, 10, 50, "Test attack move");
    }

    @Test
    @DisplayName("should add move to active battle successfully")
    void shouldAddMoveToActiveBattleSuccessfully() {
      // When
      Battle updatedBattle = activeBattle.addMove(testMove);

      // Then
      assertEquals(1, updatedBattle.getTotalMoves());
      assertEquals(10, updatedBattle.getTotalEnergySpent());
      assertTrue(updatedBattle.getMoves().contains(testMove));
    }

    @Test
    @DisplayName("should throw exception when adding move to inactive battle")
    void shouldThrowExceptionWhenAddingMoveToInactiveBattle() {
      // Given
      Battle pendingBattle = Battle.createChallenge(challengerId, defenderId, battleType);

      // When & Then
      assertThrows(
          IllegalStateException.class,
          () -> pendingBattle.addMove(testMove),
          "Cannot add moves to inactive battle");
    }

    @Test
    @DisplayName("should throw exception when move is null")
    void shouldThrowExceptionWhenMoveIsNull() {
      // When & Then
      assertThrows(
          NullPointerException.class,
          () -> activeBattle.addMove(null),
          "Battle move cannot be null");
    }

    @Test
    @DisplayName("should get moves by guardian correctly")
    void shouldGetMovesByGuardianCorrectly() {
      // Given
      BattleMove challengerMove =
          BattleMove.createAttack(
              activeBattle.getId(), challengerId, 1L, defenderId, 10, 50, "Challenger attack");
      BattleMove defenderMove =
          BattleMove.createDefense(activeBattle.getId(), defenderId, 2L, 8, "Defender defense");

      Battle battleWithMoves = activeBattle.addMove(challengerMove).addMove(defenderMove);

      // When
      List<BattleMove> challengerMoves = battleWithMoves.getMovesByGuardian(challengerId);
      List<BattleMove> defenderMoves = battleWithMoves.getMovesByGuardian(defenderId);

      // Then
      assertEquals(1, challengerMoves.size());
      assertEquals(challengerMove, challengerMoves.get(0));
      assertEquals(1, defenderMoves.size());
      assertEquals(defenderMove, defenderMoves.get(0));
    }
  }

  @DisplayName("Battle Participant Management Tests")
  class BattleParticipantManagementTests {

    private Battle battle;

    @BeforeEach
    void setUp() {
      battle = Battle.createChallenge(challengerId, defenderId, battleType);
    }

    @Test
    @DisplayName("should correctly identify participants")
    void shouldCorrectlyIdentifyParticipants() {
      // Then
      assertTrue(battle.isParticipant(challengerId));
      assertTrue(battle.isParticipant(defenderId));
      assertFalse(battle.isParticipant(999L));
    }

    @Test
    @DisplayName("should get opponent correctly")
    void shouldGetOpponentCorrectly() {
      // When & Then
      assertEquals(defenderId, battle.getOpponent(challengerId));
      assertEquals(challengerId, battle.getOpponent(defenderId));
    }

    @Test
    @DisplayName("should throw exception when getting opponent for non-participant")
    void shouldThrowExceptionWhenGettingOpponentForNonParticipant() {
      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () -> battle.getOpponent(999L),
          "Guardian is not a participant in this battle");
    }

    @Test
    @DisplayName("should return null opponent for solo battle")
    void shouldReturnNullOpponentForSoloBattle() {
      // Given
      Battle soloBattle = Battle.createSoloChallenge(challengerId);

      // When & Then
      assertNull(soloBattle.getOpponent(challengerId));
    }
  }

  @DisplayName("Battle Type Classification Tests")
  class BattleTypeClassificationTests {

    @Test
    @DisplayName("should correctly identify solo battle")
    void shouldCorrectlyIdentifySoloBattle() {
      // Given
      Battle soloBattle = Battle.createSoloChallenge(challengerId);

      // Then
      assertTrue(soloBattle.isSoloBattle());
      assertFalse(soloBattle.isPvPBattle());
    }

    @Test
    @DisplayName("should correctly identify PvP battle")
    void shouldCorrectlyIdentifyPvPBattle() {
      // Given
      Battle pvpBattle = Battle.createChallenge(challengerId, defenderId, BattleType.PVP_DUEL);

      // Then
      assertFalse(pvpBattle.isSoloBattle());
      assertTrue(pvpBattle.isPvPBattle());
    }

    @Test
    @DisplayName("should correctly identify cooperative battle")
    void shouldCorrectlyIdentifyCooperativeBattle() {
      // Given
      Battle cooperativeBattle =
          Battle.createChallenge(challengerId, defenderId, BattleType.COOPERATIVE_BATTLE);

      // Then
      assertFalse(cooperativeBattle.isSoloBattle());
      assertTrue(cooperativeBattle.isPvPBattle());
    }
  }

  @DisplayName("Battle Validation Tests")
  class BattleValidationTests {

    @Test
    @DisplayName("should validate energy spent cannot be negative")
    void shouldValidateEnergySpentCannotBeNegative() {
      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () ->
              new Battle(
                  1L,
                  challengerId,
                  defenderId,
                  battleType,
                  BattleStatus.PENDING,
                  new ArrayList<>(),
                  -10,
                  null,
                  null,
                  null,
                  null,
                  now),
          "Energy spent cannot be negative");
    }

    @Test
    @DisplayName("should validate XP reward cannot be negative")
    void shouldValidateXpRewardCannotBeNegative() {
      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () ->
              new Battle(
                  1L,
                  challengerId,
                  defenderId,
                  battleType,
                  BattleStatus.COMPLETED,
                  new ArrayList<>(),
                  50,
                  challengerId,
                  -100,
                  now,
                  now,
                  now),
          "XP reward cannot be negative");
    }
  }

  @DisplayName("Battle Equality Tests")
  class BattleEqualityTests {

    @Test
    @DisplayName("should be equal when same ID")
    void shouldBeEqualWhenSameId() {
      // Given
      Battle battle1 =
          new Battle(
              1L,
              challengerId,
              defenderId,
              battleType,
              BattleStatus.PENDING,
              new ArrayList<>(),
              0,
              null,
              null,
              null,
              null,
              now);
      Battle battle2 =
          new Battle(
              1L,
              challengerId,
              defenderId,
              battleType,
              BattleStatus.IN_PROGRESS,
              new ArrayList<>(),
              10,
              null,
              null,
              now,
              null,
              now);

      // Then
      assertEquals(battle1, battle2);
      assertEquals(battle1.hashCode(), battle2.hashCode());
    }

    @Test
    @DisplayName("should not be equal when different ID")
    void shouldNotBeEqualWhenDifferentId() {
      // Given
      Battle battle1 =
          new Battle(
              1L,
              challengerId,
              defenderId,
              battleType,
              BattleStatus.PENDING,
              new ArrayList<>(),
              0,
              null,
              null,
              null,
              null,
              now);
      Battle battle2 =
          new Battle(
              2L,
              challengerId,
              defenderId,
              battleType,
              BattleStatus.PENDING,
              new ArrayList<>(),
              0,
              null,
              null,
              null,
              null,
              now);

      // Then
      assertNotEquals(battle1, battle2);
    }
  }

  @DisplayName("Battle String Representation Tests")
  class BattleStringRepresentationTests {

    @Test
    @DisplayName("should have meaningful toString representation")
    void shouldHaveMeaningfulToStringRepresentation() {
      // Given
      Battle battle = Battle.createChallenge(challengerId, defenderId, battleType);

      // When
      String battleString = battle.toString();

      // Then
      assertNotNull(battleString);
      assertTrue(battleString.contains("Battle{"));
      assertTrue(battleString.contains("challengerId=" + challengerId));
      assertTrue(battleString.contains("defenderId=" + defenderId));
      assertTrue(battleString.contains("type=" + battleType));
      assertTrue(battleString.contains("status=" + BattleStatus.PENDING));
      assertTrue(battleString.contains("totalMoves=0"));
      assertTrue(battleString.contains("totalEnergySpent=0"));
    }
  }
}

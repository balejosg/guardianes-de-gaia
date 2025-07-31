package com.guardianes.battle.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BattleMove Domain Model Tests")
class BattleMoveTest {

  private Long battleId;
  private Long guardianId;
  private Long cardId;
  private Long targetGuardianId;
  private Integer energyCost;
  private LocalDateTime executedAt;

  @BeforeEach
  void setUp() {
    battleId = 1L;
    guardianId = 2L;
    cardId = 3L;
    targetGuardianId = 4L;
    energyCost = 10;
    executedAt = LocalDateTime.now();
  }

  @DisplayName("BattleMove Factory Method Tests")
  class BattleMoveFactoryTests {

    @Test
    @DisplayName("should create attack move successfully")
    void shouldCreateAttackMoveSuccessfully() {
      // Given
      String description = "Fire attack on opponent";
      int damage = 50;

      // When
      BattleMove attackMove =
          BattleMove.createAttack(
              battleId, guardianId, cardId, targetGuardianId, energyCost, damage, description);

      // Then
      assertNotNull(attackMove);
      assertEquals(battleId, attackMove.getBattleId());
      assertEquals(guardianId, attackMove.getGuardianId());
      assertEquals(cardId, attackMove.getCardId());
      assertEquals(targetGuardianId, attackMove.getTargetGuardianId());
      assertEquals(BattleMoveType.ATTACK, attackMove.getMoveType());
      assertEquals(energyCost, attackMove.getEnergyCost());
      assertEquals(damage, attackMove.getDamageDealt());
      assertEquals(0, attackMove.getHealingDone());
      assertEquals(description, attackMove.getMoveDescription());
      assertNotNull(attackMove.getExecutedAt());
      assertTrue(attackMove.isAttack());
      assertTrue(attackMove.isTargeted());
      assertTrue(attackMove.causedDamage());
      assertFalse(attackMove.providedHealing());
    }

    @Test
    @DisplayName("should create defense move successfully")
    void shouldCreateDefenseMoveSuccessfully() {
      // Given
      String description = "Shield defense";

      // When
      BattleMove defenseMove =
          BattleMove.createDefense(battleId, guardianId, cardId, energyCost, description);

      // Then
      assertNotNull(defenseMove);
      assertEquals(BattleMoveType.DEFENSE, defenseMove.getMoveType());
      assertNull(defenseMove.getTargetGuardianId());
      assertEquals(0, defenseMove.getDamageDealt());
      assertEquals(0, defenseMove.getHealingDone());
      assertTrue(defenseMove.isDefense());
      assertFalse(defenseMove.isTargeted());
      assertFalse(defenseMove.causedDamage());
      assertFalse(defenseMove.providedHealing());
    }

    @Test
    @DisplayName("should create support move successfully")
    void shouldCreateSupportMoveSuccessfully() {
      // Given
      String description = "Healing support";
      int healing = 30;

      // When
      BattleMove supportMove =
          BattleMove.createSupport(
              battleId, guardianId, cardId, targetGuardianId, energyCost, healing, description);

      // Then
      assertNotNull(supportMove);
      assertEquals(BattleMoveType.SUPPORT, supportMove.getMoveType());
      assertEquals(targetGuardianId, supportMove.getTargetGuardianId());
      assertEquals(0, supportMove.getDamageDealt());
      assertEquals(healing, supportMove.getHealingDone());
      assertTrue(supportMove.isSupport());
      assertTrue(supportMove.isTargeted());
      assertFalse(supportMove.causedDamage());
      assertTrue(supportMove.providedHealing());
    }

    @Test
    @DisplayName("should create special move successfully")
    void shouldCreateSpecialMoveSuccessfully() {
      // Given
      String description = "Special elemental burst";
      int damage = 40;
      int healing = 20;

      // When
      BattleMove specialMove =
          BattleMove.createSpecial(
              battleId,
              guardianId,
              cardId,
              targetGuardianId,
              energyCost,
              damage,
              healing,
              description);

      // Then
      assertNotNull(specialMove);
      assertEquals(BattleMoveType.SPECIAL, specialMove.getMoveType());
      assertEquals(targetGuardianId, specialMove.getTargetGuardianId());
      assertEquals(damage, specialMove.getDamageDealt());
      assertEquals(healing, specialMove.getHealingDone());
      assertTrue(specialMove.isSpecial());
      assertTrue(specialMove.isTargeted());
      assertTrue(specialMove.causedDamage());
      assertTrue(specialMove.providedHealing());
    }

    @Test
    @DisplayName("should throw exception when attack move lacks target")
    void shouldThrowExceptionWhenAttackMoveLacksTarget() {
      // When & Then
      assertThrows(
          NullPointerException.class,
          () ->
              BattleMove.createAttack(
                  battleId, guardianId, cardId, null, energyCost, 50, "Attack without target"),
          "Target required for attack moves");
    }
  }

  @DisplayName("BattleMove Validation Tests")
  class BattleMoveValidationTests {

    @Test
    @DisplayName("should throw exception when battle ID is null")
    void shouldThrowExceptionWhenBattleIdIsNull() {
      // When & Then
      assertThrows(
          NullPointerException.class,
          () ->
              new BattleMove(
                  1L,
                  null,
                  guardianId,
                  cardId,
                  targetGuardianId,
                  BattleMoveType.ATTACK,
                  energyCost,
                  50,
                  0,
                  "Test move",
                  executedAt),
          "Battle ID cannot be null");
    }

    @Test
    @DisplayName("should throw exception when guardian ID is null")
    void shouldThrowExceptionWhenGuardianIdIsNull() {
      // When & Then
      assertThrows(
          NullPointerException.class,
          () ->
              new BattleMove(
                  1L,
                  battleId,
                  null,
                  cardId,
                  targetGuardianId,
                  BattleMoveType.ATTACK,
                  energyCost,
                  50,
                  0,
                  "Test move",
                  executedAt),
          "Guardian ID cannot be null");
    }

    @Test
    @DisplayName("should throw exception when card ID is null")
    void shouldThrowExceptionWhenCardIdIsNull() {
      // When & Then
      assertThrows(
          NullPointerException.class,
          () ->
              new BattleMove(
                  1L,
                  battleId,
                  guardianId,
                  null,
                  targetGuardianId,
                  BattleMoveType.ATTACK,
                  energyCost,
                  50,
                  0,
                  "Test move",
                  executedAt),
          "Card ID cannot be null");
    }

    @Test
    @DisplayName("should throw exception when move type is null")
    void shouldThrowExceptionWhenMoveTypeIsNull() {
      // When & Then
      assertThrows(
          NullPointerException.class,
          () ->
              new BattleMove(
                  1L,
                  battleId,
                  guardianId,
                  cardId,
                  targetGuardianId,
                  null,
                  energyCost,
                  50,
                  0,
                  "Test move",
                  executedAt),
          "Move type cannot be null");
    }

    @Test
    @DisplayName("should throw exception when executed at is null")
    void shouldThrowExceptionWhenExecutedAtIsNull() {
      // When & Then
      assertThrows(
          NullPointerException.class,
          () ->
              new BattleMove(
                  1L,
                  battleId,
                  guardianId,
                  cardId,
                  targetGuardianId,
                  BattleMoveType.ATTACK,
                  energyCost,
                  50,
                  0,
                  "Test move",
                  null),
          "Executed at cannot be null");
    }

    @Test
    @DisplayName("should throw exception when energy cost is null")
    void shouldThrowExceptionWhenEnergyCostIsNull() {
      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () ->
              new BattleMove(
                  1L,
                  battleId,
                  guardianId,
                  cardId,
                  targetGuardianId,
                  BattleMoveType.ATTACK,
                  null,
                  50,
                  0,
                  "Test move",
                  executedAt),
          "Energy cost must be between 0 and 50");
    }

    @Test
    @DisplayName("should throw exception when energy cost is negative")
    void shouldThrowExceptionWhenEnergyCostIsNegative() {
      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () ->
              new BattleMove(
                  1L,
                  battleId,
                  guardianId,
                  cardId,
                  targetGuardianId,
                  BattleMoveType.ATTACK,
                  -5,
                  50,
                  0,
                  "Test move",
                  executedAt),
          "Energy cost must be between 0 and 50");
    }

    @Test
    @DisplayName("should throw exception when energy cost exceeds maximum")
    void shouldThrowExceptionWhenEnergyCostExceedsMaximum() {
      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () ->
              new BattleMove(
                  1L,
                  battleId,
                  guardianId,
                  cardId,
                  targetGuardianId,
                  BattleMoveType.ATTACK,
                  60,
                  50,
                  0,
                  "Test move",
                  executedAt),
          "Energy cost must be between 0 and 50");
    }

    @Test
    @DisplayName("should throw exception when damage is negative")
    void shouldThrowExceptionWhenDamageIsNegative() {
      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () ->
              new BattleMove(
                  1L,
                  battleId,
                  guardianId,
                  cardId,
                  targetGuardianId,
                  BattleMoveType.ATTACK,
                  energyCost,
                  -10,
                  0,
                  "Test move",
                  executedAt),
          "Damage cannot be negative");
    }

    @Test
    @DisplayName("should throw exception when healing is negative")
    void shouldThrowExceptionWhenHealingIsNegative() {
      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () ->
              new BattleMove(
                  1L,
                  battleId,
                  guardianId,
                  cardId,
                  targetGuardianId,
                  BattleMoveType.SUPPORT,
                  energyCost,
                  0,
                  -20,
                  "Test move",
                  executedAt),
          "Healing cannot be negative");
    }

    @Test
    @DisplayName("should throw exception when description is null")
    void shouldThrowExceptionWhenDescriptionIsNull() {
      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () ->
              new BattleMove(
                  1L,
                  battleId,
                  guardianId,
                  cardId,
                  targetGuardianId,
                  BattleMoveType.ATTACK,
                  energyCost,
                  50,
                  0,
                  null,
                  executedAt),
          "Move description cannot be null or empty");
    }

    @Test
    @DisplayName("should throw exception when description is empty")
    void shouldThrowExceptionWhenDescriptionIsEmpty() {
      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () ->
              new BattleMove(
                  1L,
                  battleId,
                  guardianId,
                  cardId,
                  targetGuardianId,
                  BattleMoveType.ATTACK,
                  energyCost,
                  50,
                  0,
                  "   ",
                  executedAt),
          "Move description cannot be null or empty");
    }

    @Test
    @DisplayName("should throw exception when description exceeds maximum length")
    void shouldThrowExceptionWhenDescriptionExceedsMaximumLength() {
      // Given
      String longDescription = "A".repeat(201);

      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () ->
              new BattleMove(
                  1L,
                  battleId,
                  guardianId,
                  cardId,
                  targetGuardianId,
                  BattleMoveType.ATTACK,
                  energyCost,
                  50,
                  0,
                  longDescription,
                  executedAt),
          "Move description cannot exceed 200 characters");
    }
  }

  @DisplayName("BattleMove Business Logic Tests")
  class BattleMoveBusinessLogicTests {

    @Test
    @DisplayName("should calculate total effect correctly")
    void shouldCalculateTotalEffectCorrectly() {
      // Given
      BattleMove specialMove =
          BattleMove.createSpecial(
              battleId, guardianId, cardId, targetGuardianId, energyCost, 30, 20, "Special move");

      // When
      int totalEffect = specialMove.getTotalEffect();

      // Then
      assertEquals(50, totalEffect); // 30 damage + 20 healing
    }

    @Test
    @DisplayName("should calculate energy efficiency correctly")
    void shouldCalculateEnergyEfficiencyCorrectly() {
      // Given
      BattleMove efficientMove =
          BattleMove.createAttack(
              battleId, guardianId, cardId, targetGuardianId, 10, 50, "Efficient attack");

      // When
      double efficiency = efficientMove.getEnergyEfficiency();

      // Then
      assertEquals(5.0, efficiency, 0.01); // 50 total effect / 10 energy cost
    }

    @Test
    @DisplayName("should handle zero energy cost in efficiency calculation")
    void shouldHandleZeroEnergyCostInEfficiencyCalculation() {
      // Given
      BattleMove freeMove =
          new BattleMove(
              1L,
              battleId,
              guardianId,
              cardId,
              targetGuardianId,
              BattleMoveType.DEFENSE,
              0,
              0,
              0,
              "Free move",
              executedAt);

      // When
      double efficiency = freeMove.getEnergyEfficiency();

      // Then
      assertEquals(0.0, efficiency, 0.01);
    }

    @Test
    @DisplayName("should identify move types correctly")
    void shouldIdentifyMoveTypesCorrectly() {
      // Given
      BattleMove attackMove =
          BattleMove.createAttack(
              battleId, guardianId, cardId, targetGuardianId, energyCost, 50, "Attack");
      BattleMove defenseMove =
          BattleMove.createDefense(battleId, guardianId, cardId, energyCost, "Defense");
      BattleMove supportMove =
          BattleMove.createSupport(
              battleId, guardianId, cardId, targetGuardianId, energyCost, 30, "Support");
      BattleMove specialMove =
          BattleMove.createSpecial(
              battleId, guardianId, cardId, targetGuardianId, energyCost, 20, 15, "Special");

      // Then
      assertTrue(attackMove.isAttack());
      assertFalse(attackMove.isDefense());
      assertFalse(attackMove.isSupport());
      assertFalse(attackMove.isSpecial());

      assertFalse(defenseMove.isAttack());
      assertTrue(defenseMove.isDefense());
      assertFalse(defenseMove.isSupport());
      assertFalse(defenseMove.isSpecial());

      assertFalse(supportMove.isAttack());
      assertFalse(supportMove.isDefense());
      assertTrue(supportMove.isSupport());
      assertFalse(supportMove.isSpecial());

      assertFalse(specialMove.isAttack());
      assertFalse(specialMove.isDefense());
      assertFalse(specialMove.isSupport());
      assertTrue(specialMove.isSpecial());
    }
  }

  @DisplayName("BattleMove Equality Tests")
  class BattleMoveEqualityTests {

    @Test
    @DisplayName("should be equal when same ID, battle ID, guardian ID, and executed time")
    void shouldBeEqualWhenSameIdBattleIdGuardianIdAndExecutedTime() {
      // Given
      LocalDateTime fixedTime = LocalDateTime.now();
      BattleMove move1 =
          new BattleMove(
              1L,
              battleId,
              guardianId,
              cardId,
              targetGuardianId,
              BattleMoveType.ATTACK,
              energyCost,
              50,
              0,
              "Test move",
              fixedTime);
      BattleMove move2 =
          new BattleMove(
              1L,
              battleId,
              guardianId,
              999L, // Different card ID
              targetGuardianId,
              BattleMoveType.DEFENSE, // Different move type
              energyCost,
              0, // Different damage
              30, // Different healing
              "Different description",
              fixedTime);

      // Then
      assertEquals(move1, move2);
      assertEquals(move1.hashCode(), move2.hashCode());
    }

    @Test
    @DisplayName("should not be equal when different ID")
    void shouldNotBeEqualWhenDifferentId() {
      // Given
      LocalDateTime fixedTime = LocalDateTime.now();
      BattleMove move1 =
          new BattleMove(
              1L,
              battleId,
              guardianId,
              cardId,
              targetGuardianId,
              BattleMoveType.ATTACK,
              energyCost,
              50,
              0,
              "Test move",
              fixedTime);
      BattleMove move2 =
          new BattleMove(
              2L, // Different ID
              battleId,
              guardianId,
              cardId,
              targetGuardianId,
              BattleMoveType.ATTACK,
              energyCost,
              50,
              0,
              "Test move",
              fixedTime);

      // Then
      assertNotEquals(move1, move2);
    }
  }

  @DisplayName("BattleMove String Representation Tests")
  class BattleMoveStringRepresentationTests {

    @Test
    @DisplayName("should have meaningful toString representation")
    void shouldHaveMeaningfulToStringRepresentation() {
      // Given
      BattleMove move =
          BattleMove.createAttack(
              battleId, guardianId, cardId, targetGuardianId, energyCost, 50, "Test attack");

      // When
      String moveString = move.toString();

      // Then
      assertNotNull(moveString);
      assertTrue(moveString.contains("BattleMove{"));
      assertTrue(moveString.contains("battleId=" + battleId));
      assertTrue(moveString.contains("guardianId=" + guardianId));
      assertTrue(moveString.contains("cardId=" + cardId));
      assertTrue(moveString.contains("moveType=" + BattleMoveType.ATTACK));
      assertTrue(moveString.contains("energyCost=" + energyCost));
      assertTrue(moveString.contains("damageDealt=50"));
      assertTrue(moveString.contains("targetGuardianId=" + targetGuardianId));
    }
  }
}

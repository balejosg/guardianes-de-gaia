package com.guardianes.battle.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a single move made by a Guardian during a battle. Each move involves playing a card
 * and can target another Guardian or be a general action.
 */
public class BattleMove {
  private final Long id;
  private final Long battleId;
  private final Long guardianId;
  private final Long cardId;
  private final Long targetGuardianId;
  private final BattleMoveType moveType;
  private final Integer energyCost;
  private final Integer damageDealt;
  private final Integer healingDone;
  private final String moveDescription;
  private final LocalDateTime executedAt;

  public BattleMove(
      Long id,
      Long battleId,
      Long guardianId,
      Long cardId,
      Long targetGuardianId,
      BattleMoveType moveType,
      Integer energyCost,
      Integer damageDealt,
      Integer healingDone,
      String moveDescription,
      LocalDateTime executedAt) {
    this.id = id;
    this.battleId = Objects.requireNonNull(battleId, "Battle ID cannot be null");
    this.guardianId = Objects.requireNonNull(guardianId, "Guardian ID cannot be null");
    this.cardId = Objects.requireNonNull(cardId, "Card ID cannot be null");
    this.targetGuardianId = targetGuardianId; // Can be null for non-targeted moves
    this.moveType = Objects.requireNonNull(moveType, "Move type cannot be null");
    this.energyCost = validateEnergyCost(energyCost);
    this.damageDealt = validateDamage(damageDealt);
    this.healingDone = validateHealing(healingDone);
    this.moveDescription = validateDescription(moveDescription);
    this.executedAt = Objects.requireNonNull(executedAt, "Executed at cannot be null");
  }

  public static BattleMove createAttack(
      Long battleId,
      Long guardianId,
      Long cardId,
      Long targetGuardianId,
      Integer energyCost,
      Integer damageDealt,
      String description) {
    return new BattleMove(
        null,
        battleId,
        guardianId,
        cardId,
        Objects.requireNonNull(targetGuardianId, "Target required for attack moves"),
        BattleMoveType.ATTACK,
        energyCost,
        damageDealt,
        0,
        description,
        LocalDateTime.now());
  }

  public static BattleMove createDefense(
      Long battleId, Long guardianId, Long cardId, Integer energyCost, String description) {
    return new BattleMove(
        null,
        battleId,
        guardianId,
        cardId,
        null, // No target for defense moves
        BattleMoveType.DEFENSE,
        energyCost,
        0,
        0,
        description,
        LocalDateTime.now());
  }

  public static BattleMove createSupport(
      Long battleId,
      Long guardianId,
      Long cardId,
      Long targetGuardianId,
      Integer energyCost,
      Integer healingDone,
      String description) {
    return new BattleMove(
        null,
        battleId,
        guardianId,
        cardId,
        targetGuardianId, // Can be null for self-support
        BattleMoveType.SUPPORT,
        energyCost,
        0,
        healingDone,
        description,
        LocalDateTime.now());
  }

  public static BattleMove createSpecial(
      Long battleId,
      Long guardianId,
      Long cardId,
      Long targetGuardianId,
      Integer energyCost,
      Integer damageDealt,
      Integer healingDone,
      String description) {
    return new BattleMove(
        null,
        battleId,
        guardianId,
        cardId,
        targetGuardianId,
        BattleMoveType.SPECIAL,
        energyCost,
        damageDealt,
        healingDone,
        description,
        LocalDateTime.now());
  }

  // Validation methods
  private Integer validateEnergyCost(Integer energyCost) {
    if (energyCost == null || energyCost < 0 || energyCost > 50) {
      throw new IllegalArgumentException("Energy cost must be between 0 and 50");
    }
    return energyCost;
  }

  private Integer validateDamage(Integer damage) {
    if (damage != null && damage < 0) {
      throw new IllegalArgumentException("Damage cannot be negative");
    }
    return damage != null ? damage : 0;
  }

  private Integer validateHealing(Integer healing) {
    if (healing != null && healing < 0) {
      throw new IllegalArgumentException("Healing cannot be negative");
    }
    return healing != null ? healing : 0;
  }

  private String validateDescription(String description) {
    if (description == null || description.trim().isEmpty()) {
      throw new IllegalArgumentException("Move description cannot be null or empty");
    }
    if (description.length() > 200) {
      throw new IllegalArgumentException("Move description cannot exceed 200 characters");
    }
    return description.trim();
  }

  // Business logic methods
  public boolean isTargeted() {
    return targetGuardianId != null;
  }

  public boolean isAttack() {
    return moveType == BattleMoveType.ATTACK;
  }

  public boolean isDefense() {
    return moveType == BattleMoveType.DEFENSE;
  }

  public boolean isSupport() {
    return moveType == BattleMoveType.SUPPORT;
  }

  public boolean isSpecial() {
    return moveType == BattleMoveType.SPECIAL;
  }

  public boolean causedDamage() {
    return damageDealt != null && damageDealt > 0;
  }

  public boolean providedHealing() {
    return healingDone != null && healingDone > 0;
  }

  public int getTotalEffect() {
    return (damageDealt != null ? damageDealt : 0) + (healingDone != null ? healingDone : 0);
  }

  public double getEnergyEfficiency() {
    if (energyCost == 0) return 0.0;
    return (double) getTotalEffect() / energyCost;
  }

  // Getters
  public Long getId() {
    return id;
  }

  public Long getBattleId() {
    return battleId;
  }

  public Long getGuardianId() {
    return guardianId;
  }

  public Long getCardId() {
    return cardId;
  }

  public Long getTargetGuardianId() {
    return targetGuardianId;
  }

  public BattleMoveType getMoveType() {
    return moveType;
  }

  public Integer getEnergyCost() {
    return energyCost;
  }

  public Integer getDamageDealt() {
    return damageDealt;
  }

  public Integer getHealingDone() {
    return healingDone;
  }

  public String getMoveDescription() {
    return moveDescription;
  }

  public LocalDateTime getExecutedAt() {
    return executedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BattleMove that = (BattleMove) o;
    return Objects.equals(id, that.id)
        && Objects.equals(battleId, that.battleId)
        && Objects.equals(guardianId, that.guardianId)
        && Objects.equals(executedAt, that.executedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, battleId, guardianId, executedAt);
  }

  @Override
  public String toString() {
    return "BattleMove{"
        + "id="
        + id
        + ", battleId="
        + battleId
        + ", guardianId="
        + guardianId
        + ", cardId="
        + cardId
        + ", moveType="
        + moveType
        + ", energyCost="
        + energyCost
        + ", damageDealt="
        + damageDealt
        + ", healingDone="
        + healingDone
        + ", targetGuardianId="
        + targetGuardianId
        + '}';
  }
}

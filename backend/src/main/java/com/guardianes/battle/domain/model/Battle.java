package com.guardianes.battle.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a battle between Guardians using their collected cards. Battles consume energy and
 * result in XP rewards for participants.
 */
public class Battle {
  private final Long id;
  private final Long challengerId;
  private final Long defenderId;
  private final BattleType type;
  private final BattleStatus status;
  private final List<BattleMove> moves;
  private final Integer totalEnergySpent;
  private final Long winnerId;
  private final Integer xpReward;
  private final LocalDateTime startedAt;
  private final LocalDateTime completedAt;
  private final LocalDateTime createdAt;

  public Battle(
      Long id,
      Long challengerId,
      Long defenderId,
      BattleType type,
      BattleStatus status,
      List<BattleMove> moves,
      Integer totalEnergySpent,
      Long winnerId,
      Integer xpReward,
      LocalDateTime startedAt,
      LocalDateTime completedAt,
      LocalDateTime createdAt) {
    this.id = id;
    this.challengerId = Objects.requireNonNull(challengerId, "Challenger ID cannot be null");
    this.defenderId = validateDefenderId(defenderId);
    this.type = Objects.requireNonNull(type, "Battle type cannot be null");
    this.status = Objects.requireNonNull(status, "Battle status cannot be null");
    this.moves = moves != null ? new ArrayList<>(moves) : new ArrayList<>();
    this.totalEnergySpent = validateEnergySpent(totalEnergySpent);
    this.winnerId = winnerId;
    this.xpReward = validateXpReward(xpReward);
    this.startedAt = startedAt;
    this.completedAt = completedAt;
    this.createdAt = Objects.requireNonNull(createdAt, "Created at cannot be null");
  }

  public static Battle createChallenge(Long challengerId, Long defenderId, BattleType type) {
    validateBattleParticipants(challengerId, defenderId);
    return new Battle(
        null,
        challengerId,
        defenderId,
        type,
        BattleStatus.PENDING,
        new ArrayList<>(),
        0,
        null,
        null,
        null,
        null,
        LocalDateTime.now());
  }

  public static Battle createSoloChallenge(Long challengerId) {
    return new Battle(
        null,
        challengerId,
        null, // No defender for solo battles
        BattleType.SOLO_CHALLENGE,
        BattleStatus.PENDING,
        new ArrayList<>(),
        0,
        null,
        null,
        null,
        null,
        LocalDateTime.now());
  }

  // Validation methods
  private Long validateDefenderId(Long defenderId) {
    // Defender can be null for solo battles
    if (type == BattleType.SOLO_CHALLENGE) {
      return null;
    }
    return Objects.requireNonNull(defenderId, "Defender ID cannot be null for PvP battles");
  }

  private Integer validateEnergySpent(Integer energySpent) {
    if (energySpent != null && energySpent < 0) {
      throw new IllegalArgumentException("Energy spent cannot be negative");
    }
    return energySpent != null ? energySpent : 0;
  }

  private Integer validateXpReward(Integer xpReward) {
    if (xpReward != null && xpReward < 0) {
      throw new IllegalArgumentException("XP reward cannot be negative");
    }
    return xpReward;
  }

  private static void validateBattleParticipants(Long challengerId, Long defenderId) {
    Objects.requireNonNull(challengerId, "Challenger ID cannot be null");
    Objects.requireNonNull(defenderId, "Defender ID cannot be null");
    if (challengerId.equals(defenderId)) {
      throw new IllegalArgumentException("Challenger and defender cannot be the same Guardian");
    }
  }

  // Business logic methods
  public boolean canStart() {
    return this.status == BattleStatus.PENDING;
  }

  public boolean isActive() {
    return this.status == BattleStatus.IN_PROGRESS;
  }

  public boolean isCompleted() {
    return this.status == BattleStatus.COMPLETED || this.status == BattleStatus.ABANDONED;
  }

  public boolean isSoloBattle() {
    return this.type == BattleType.SOLO_CHALLENGE;
  }

  public boolean isPvPBattle() {
    return this.type == BattleType.PVP_DUEL || this.type == BattleType.COOPERATIVE_BATTLE;
  }

  public Battle start() {
    if (!canStart()) {
      throw new IllegalStateException("Battle cannot be started in current status: " + this.status);
    }

    return new Battle(
        this.id,
        this.challengerId,
        this.defenderId,
        this.type,
        BattleStatus.IN_PROGRESS,
        this.moves,
        this.totalEnergySpent,
        this.winnerId,
        this.xpReward,
        LocalDateTime.now(),
        this.completedAt,
        this.createdAt);
  }

  public Battle addMove(BattleMove move) {
    if (!isActive()) {
      throw new IllegalStateException("Cannot add moves to inactive battle");
    }

    Objects.requireNonNull(move, "Battle move cannot be null");
    List<BattleMove> updatedMoves = new ArrayList<>(this.moves);
    updatedMoves.add(move);

    return new Battle(
        this.id,
        this.challengerId,
        this.defenderId,
        this.type,
        this.status,
        updatedMoves,
        this.totalEnergySpent + move.getEnergyCost(),
        this.winnerId,
        this.xpReward,
        this.startedAt,
        this.completedAt,
        this.createdAt);
  }

  public Battle complete(Long winnerId, Integer xpReward) {
    if (!isActive()) {
      throw new IllegalStateException("Can only complete active battles");
    }

    // Validate winner is a participant
    if (winnerId != null && !isValidParticipant(winnerId)) {
      throw new IllegalArgumentException("Winner must be a battle participant");
    }

    return new Battle(
        this.id,
        this.challengerId,
        this.defenderId,
        this.type,
        BattleStatus.COMPLETED,
        this.moves,
        this.totalEnergySpent,
        winnerId,
        xpReward != null ? xpReward : 0,
        this.startedAt,
        LocalDateTime.now(),
        this.createdAt);
  }

  public Battle abandon() {
    if (isCompleted()) {
      throw new IllegalStateException("Cannot abandon completed battle");
    }

    return new Battle(
        this.id,
        this.challengerId,
        this.defenderId,
        this.type,
        BattleStatus.ABANDONED,
        this.moves,
        this.totalEnergySpent,
        null, // No winner for abandoned battles
        0, // No XP reward for abandoned battles
        this.startedAt,
        LocalDateTime.now(),
        this.createdAt);
  }

  private boolean isValidParticipant(Long guardianId) {
    return challengerId.equals(guardianId) || (defenderId != null && defenderId.equals(guardianId));
  }

  public boolean isParticipant(Long guardianId) {
    return isValidParticipant(guardianId);
  }

  public Long getOpponent(Long guardianId) {
    if (!isValidParticipant(guardianId)) {
      throw new IllegalArgumentException("Guardian is not a participant in this battle");
    }

    if (isSoloBattle()) {
      return null; // No opponent in solo battles
    }

    return challengerId.equals(guardianId) ? defenderId : challengerId;
  }

  public int getTotalMoves() {
    return moves.size();
  }

  public List<BattleMove> getMovesByGuardian(Long guardianId) {
    return moves.stream().filter(move -> move.getGuardianId().equals(guardianId)).toList();
  }

  // Getters
  public Long getId() {
    return id;
  }

  public Long getChallengerId() {
    return challengerId;
  }

  public Long getDefenderId() {
    return defenderId;
  }

  public BattleType getType() {
    return type;
  }

  public BattleStatus getStatus() {
    return status;
  }

  public List<BattleMove> getMoves() {
    return new ArrayList<>(moves);
  }

  public Integer getTotalEnergySpent() {
    return totalEnergySpent;
  }

  public Long getWinnerId() {
    return winnerId;
  }

  public Integer getXpReward() {
    return xpReward;
  }

  public LocalDateTime getStartedAt() {
    return startedAt;
  }

  public LocalDateTime getCompletedAt() {
    return completedAt;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Battle battle = (Battle) o;
    return Objects.equals(id, battle.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "Battle{"
        + "id="
        + id
        + ", challengerId="
        + challengerId
        + ", defenderId="
        + defenderId
        + ", type="
        + type
        + ", status="
        + status
        + ", totalMoves="
        + moves.size()
        + ", totalEnergySpent="
        + totalEnergySpent
        + ", winnerId="
        + winnerId
        + ", xpReward="
        + xpReward
        + '}';
  }
}

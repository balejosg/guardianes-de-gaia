package com.guardianes.battle.domain.model;

import com.guardianes.battle.domain.service.BattleEngine;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Represents the final result of a completed battle, including winner determination, statistics,
 * and XP rewards for all participants.
 */
public class BattleResult {
  private final Long battleId;
  private final Long winnerId;
  private final Integer totalMoves;
  private final Integer totalEnergySpent;
  private final LocalDateTime startedAt;
  private final LocalDateTime completedAt;
  private final List<BattleEngine.XpReward> xpRewards;

  public BattleResult(
      Long battleId,
      Long winnerId,
      Integer totalMoves,
      Integer totalEnergySpent,
      LocalDateTime startedAt,
      LocalDateTime completedAt,
      List<BattleEngine.XpReward> xpRewards) {
    this.battleId = Objects.requireNonNull(battleId, "Battle ID cannot be null");
    this.winnerId = winnerId; // Can be null for draws or abandoned battles
    this.totalMoves = Objects.requireNonNull(totalMoves, "Total moves cannot be null");
    this.totalEnergySpent =
        Objects.requireNonNull(totalEnergySpent, "Total energy spent cannot be null");
    this.startedAt = Objects.requireNonNull(startedAt, "Started at cannot be null");
    this.completedAt = Objects.requireNonNull(completedAt, "Completed at cannot be null");
    this.xpRewards = Objects.requireNonNull(xpRewards, "XP rewards cannot be null");

    // Validation
    if (totalMoves < 0) {
      throw new IllegalArgumentException("Total moves cannot be negative");
    }
    if (totalEnergySpent < 0) {
      throw new IllegalArgumentException("Total energy spent cannot be negative");
    }
    if (completedAt.isBefore(startedAt)) {
      throw new IllegalArgumentException("Completed at cannot be before started at");
    }
  }

  // Business logic methods

  /** Checks if the battle had a winner (not a draw or abandonment). */
  public boolean hasWinner() {
    return winnerId != null;
  }

  /** Gets the total duration of the battle in seconds. */
  public long getDurationSeconds() {
    return java.time.Duration.between(startedAt, completedAt).getSeconds();
  }

  /** Gets the total duration of the battle in minutes. */
  public long getDurationMinutes() {
    return getDurationSeconds() / 60;
  }

  /** Calculates the average energy spent per move. */
  public double getAverageEnergyPerMove() {
    if (totalMoves == 0) return 0.0;
    return (double) totalEnergySpent / totalMoves;
  }

  /** Gets the total XP awarded across all participants. */
  public int getTotalXpAwarded() {
    return xpRewards.stream().mapToInt(BattleEngine.XpReward::amount).sum();
  }

  /** Gets the XP reward amount for a specific Guardian. */
  public int getXpRewardAmountForGuardian(Long guardianId) {
    return xpRewards.stream()
        .filter(reward -> reward.guardianId().equals(guardianId))
        .mapToInt(BattleEngine.XpReward::amount)
        .findFirst()
        .orElse(0);
  }

  /** Checks if a specific Guardian won the battle. */
  public boolean isWinner(Long guardianId) {
    return winnerId != null && winnerId.equals(guardianId);
  }

  /** Gets the battle duration in minutes. */
  public long getBattleDurationMinutes() {
    return getDurationMinutes();
  }

  /** Gets the winner's XP reward, or null if no winner. */
  public BattleEngine.XpReward getWinnerXpReward() {
    if (winnerId == null) return null;
    return xpRewards.stream()
        .filter(reward -> reward.guardianId().equals(winnerId))
        .findFirst()
        .orElse(null);
  }

  /** Gets the number of participants in the battle. */
  public int getParticipantCount() {
    return xpRewards.size();
  }

  /** Gets the XP reward for a specific Guardian. */
  public BattleEngine.XpReward getXpRewardForGuardian(Long guardianId) {
    return xpRewards.stream()
        .filter(reward -> reward.guardianId().equals(guardianId))
        .findFirst()
        .orElse(null);
  }

  /** Gets the highest XP reward given in the battle. */
  public BattleEngine.XpReward getHighestXpReward() {
    return xpRewards.stream()
        .max((r1, r2) -> Integer.compare(r1.amount(), r2.amount()))
        .orElse(null);
  }

  /** Gets the lowest XP reward given in the battle. */
  public BattleEngine.XpReward getLowestXpReward() {
    return xpRewards.stream()
        .min((r1, r2) -> Integer.compare(r1.amount(), r2.amount()))
        .orElse(null);
  }

  /** Gets the average XP reward across all participants. */
  public double getAverageXpReward() {
    if (xpRewards.isEmpty()) return 0.0;
    return (double) getTotalXpAwarded() / xpRewards.size();
  }

  /** Checks if the battle was efficient (low moves relative to energy). */
  public boolean wasEfficient() {
    if (totalEnergySpent == 0) return false;
    return totalMoves < (totalEnergySpent / 5);
  }

  /** Checks if the battle was completed quickly (< 15 minutes). */
  public boolean wasQuick() {
    return getDurationMinutes() < 15;
  }

  /** Gets battle efficiency score (XP per energy spent). */
  public double getEfficiencyScore() {
    if (totalEnergySpent == 0) return 0.0;
    return (double) getTotalXpAwarded() / totalEnergySpent;
  }

  /** Checks if the battle was completed quickly (under average duration). */
  public boolean wasQuickBattle() {
    return getDurationMinutes() < 10; // Less than 10 minutes is considered quick
  }

  /** Checks if the battle was energy-efficient (high XP per energy ratio). */
  public boolean wasEnergyEfficient() {
    return getEfficiencyScore() > 2.0; // More than 2 XP per energy is efficient
  }

  // Record-style accessors for compatibility with test expectations
  public Long battleId() {
    return battleId;
  }

  public Long winnerId() {
    return winnerId;
  }

  public Integer totalMoves() {
    return totalMoves;
  }

  public Integer totalEnergySpent() {
    return totalEnergySpent;
  }

  public LocalDateTime startedAt() {
    return startedAt;
  }

  public LocalDateTime completedAt() {
    return completedAt;
  }

  public List<BattleEngine.XpReward> xpRewards() {
    return List.copyOf(xpRewards);
  }

  // Getters
  public Long getBattleId() {
    return battleId;
  }

  public Long getWinnerId() {
    return winnerId;
  }

  public Integer getTotalMoves() {
    return totalMoves;
  }

  public Integer getTotalEnergySpent() {
    return totalEnergySpent;
  }

  public LocalDateTime getStartedAt() {
    return startedAt;
  }

  public LocalDateTime getCompletedAt() {
    return completedAt;
  }

  public List<BattleEngine.XpReward> getXpRewards() {
    return List.copyOf(xpRewards);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BattleResult that = (BattleResult) o;
    return Objects.equals(battleId, that.battleId)
        && Objects.equals(startedAt, that.startedAt)
        && Objects.equals(completedAt, that.completedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(battleId, startedAt, completedAt);
  }

  @Override
  public String toString() {
    return "BattleResult{"
        + "battleId="
        + battleId
        + ", winnerId="
        + winnerId
        + ", totalMoves="
        + totalMoves
        + ", totalEnergySpent="
        + totalEnergySpent
        + ", duration="
        + getDurationMinutes()
        + "min"
        + ", totalXpAwarded="
        + getTotalXpAwarded()
        + ", participants="
        + xpRewards.size()
        + '}';
  }
}

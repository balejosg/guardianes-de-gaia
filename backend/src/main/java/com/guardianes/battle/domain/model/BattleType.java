package com.guardianes.battle.domain.model;

/** Represents the different types of battles available in the Guardianes de Gaia game. */
public enum BattleType {
  /** Solo battle against AI or challenges - no other Guardian required */
  SOLO_CHALLENGE("Solo Challenge", 1, 50),

  /** Player vs Player duel between two Guardians */
  PVP_DUEL("PvP Duel", 2, 100),

  /** Cooperative battle where Guardians team up against challenges */
  COOPERATIVE_BATTLE("Cooperative Battle", 2, 150);

  private final String displayName;
  private final int requiredParticipants;
  private final int baseXpReward;

  BattleType(String displayName, int requiredParticipants, int baseXpReward) {
    this.displayName = displayName;
    this.requiredParticipants = requiredParticipants;
    this.baseXpReward = baseXpReward;
  }

  /** Gets the display name for the battle type. */
  public String getDisplayName() {
    return displayName;
  }

  /** Gets the number of Guardians required to participate in this battle type. */
  public int getRequiredParticipants() {
    return requiredParticipants;
  }

  /** Gets the base XP reward for this battle type before any modifiers. */
  public int getBaseXpReward() {
    return baseXpReward;
  }

  /** Checks if this battle type requires multiple Guardians. */
  public boolean isMultiplayer() {
    return requiredParticipants > 1;
  }

  /** Checks if this battle type is a solo experience. */
  public boolean isSolo() {
    return requiredParticipants == 1;
  }

  /** Calculates the XP reward based on battle performance and energy spent. */
  public int calculateXpReward(int energySpent, boolean isWinner, double performanceMultiplier) {
    double baseReward = this.baseXpReward;

    // Energy bonus: 1 XP per energy spent
    baseReward += energySpent;

    // Winner bonus (only applies to competitive battles)
    if (isWinner && this != COOPERATIVE_BATTLE) {
      baseReward *= 1.5; // 50% winner bonus
    }

    // Performance multiplier (based on battle efficiency, combos, etc.)
    baseReward *= Math.max(0.5, Math.min(2.0, performanceMultiplier));

    return (int) Math.round(baseReward);
  }

  /** Gets the minimum energy cost to participate in this battle type. */
  public int getMinimumEnergyCost() {
    return switch (this) {
      case SOLO_CHALLENGE -> 10;
      case PVP_DUEL -> 15;
      case COOPERATIVE_BATTLE -> 20;
    };
  }

  /** Gets the maximum duration in minutes for this battle type. */
  public int getMaxDurationMinutes() {
    return switch (this) {
      case SOLO_CHALLENGE -> 15;
      case PVP_DUEL -> 30;
      case COOPERATIVE_BATTLE -> 45;
    };
  }

  @Override
  public String toString() {
    return displayName;
  }
}

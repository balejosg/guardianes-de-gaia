package com.guardianes.battle.domain.model;

/** Represents the different types of moves that can be made during a battle. */
public enum BattleMoveType {
  /** Offensive move targeting an opponent */
  ATTACK("Attack", "Deal damage to target", true, false),

  /** Defensive move to protect or reduce incoming damage */
  DEFENSE("Defense", "Protect from damage or reduce effects", false, false),

  /** Supportive move to heal or buff allies */
  SUPPORT("Support", "Heal or enhance allies", false, true),

  /** Special ability with unique effects */
  SPECIAL("Special", "Unique card ability with varied effects", true, true);

  private final String displayName;
  private final String description;
  private final boolean canTarget;
  private final boolean canHeal;

  BattleMoveType(String displayName, String description, boolean canTarget, boolean canHeal) {
    this.displayName = displayName;
    this.description = description;
    this.canTarget = canTarget;
    this.canHeal = canHeal;
  }

  /** Gets the display name for the move type. */
  public String getDisplayName() {
    return displayName;
  }

  /** Gets the description of what this move type does. */
  public String getDescription() {
    return description;
  }

  /** Checks if this move type can target other Guardians. */
  public boolean canTarget() {
    return canTarget;
  }

  /** Checks if this move type can provide healing. */
  public boolean canHeal() {
    return canHeal;
  }

  /** Checks if this move type can deal damage. */
  public boolean canDealDamage() {
    return this == ATTACK || this == SPECIAL;
  }

  /** Checks if this move type is defensive in nature. */
  public boolean isDefensive() {
    return this == DEFENSE;
  }

  /** Checks if this move type is supportive in nature. */
  public boolean isSupportive() {
    return this == SUPPORT || this == SPECIAL;
  }

  /** Checks if this move type is offensive in nature. */
  public boolean isOffensive() {
    return this == ATTACK || this == SPECIAL;
  }

  /** Gets the base energy cost for this move type. */
  public int getBaseEnergyCost() {
    return switch (this) {
      case ATTACK -> 15;
      case DEFENSE -> 8;
      case SUPPORT -> 12;
      case SPECIAL -> 25;
    };
  }

  /** Validates if the given energy cost is appropriate for this move type. */
  public boolean isValidEnergyCost(int energyCost) {
    int baseEnergy = getBaseEnergyCost();
    return energyCost >= baseEnergy && energyCost <= 50;
  }

  /** Checks compatibility with PvP battles. */
  public boolean isCompatibleWithPvP() {
    return true; // All move types work in PvP
  }

  /** Checks compatibility with solo battles. */
  public boolean isCompatibleWithSolo() {
    return this != SUPPORT; // Support less useful in solo battles
  }

  /** Checks compatibility with cooperative battles. */
  public boolean isCompatibleWithCooperative() {
    return true; // All move types work in cooperative battles
  }

  /** Gets the strategic value of this move type (1-10 scale). */
  public int getStrategicValue() {
    return switch (this) {
      case ATTACK -> 8; // High damage potential
      case DEFENSE -> 6; // Moderate strategic value
      case SUPPORT -> 7; // Good team value
      case SPECIAL -> 9; // Highest versatility
    };
  }

  /** Gets the base energy cost multiplier for this move type. */
  public double getBaseCostMultiplier() {
    return switch (this) {
      case ATTACK -> 1.0;
      case DEFENSE -> 0.8;
      case SUPPORT -> 1.2;
      case SPECIAL -> 1.5;
    };
  }

  /** Gets the effectiveness multiplier for this move type in different battle contexts. */
  public double getEffectivenessMultiplier(BattleContext context) {
    return switch (this) {
      case ATTACK -> context == BattleContext.PVP ? 1.2 : 1.0;
      case DEFENSE -> context == BattleContext.COOPERATIVE ? 1.3 : 1.0;
      case SUPPORT -> context == BattleContext.COOPERATIVE ? 1.4 : 1.1;
      case SPECIAL -> 1.0; // Special moves maintain consistent effectiveness
    };
  }

  @Override
  public String toString() {
    return displayName;
  }

  /** Battle context enum for calculating effectiveness. */
  public enum BattleContext {
    PVP,
    COOPERATIVE,
    SOLO_CHALLENGE
  }
}

package com.guardianes.battle.domain.model;

/** Represents the current status of a battle in the Guardianes de Gaia game. */
public enum BattleStatus {
  /** Battle has been created but not yet started */
  PENDING("Pending", "Battle is waiting to begin"),

  /** Battle is currently active and moves can be made */
  IN_PROGRESS("In Progress", "Battle is ongoing"),

  /** Battle has finished with a result */
  COMPLETED("Completed", "Battle has ended"),

  /** Battle was abandoned before completion */
  ABANDONED("Abandoned", "Battle was abandoned");

  private final String displayName;
  private final String description;

  BattleStatus(String displayName, String description) {
    this.displayName = displayName;
    this.description = description;
  }

  /** Gets the display name for the battle status. */
  public String getDisplayName() {
    return displayName;
  }

  /** Gets the description of what this status means. */
  public String getDescription() {
    return description;
  }

  /** Checks if the battle can be started from this status. */
  public boolean canStart() {
    return this == PENDING;
  }

  /** Checks if the battle is currently active. */
  public boolean isActive() {
    return this == IN_PROGRESS;
  }

  /** Checks if the battle has ended (completed or abandoned). */
  public boolean isFinished() {
    return this == COMPLETED || this == ABANDONED;
  }

  /** Checks if moves can be made in this status. */
  public boolean allowsMoves() {
    return this == IN_PROGRESS;
  }

  /** Gets the next valid statuses that can be transitioned to from this status. */
  public BattleStatus[] getValidTransitions() {
    return switch (this) {
      case PENDING -> new BattleStatus[] {IN_PROGRESS, ABANDONED};
      case IN_PROGRESS -> new BattleStatus[] {COMPLETED, ABANDONED};
      case COMPLETED -> new BattleStatus[] {};
      case ABANDONED -> new BattleStatus[] {};
    };
  }

  /** Checks if transition to another status is valid. */
  public boolean canTransitionTo(BattleStatus newStatus) {
    BattleStatus[] validTransitions = getValidTransitions();
    for (BattleStatus validStatus : validTransitions) {
      if (validStatus == newStatus) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return displayName;
  }
}

package com.guardianes.cards.domain.model;

/**
 * Represents the rarity levels of cards in the Guardianes de Gaia game. Higher rarity cards are
 * more difficult to obtain and typically more powerful.
 */
public enum CardRarity {
  COMMON("Común", "⚪", 1.0, 60.0),
  UNCOMMON("Poco Común", "🟢", 1.2, 25.0),
  RARE("Raro", "🔵", 1.5, 10.0),
  EPIC("Épico", "🟣", 1.8, 4.0),
  LEGENDARY("Legendario", "🟡", 2.0, 1.0);

  private final String displayName;
  private final String colorEmoji;
  private final double powerMultiplier;
  private final double dropRate; // Percentage chance of obtaining this rarity

  CardRarity(String displayName, String colorEmoji, double powerMultiplier, double dropRate) {
    this.displayName = displayName;
    this.colorEmoji = colorEmoji;
    this.powerMultiplier = powerMultiplier;
    this.dropRate = dropRate;
  }

  /** Calculates the adjusted power based on rarity multiplier. */
  public int applyPowerMultiplier(int basePower) {
    return (int) Math.round(basePower * powerMultiplier);
  }

  /** Determines if this rarity is higher than another rarity. */
  public boolean isHigherThan(CardRarity other) {
    if (other == null) return true;
    return this.ordinal() > other.ordinal();
  }

  /** Determines if this rarity is lower than another rarity. */
  public boolean isLowerThan(CardRarity other) {
    if (other == null) return false;
    return this.ordinal() < other.ordinal();
  }

  /** Gets the experience points awarded for collecting a card of this rarity. */
  public int getExperiencePoints() {
    return switch (this) {
      case COMMON -> 10;
      case UNCOMMON -> 25;
      case RARE -> 50;
      case EPIC -> 100;
      case LEGENDARY -> 200;
    };
  }

  /** Gets the energy cost modifier for cards of this rarity. */
  public double getEnergyCostModifier() {
    return switch (this) {
      case COMMON -> 1.0;
      case UNCOMMON -> 1.1;
      case RARE -> 1.2;
      case EPIC -> 1.3;
      case LEGENDARY -> 1.5;
    };
  }

  /** Gets the minimum level required to use cards of this rarity. */
  public int getMinimumLevel() {
    return switch (this) {
      case COMMON -> 1;
      case UNCOMMON -> 3;
      case RARE -> 5;
      case EPIC -> 7;
      case LEGENDARY -> 10;
    };
  }

  /** Determines the rarity based on a random roll (0-100). Used for card pack opening mechanics. */
  public static CardRarity getRarityFromRoll(double roll) {
    double cumulative = 0;

    for (CardRarity rarity : values()) {
      cumulative += rarity.dropRate;
      if (roll <= cumulative) {
        return rarity;
      }
    }

    // Fallback to common if something goes wrong
    return COMMON;
  }

  /** Gets the trade value of this rarity (used for card trading system). */
  public int getTradeValue() {
    return switch (this) {
      case COMMON -> 1;
      case UNCOMMON -> 3;
      case RARE -> 10;
      case EPIC -> 25;
      case LEGENDARY -> 100;
    };
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getColorEmoji() {
    return colorEmoji;
  }

  public double getPowerMultiplier() {
    return powerMultiplier;
  }

  public double getDropRate() {
    return dropRate;
  }

  public String getDisplayNameWithEmoji() {
    return colorEmoji + " " + displayName;
  }

  @Override
  public String toString() {
    return displayName;
  }
}

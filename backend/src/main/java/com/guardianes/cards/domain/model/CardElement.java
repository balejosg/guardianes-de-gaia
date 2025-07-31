package com.guardianes.cards.domain.model;

/**
 * Represents the four elemental types in the Guardianes de Gaia card game. Each element has
 * advantages and disadvantages against other elements, following the natural cycle: Fire > Earth >
 * Water > Air > Fire.
 */
public enum CardElement {
  FIRE("Fuego", "🔥"),
  EARTH("Tierra", "🌍"),
  WATER("Agua", "💧"),
  AIR("Aire", "🌪️");

  private final String displayName;
  private final String emoji;

  CardElement(String displayName, String emoji) {
    this.displayName = displayName;
    this.emoji = emoji;
  }

  /**
   * Determines if this element has an advantage over another element. The advantage cycle is: Fire
   * > Earth > Water > Air > Fire
   */
  public boolean hasAdvantageOver(CardElement other) {
    if (other == null) return false;

    return switch (this) {
      case FIRE -> other == EARTH;
      case EARTH -> other == WATER;
      case WATER -> other == AIR;
      case AIR -> other == FIRE;
    };
  }

  /** Determines if this element is disadvantaged against another element. */
  public boolean isDisadvantagedAgainst(CardElement other) {
    if (other == null) return false;
    return other.hasAdvantageOver(this);
  }

  /** Gets the element that this element is strong against. */
  public CardElement getAdvantageousElement() {
    return switch (this) {
      case FIRE -> EARTH;
      case EARTH -> WATER;
      case WATER -> AIR;
      case AIR -> FIRE;
    };
  }

  /** Gets the element that this element is weak against. */
  public CardElement getDisadvantageousElement() {
    return switch (this) {
      case FIRE -> AIR;
      case EARTH -> FIRE;
      case WATER -> EARTH;
      case AIR -> WATER;
    };
  }

  /**
   * Gets the damage multiplier when attacking an element. Returns 1.5 for advantage, 0.75 for
   * disadvantage, 1.0 for neutral.
   */
  public double getDamageMultiplierAgainst(CardElement other) {
    if (other == null) return 1.0;

    if (hasAdvantageOver(other)) {
      return 1.5;
    } else if (isDisadvantagedAgainst(other)) {
      return 0.75;
    } else {
      return 1.0;
    }
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getEmoji() {
    return emoji;
  }

  public String getDisplayNameWithEmoji() {
    return emoji + " " + displayName;
  }

  @Override
  public String toString() {
    return displayName;
  }
}

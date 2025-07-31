package com.guardianes.cards.domain.model;

import java.util.Objects;

/**
 * Represents the result of adding a card to a collection. Provides information about whether it was
 * a new card or a duplicate.
 */
public class CollectionResult {
  private final Card card;
  private final boolean isNewCard;
  private final int totalCount;
  private final String message;

  private CollectionResult(Card card, boolean isNewCard, int totalCount, String message) {
    this.card = Objects.requireNonNull(card, "Card cannot be null");
    this.isNewCard = isNewCard;
    this.totalCount = validateTotalCount(totalCount);
    this.message = Objects.requireNonNull(message, "Message cannot be null");
  }

  public static CollectionResult newCard(Card card) {
    String message =
        String.format(
            "¡Nueva carta añadida! %s (%s)", card.getName(), card.getRarity().getDisplayName());
    return new CollectionResult(card, true, 1, message);
  }

  public static CollectionResult duplicate(Card card, int totalCount) {
    String message = String.format("Carta duplicada: %s (Total: %d)", card.getName(), totalCount);
    return new CollectionResult(card, false, totalCount, message);
  }

  private int validateTotalCount(int totalCount) {
    if (totalCount < 1) {
      throw new IllegalArgumentException("Total count must be at least 1");
    }
    return totalCount;
  }

  /** Gets the experience points awarded for this collection result. */
  public int getExperiencePoints() {
    int baseXP = card.getRarity().getExperiencePoints();

    if (isNewCard) {
      // Bonus XP for new cards
      return (int) (baseXP * 1.5);
    } else {
      // Reduced XP for duplicates
      return Math.max(1, baseXP / 4);
    }
  }

  /** Checks if this result should trigger special effects or notifications. */
  public boolean shouldTriggerSpecialEffects() {
    return isNewCard
        && (card.getRarity() == CardRarity.EPIC || card.getRarity() == CardRarity.LEGENDARY);
  }

  /** Gets the trade value reward for this collection. */
  public int getTradeValueReward() {
    if (isNewCard) {
      return card.getRarity().getTradeValue();
    } else {
      // Reduced trade value for duplicates
      return Math.max(1, card.getRarity().getTradeValue() / 2);
    }
  }

  // Getters
  public Card getCard() {
    return card;
  }

  public boolean isNewCard() {
    return isNewCard;
  }

  public boolean isDuplicate() {
    return !isNewCard;
  }

  public int getTotalCount() {
    return totalCount;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CollectionResult that = (CollectionResult) o;
    return isNewCard == that.isNewCard
        && totalCount == that.totalCount
        && Objects.equals(card, that.card);
  }

  @Override
  public int hashCode() {
    return Objects.hash(card, isNewCard, totalCount);
  }

  @Override
  public String toString() {
    return "CollectionResult{"
        + "cardName='"
        + card.getName()
        + '\''
        + ", isNewCard="
        + isNewCard
        + ", totalCount="
        + totalCount
        + ", message='"
        + message
        + '\''
        + '}';
  }
}

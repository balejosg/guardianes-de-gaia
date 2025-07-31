package com.guardianes.cards.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/** Represents a card owned by a Guardian, including ownership metadata. */
public class CollectedCard {
  private final Card card;
  private final int count;
  private final LocalDateTime firstCollectedAt;
  private final LocalDateTime lastCollectedAt;

  public CollectedCard(
      Card card, int count, LocalDateTime firstCollectedAt, LocalDateTime lastCollectedAt) {
    this.card = Objects.requireNonNull(card, "Card cannot be null");
    this.count = validateCount(count);
    this.firstCollectedAt =
        Objects.requireNonNull(firstCollectedAt, "First collected at cannot be null");
    this.lastCollectedAt =
        Objects.requireNonNull(lastCollectedAt, "Last collected at cannot be null");
  }

  public static CollectedCard create(Card card, LocalDateTime collectionTime) {
    return new CollectedCard(card, 1, collectionTime, collectionTime);
  }

  public CollectedCard incrementCount(LocalDateTime collectionTime) {
    return new CollectedCard(this.card, this.count + 1, this.firstCollectedAt, collectionTime);
  }

  private int validateCount(int count) {
    if (count < 1) {
      throw new IllegalArgumentException("Count must be at least 1");
    }
    if (count > 999) {
      throw new IllegalArgumentException("Count cannot exceed 999");
    }
    return count;
  }

  public boolean isMultiple() {
    return count > 1;
  }

  public boolean isPremium() {
    return card.isPremium();
  }

  // Getters
  public Card getCard() {
    return card;
  }

  public int getCount() {
    return count;
  }

  public LocalDateTime getFirstCollectedAt() {
    return firstCollectedAt;
  }

  public LocalDateTime getLastCollectedAt() {
    return lastCollectedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CollectedCard that = (CollectedCard) o;
    return Objects.equals(card.getId(), that.card.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(card.getId());
  }

  @Override
  public String toString() {
    return "CollectedCard{"
        + "cardName='"
        + card.getName()
        + '\''
        + ", count="
        + count
        + ", firstCollectedAt="
        + firstCollectedAt
        + ", lastCollectedAt="
        + lastCollectedAt
        + '}';
  }
}

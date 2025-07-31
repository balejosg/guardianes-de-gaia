package com.guardianes.cards.domain.model;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Represents a Guardian's collection of cards. Tracks ownership, collection dates, and provides
 * collection statistics.
 */
public class CardCollection {
  private final Long guardianId;
  private final Map<Long, CollectedCard> cards;
  private final LocalDateTime createdAt;

  public CardCollection(Long guardianId, Map<Long, CollectedCard> cards, LocalDateTime createdAt) {
    this.guardianId = Objects.requireNonNull(guardianId, "Guardian ID cannot be null");
    this.cards = new HashMap<>(cards != null ? cards : new HashMap<>());
    this.createdAt = Objects.requireNonNull(createdAt, "Created at cannot be null");
  }

  public static CardCollection createForGuardian(Long guardianId) {
    return new CardCollection(guardianId, new HashMap<>(), LocalDateTime.now());
  }

  /** Adds a card to the collection. If the card already exists, increments the count. */
  public CollectionResult addCard(Card card) {
    if (card == null) {
      throw new IllegalArgumentException("Card cannot be null");
    }

    CollectedCard existingCard = cards.get(card.getId());
    LocalDateTime collectionTime = LocalDateTime.now();

    if (existingCard != null) {
      // Card already exists, increment count
      CollectedCard updatedCard = existingCard.incrementCount(collectionTime);
      cards.put(card.getId(), updatedCard);
      return CollectionResult.duplicate(card, updatedCard.getCount());
    } else {
      // New card
      CollectedCard newCollectedCard = CollectedCard.create(card, collectionTime);
      cards.put(card.getId(), newCollectedCard);
      return CollectionResult.newCard(card);
    }
  }

  /** Checks if the guardian owns a specific card. */
  public boolean hasCard(Long cardId) {
    return cards.containsKey(cardId);
  }

  /** Gets the number of copies of a specific card. */
  public int getCardCount(Long cardId) {
    CollectedCard collectedCard = cards.get(cardId);
    return collectedCard != null ? collectedCard.getCount() : 0;
  }

  /** Gets all cards in the collection. */
  public Collection<CollectedCard> getAllCards() {
    return new ArrayList<>(cards.values());
  }

  /** Gets cards filtered by element. */
  public Collection<CollectedCard> getCardsByElement(CardElement element) {
    return cards.values().stream()
        .filter(collectedCard -> collectedCard.getCard().getElement() == element)
        .toList();
  }

  /** Gets cards filtered by rarity. */
  public Collection<CollectedCard> getCardsByRarity(CardRarity rarity) {
    return cards.values().stream()
        .filter(collectedCard -> collectedCard.getCard().getRarity() == rarity)
        .toList();
  }

  /** Gets the total number of unique cards. */
  public int getUniqueCardCount() {
    return cards.size();
  }

  /** Gets the total number of all cards (including duplicates). */
  public int getTotalCardCount() {
    return cards.values().stream().mapToInt(CollectedCard::getCount).sum();
  }

  /** Calculates collection completion percentage based on available cards. */
  public double getCompletionPercentage(int totalAvailableCards) {
    if (totalAvailableCards <= 0) return 0.0;
    return (double) getUniqueCardCount() / totalAvailableCards * 100.0;
  }

  /** Gets collection statistics by rarity. */
  public Map<CardRarity, Integer> getCardCountsByRarity() {
    Map<CardRarity, Integer> counts = new EnumMap<>(CardRarity.class);

    for (CardRarity rarity : CardRarity.values()) {
      counts.put(rarity, 0);
    }

    for (CollectedCard collectedCard : cards.values()) {
      CardRarity rarity = collectedCard.getCard().getRarity();
      counts.put(rarity, counts.get(rarity) + collectedCard.getCount());
    }

    return counts;
  }

  /** Gets collection statistics by element. */
  public Map<CardElement, Integer> getCardCountsByElement() {
    Map<CardElement, Integer> counts = new EnumMap<>(CardElement.class);

    for (CardElement element : CardElement.values()) {
      counts.put(element, 0);
    }

    for (CollectedCard collectedCard : cards.values()) {
      CardElement element = collectedCard.getCard().getElement();
      counts.put(element, counts.get(element) + collectedCard.getCount());
    }

    return counts;
  }

  /** Gets the most recently collected cards. */
  public List<CollectedCard> getRecentlyCollected(int limit) {
    return cards.values().stream()
        .sorted((a, b) -> b.getLastCollectedAt().compareTo(a.getLastCollectedAt()))
        .limit(limit)
        .toList();
  }

  /** Gets the total trade value of the collection. */
  public int getTotalTradeValue() {
    return cards.values().stream()
        .mapToInt(
            collectedCard ->
                collectedCard.getCard().getRarity().getTradeValue() * collectedCard.getCount())
        .sum();
  }

  /** Checks if the collection has at least one card of each element. */
  public boolean hasElementalBalance() {
    Set<CardElement> elements =
        cards.values().stream()
            .map(collectedCard -> collectedCard.getCard().getElement())
            .collect(HashSet::new, HashSet::add, HashSet::addAll);

    return elements.size() == CardElement.values().length;
  }

  /** Gets the rarest card in the collection. */
  public Optional<CollectedCard> getRarestCard() {
    return cards.values().stream()
        .max(Comparator.comparing(collectedCard -> collectedCard.getCard().getRarity()));
  }

  // Getters
  public Long getGuardianId() {
    return guardianId;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CardCollection that = (CardCollection) o;
    return Objects.equals(guardianId, that.guardianId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(guardianId);
  }

  @Override
  public String toString() {
    return "CardCollection{"
        + "guardianId="
        + guardianId
        + ", uniqueCards="
        + getUniqueCardCount()
        + ", totalCards="
        + getTotalCardCount()
        + ", createdAt="
        + createdAt
        + '}';
  }
}

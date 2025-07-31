package com.guardianes.cards.domain.repository;

import com.guardianes.cards.domain.model.CardCollection;
import com.guardianes.cards.domain.model.CardElement;
import com.guardianes.cards.domain.model.CardRarity;
import com.guardianes.cards.domain.model.CollectedCard;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Domain repository interface for CardCollection entities. */
public interface CardCollectionRepository {

  /**
   * Saves a card collection to the repository.
   *
   * @param collection the collection to save
   * @return the saved collection
   */
  CardCollection save(CardCollection collection);

  /**
   * Finds a collection by guardian ID.
   *
   * @param guardianId the guardian ID
   * @return the collection if found
   */
  Optional<CardCollection> findByGuardianId(Long guardianId);

  /**
   * Finds or creates a collection for a guardian.
   *
   * @param guardianId the guardian ID
   * @return the existing or newly created collection
   */
  CardCollection findOrCreateByGuardianId(Long guardianId);

  /**
   * Adds a card to a guardian's collection.
   *
   * @param guardianId the guardian ID
   * @param cardId the card ID
   * @param count the number of copies to add
   * @return the updated collection
   */
  CardCollection addCardToCollection(Long guardianId, Long cardId, int count);

  /**
   * Checks if a guardian owns a specific card.
   *
   * @param guardianId the guardian ID
   * @param cardId the card ID
   * @return true if the guardian owns the card
   */
  boolean guardianOwnsCard(Long guardianId, Long cardId);

  /**
   * Gets the count of a specific card in a guardian's collection.
   *
   * @param guardianId the guardian ID
   * @param cardId the card ID
   * @return the count of the card (0 if not owned)
   */
  int getCardCount(Long guardianId, Long cardId);

  /**
   * Gets all cards in a guardian's collection.
   *
   * @param guardianId the guardian ID
   * @return list of collected cards
   */
  List<CollectedCard> getGuardianCards(Long guardianId);

  /**
   * Gets cards in a guardian's collection filtered by element.
   *
   * @param guardianId the guardian ID
   * @param element the element to filter by
   * @return list of collected cards with the specified element
   */
  List<CollectedCard> getGuardianCardsByElement(Long guardianId, CardElement element);

  /**
   * Gets cards in a guardian's collection filtered by rarity.
   *
   * @param guardianId the guardian ID
   * @param rarity the rarity to filter by
   * @return list of collected cards with the specified rarity
   */
  List<CollectedCard> getGuardianCardsByRarity(Long guardianId, CardRarity rarity);

  /**
   * Gets the total number of unique cards in a guardian's collection.
   *
   * @param guardianId the guardian ID
   * @return count of unique cards
   */
  int getUniqueCardCount(Long guardianId);

  /**
   * Gets the total number of all cards (including duplicates) in a guardian's collection.
   *
   * @param guardianId the guardian ID
   * @return total count of cards
   */
  int getTotalCardCount(Long guardianId);

  /**
   * Gets collection completion percentage for a guardian.
   *
   * @param guardianId the guardian ID
   * @param totalAvailableCards total number of available cards
   * @return completion percentage (0-100)
   */
  double getCompletionPercentage(Long guardianId, int totalAvailableCards);

  /**
   * Gets card count statistics by rarity for a guardian.
   *
   * @param guardianId the guardian ID
   * @return map of rarity to count
   */
  Map<CardRarity, Integer> getCardCountsByRarity(Long guardianId);

  /**
   * Gets card count statistics by element for a guardian.
   *
   * @param guardianId the guardian ID
   * @return map of element to count
   */
  Map<CardElement, Integer> getCardCountsByElement(Long guardianId);

  /**
   * Gets the most recently collected cards for a guardian.
   *
   * @param guardianId the guardian ID
   * @param limit maximum number of cards to return
   * @return list of recently collected cards
   */
  List<CollectedCard> getRecentlyCollected(Long guardianId, int limit);

  /**
   * Gets the total trade value of a guardian's collection.
   *
   * @param guardianId the guardian ID
   * @return total trade value
   */
  int getTotalTradeValue(Long guardianId);

  /**
   * Checks if a guardian has elemental balance (at least one card of each element).
   *
   * @param guardianId the guardian ID
   * @return true if the collection has elemental balance
   */
  boolean hasElementalBalance(Long guardianId);

  /**
   * Gets the rarest card in a guardian's collection.
   *
   * @param guardianId the guardian ID
   * @return the rarest collected card if any
   */
  Optional<CollectedCard> getRarestCard(Long guardianId);

  /**
   * Removes cards from a guardian's collection (for trading, consuming, etc.).
   *
   * @param guardianId the guardian ID
   * @param cardId the card ID
   * @param count the number of copies to remove
   * @return the updated collection
   */
  CardCollection removeCardsFromCollection(Long guardianId, Long cardId, int count);

  /**
   * Updates a collection.
   *
   * @param collection the collection to update
   * @return the updated collection
   */
  CardCollection update(CardCollection collection);

  /**
   * Deletes a collection by guardian ID.
   *
   * @param guardianId the guardian ID
   */
  void deleteByGuardianId(Long guardianId);

  /**
   * Finds all collections (for admin purposes).
   *
   * @return list of all collections
   */
  List<CardCollection> findAll();

  /**
   * Gets guardians who own a specific card.
   *
   * @param cardId the card ID
   * @return list of guardian IDs who own the card
   */
  List<Long> findGuardiansOwningCard(Long cardId);

  /**
   * Gets the most popular cards (owned by most guardians).
   *
   * @param limit maximum number of cards to return
   * @return list of most popular card IDs with their ownership count
   */
  Map<Long, Integer> getMostPopularCards(int limit);

  /**
   * Gets collection statistics for all guardians.
   *
   * @return map of guardian ID to collection size
   */
  Map<Long, Integer> getCollectionSizeStatistics();
}

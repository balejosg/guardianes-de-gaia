package com.guardianes.cards.domain.repository;

import com.guardianes.cards.domain.model.Card;
import com.guardianes.cards.domain.model.CardElement;
import com.guardianes.cards.domain.model.CardRarity;
import java.util.List;
import java.util.Optional;

/** Domain repository interface for Card entities. */
public interface CardRepository {

  /**
   * Saves a card to the repository.
   *
   * @param card the card to save
   * @return the saved card with generated ID
   */
  Card save(Card card);

  /**
   * Finds a card by its ID.
   *
   * @param id the card ID
   * @return the card if found
   */
  Optional<Card> findById(Long id);

  /**
   * Finds a card by its QR code.
   *
   * @param qrCode the QR code
   * @return the card if found
   */
  Optional<Card> findByQrCode(String qrCode);

  /**
   * Finds a card by its NFC code.
   *
   * @param nfcCode the NFC code
   * @return the card if found
   */
  Optional<Card> findByNfcCode(String nfcCode);

  /**
   * Finds all cards.
   *
   * @return list of all active cards
   */
  List<Card> findAll();

  /**
   * Finds all active cards.
   *
   * @return list of all active cards
   */
  List<Card> findAllActive();

  /**
   * Finds cards by element.
   *
   * @param element the element to filter by
   * @return list of cards with the specified element
   */
  List<Card> findByElement(CardElement element);

  /**
   * Finds cards by rarity.
   *
   * @param rarity the rarity to filter by
   * @return list of cards with the specified rarity
   */
  List<Card> findByRarity(CardRarity rarity);

  /**
   * Finds cards by element and rarity.
   *
   * @param element the element to filter by
   * @param rarity the rarity to filter by
   * @return list of cards with the specified element and rarity
   */
  List<Card> findByElementAndRarity(CardElement element, CardRarity rarity);

  /**
   * Counts the total number of active cards.
   *
   * @return total count of active cards
   */
  long countActive();

  /**
   * Counts cards by element.
   *
   * @param element the element to count
   * @return count of cards with the specified element
   */
  long countByElement(CardElement element);

  /**
   * Counts cards by rarity.
   *
   * @param rarity the rarity to count
   * @return count of cards with the specified rarity
   */
  long countByRarity(CardRarity rarity);

  /**
   * Finds cards by name (case-insensitive partial match).
   *
   * @param name the name to search for
   * @return list of cards matching the name
   */
  List<Card> findByNameContainingIgnoreCase(String name);

  /**
   * Checks if a QR code already exists.
   *
   * @param qrCode the QR code to check
   * @return true if the QR code exists
   */
  boolean existsByQrCode(String qrCode);

  /**
   * Checks if an NFC code already exists.
   *
   * @param nfcCode the NFC code to check
   * @return true if the NFC code exists
   */
  boolean existsByNfcCode(String nfcCode);

  /**
   * Deletes a card by ID (soft delete - marks as inactive).
   *
   * @param id the card ID
   */
  void deleteById(Long id);

  /**
   * Updates a card.
   *
   * @param card the card to update
   * @return the updated card
   */
  Card update(Card card);

  /**
   * Finds cards suitable for a specific guardian level.
   *
   * @param guardianLevel the guardian level
   * @return list of cards suitable for the guardian level
   */
  List<Card> findSuitableForLevel(int guardianLevel);

  /**
   * Finds premium cards (cards with NFC codes).
   *
   * @return list of premium cards
   */
  List<Card> findPremiumCards();

  /**
   * Finds cards within a power range.
   *
   * @param minPower minimum total power
   * @param maxPower maximum total power
   * @return list of cards within the power range
   */
  List<Card> findByPowerRange(int minPower, int maxPower);
}

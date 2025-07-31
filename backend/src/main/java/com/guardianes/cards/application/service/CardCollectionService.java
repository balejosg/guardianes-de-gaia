package com.guardianes.cards.application.service;

import com.guardianes.cards.domain.model.*;
import com.guardianes.cards.domain.repository.CardCollectionRepository;
import com.guardianes.cards.domain.repository.CardRepository;
import com.guardianes.cards.domain.service.QRValidationService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CardCollectionService {

  private final CardRepository cardRepository;
  private final CardCollectionRepository collectionRepository;
  private final QRValidationService qrValidationService;

  public CardCollectionService(
      CardRepository cardRepository,
      CardCollectionRepository collectionRepository,
      QRValidationService qrValidationService) {
    this.cardRepository = cardRepository;
    this.collectionRepository = collectionRepository;
    this.qrValidationService = qrValidationService;
  }

  /** Scans and validates a QR code, then adds the card to the guardian's collection if valid. */
  public CardScanResult scanQRCode(Long guardianId, String qrCode) {
    // Validate QR code format
    QRValidationService.QRValidationResult validationResult =
        qrValidationService.validateQRCode(qrCode);
    if (!validationResult.isValid()) {
      return CardScanResult.invalidQR(validationResult.getErrorMessage());
    }

    // Find card by QR code
    Optional<Card> cardOpt = cardRepository.findByQrCode(validationResult.getCleanCode());
    if (cardOpt.isEmpty()) {
      return CardScanResult.cardNotFound("Card not found for QR code: " + qrCode);
    }

    Card card = cardOpt.get();
    if (!card.isActive()) {
      return CardScanResult.cardInactive("Card is no longer active: " + card.getName());
    }

    // Check if guardian already owns this card
    boolean alreadyOwned = collectionRepository.guardianOwnsCard(guardianId, card.getId());

    // Add card to collection
    collectionRepository.addCardToCollection(guardianId, card.getId(), 1);

    if (alreadyOwned) {
      int newCount = collectionRepository.getCardCount(guardianId, card.getId());
      return CardScanResult.duplicate(card, newCount);
    } else {
      return CardScanResult.newCard(card);
    }
  }

  /** Gets a guardian's complete card collection. */
  public CardCollection getGuardianCollection(Long guardianId) {
    return collectionRepository.findOrCreateByGuardianId(guardianId);
  }

  /** Gets cards in a guardian's collection. */
  public List<CollectedCard> getGuardianCards(Long guardianId) {
    return collectionRepository.getGuardianCards(guardianId);
  }

  /** Gets cards filtered by element. */
  public List<CollectedCard> getGuardianCardsByElement(Long guardianId, CardElement element) {
    return collectionRepository.getGuardianCardsByElement(guardianId, element);
  }

  /** Gets cards filtered by rarity. */
  public List<CollectedCard> getGuardianCardsByRarity(Long guardianId, CardRarity rarity) {
    return collectionRepository.getGuardianCardsByRarity(guardianId, rarity);
  }

  /** Gets collection statistics for a guardian. */
  public CollectionStatistics getCollectionStatistics(Long guardianId) {
    int totalAvailableCards = (int) cardRepository.countActive();

    return new CollectionStatistics(
        collectionRepository.getUniqueCardCount(guardianId),
        collectionRepository.getTotalCardCount(guardianId),
        collectionRepository.getCompletionPercentage(guardianId, totalAvailableCards),
        collectionRepository.getCardCountsByElement(guardianId),
        collectionRepository.getCardCountsByRarity(guardianId),
        collectionRepository.getTotalTradeValue(guardianId),
        collectionRepository.hasElementalBalance(guardianId));
  }

  /** Gets recently collected cards. */
  public List<CollectedCard> getRecentlyCollected(Long guardianId, int limit) {
    return collectionRepository.getRecentlyCollected(guardianId, Math.min(limit, 50));
  }

  /** Gets the rarest card in a guardian's collection. */
  public Optional<CollectedCard> getRarestCard(Long guardianId) {
    return collectionRepository.getRarestCard(guardianId);
  }

  /** Searches for cards by name. */
  public List<Card> searchCards(String name) {
    if (name == null || name.trim().isEmpty()) {
      return cardRepository.findAllActive();
    }
    return cardRepository.findByNameContainingIgnoreCase(name.trim());
  }

  /** Gets all cards of a specific element. */
  public List<Card> getCardsByElement(CardElement element) {
    return cardRepository.findByElement(element);
  }

  /** Gets all cards of a specific rarity. */
  public List<Card> getCardsByRarity(CardRarity rarity) {
    return cardRepository.findByRarity(rarity);
  }

  /** Gets a specific card by ID. */
  public Optional<Card> getCard(Long cardId) {
    return cardRepository.findById(cardId);
  }

  // Inner classes for response DTOs
  public static class CardScanResult {
    private final boolean success;
    private final String message;
    private final Card card;
    private final Integer count;
    private final boolean isNew;

    private CardScanResult(
        boolean success, String message, Card card, Integer count, boolean isNew) {
      this.success = success;
      this.message = message;
      this.card = card;
      this.count = count;
      this.isNew = isNew;
    }

    public static CardScanResult invalidQR(String message) {
      return new CardScanResult(false, message, null, null, false);
    }

    public static CardScanResult cardNotFound(String message) {
      return new CardScanResult(false, message, null, null, false);
    }

    public static CardScanResult cardInactive(String message) {
      return new CardScanResult(false, message, null, null, false);
    }

    public static CardScanResult newCard(Card card) {
      return new CardScanResult(true, "New card collected!", card, 1, true);
    }

    public static CardScanResult duplicate(Card card, int count) {
      return new CardScanResult(true, "Card already owned - count increased!", card, count, false);
    }

    // Getters
    public boolean isSuccess() {
      return success;
    }

    public String getMessage() {
      return message;
    }

    public Card getCard() {
      return card;
    }

    public Integer getCount() {
      return count;
    }

    public boolean isNew() {
      return isNew;
    }
  }

  public static class CollectionStatistics {
    private final int uniqueCardCount;
    private final int totalCardCount;
    private final double completionPercentage;
    private final Map<CardElement, Integer> cardCountsByElement;
    private final Map<CardRarity, Integer> cardCountsByRarity;
    private final int totalTradeValue;
    private final boolean hasElementalBalance;

    public CollectionStatistics(
        int uniqueCardCount,
        int totalCardCount,
        double completionPercentage,
        Map<CardElement, Integer> cardCountsByElement,
        Map<CardRarity, Integer> cardCountsByRarity,
        int totalTradeValue,
        boolean hasElementalBalance) {
      this.uniqueCardCount = uniqueCardCount;
      this.totalCardCount = totalCardCount;
      this.completionPercentage = completionPercentage;
      this.cardCountsByElement = cardCountsByElement;
      this.cardCountsByRarity = cardCountsByRarity;
      this.totalTradeValue = totalTradeValue;
      this.hasElementalBalance = hasElementalBalance;
    }

    // Getters
    public int getUniqueCardCount() {
      return uniqueCardCount;
    }

    public int getTotalCardCount() {
      return totalCardCount;
    }

    public double getCompletionPercentage() {
      return completionPercentage;
    }

    public Map<CardElement, Integer> getCardCountsByElement() {
      return cardCountsByElement;
    }

    public Map<CardRarity, Integer> getCardCountsByRarity() {
      return cardCountsByRarity;
    }

    public int getTotalTradeValue() {
      return totalTradeValue;
    }

    public boolean hasElementalBalance() {
      return hasElementalBalance;
    }
  }
}

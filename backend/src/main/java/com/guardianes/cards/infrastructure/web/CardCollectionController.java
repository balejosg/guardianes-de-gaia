package com.guardianes.cards.infrastructure.web;

import com.guardianes.cards.application.service.CardCollectionService;
import com.guardianes.cards.domain.model.*;
import com.guardianes.shared.infrastructure.web.ApiResponse;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
public class CardCollectionController {

  private final CardCollectionService cardCollectionService;

  public CardCollectionController(CardCollectionService cardCollectionService) {
    this.cardCollectionService = cardCollectionService;
  }

  /** Scan a QR code and attempt to collect the card. POST /api/cards/scan */
  @PostMapping("/scan")
  public ResponseEntity<ApiResponse<CardCollectionService.CardScanResult>> scanQRCode(
      @RequestBody QRScanRequest request) {

    if (request.getGuardianId() == null) {
      return ResponseEntity.badRequest().body(ApiResponse.error("Guardian ID is required"));
    }

    if (request.getQrCode() == null || request.getQrCode().trim().isEmpty()) {
      return ResponseEntity.badRequest().body(ApiResponse.error("QR code is required"));
    }

    try {
      CardCollectionService.CardScanResult result =
          cardCollectionService.scanQRCode(request.getGuardianId(), request.getQrCode());

      if (result.isSuccess()) {
        return ResponseEntity.ok(ApiResponse.success(result));
      } else {
        return ResponseEntity.badRequest().body(ApiResponse.error(result.getMessage()));
      }
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Failed to scan QR code: " + e.getMessage()));
    }
  }

  /** Get a guardian's card collection. GET /api/cards/collection/{guardianId} */
  @GetMapping("/collection/{guardianId}")
  public ResponseEntity<ApiResponse<CardCollection>> getCollection(@PathVariable Long guardianId) {

    try {
      CardCollection collection = cardCollectionService.getGuardianCollection(guardianId);
      return ResponseEntity.ok(ApiResponse.success(collection));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Failed to get collection: " + e.getMessage()));
    }
  }

  /** Get cards in a guardian's collection. GET /api/cards/collection/{guardianId}/cards */
  @GetMapping("/collection/{guardianId}/cards")
  public ResponseEntity<ApiResponse<List<CollectedCard>>> getCollectionCards(
      @PathVariable Long guardianId,
      @RequestParam(required = false) String element,
      @RequestParam(required = false) String rarity) {

    try {
      List<CollectedCard> cards;

      if (element != null && !element.trim().isEmpty()) {
        CardElement cardElement = CardElement.valueOf(element.toUpperCase());
        cards = cardCollectionService.getGuardianCardsByElement(guardianId, cardElement);
      } else if (rarity != null && !rarity.trim().isEmpty()) {
        CardRarity cardRarity = CardRarity.valueOf(rarity.toUpperCase());
        cards = cardCollectionService.getGuardianCardsByRarity(guardianId, cardRarity);
      } else {
        cards = cardCollectionService.getGuardianCards(guardianId);
      }

      return ResponseEntity.ok(ApiResponse.success(cards));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Invalid element or rarity: " + e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Failed to get collection cards: " + e.getMessage()));
    }
  }

  /** Get collection statistics for a guardian. GET /api/cards/collection/{guardianId}/stats */
  @GetMapping("/collection/{guardianId}/stats")
  public ResponseEntity<ApiResponse<CardCollectionService.CollectionStatistics>> getCollectionStats(
      @PathVariable Long guardianId) {

    try {
      CardCollectionService.CollectionStatistics stats =
          cardCollectionService.getCollectionStatistics(guardianId);
      return ResponseEntity.ok(ApiResponse.success(stats));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Failed to get collection statistics: " + e.getMessage()));
    }
  }

  /** Get recently collected cards. GET /api/cards/collection/{guardianId}/recent */
  @GetMapping("/collection/{guardianId}/recent")
  public ResponseEntity<ApiResponse<List<CollectedCard>>> getRecentlyCollected(
      @PathVariable Long guardianId, @RequestParam(defaultValue = "10") int limit) {

    try {
      List<CollectedCard> recentCards =
          cardCollectionService.getRecentlyCollected(guardianId, limit);
      return ResponseEntity.ok(ApiResponse.success(recentCards));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Failed to get recently collected cards: " + e.getMessage()));
    }
  }

  /**
   * Get the rarest card in a guardian's collection. GET /api/cards/collection/{guardianId}/rarest
   */
  @GetMapping("/collection/{guardianId}/rarest")
  public ResponseEntity<ApiResponse<CollectedCard>> getRarestCard(@PathVariable Long guardianId) {

    try {
      Optional<CollectedCard> rarestCard = cardCollectionService.getRarestCard(guardianId);

      if (rarestCard.isPresent()) {
        return ResponseEntity.ok(ApiResponse.success(rarestCard.get()));
      } else {
        return ResponseEntity.ok(ApiResponse.success(null, "No cards in collection"));
      }
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Failed to get rarest card: " + e.getMessage()));
    }
  }

  /** Search for cards by name. GET /api/cards/search */
  @GetMapping("/search")
  public ResponseEntity<ApiResponse<List<Card>>> searchCards(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String element,
      @RequestParam(required = false) String rarity) {

    try {
      List<Card> cards;

      if (element != null && !element.trim().isEmpty()) {
        CardElement cardElement = CardElement.valueOf(element.toUpperCase());
        cards = cardCollectionService.getCardsByElement(cardElement);
      } else if (rarity != null && !rarity.trim().isEmpty()) {
        CardRarity cardRarity = CardRarity.valueOf(rarity.toUpperCase());
        cards = cardCollectionService.getCardsByRarity(cardRarity);
      } else {
        cards = cardCollectionService.searchCards(name);
      }

      return ResponseEntity.ok(ApiResponse.success(cards));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Invalid element or rarity: " + e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Failed to search cards: " + e.getMessage()));
    }
  }

  /** Get a specific card by ID. GET /api/cards/{cardId} */
  @GetMapping("/{cardId}")
  public ResponseEntity<ApiResponse<Card>> getCard(@PathVariable Long cardId) {

    try {
      Optional<Card> card = cardCollectionService.getCard(cardId);

      if (card.isPresent()) {
        return ResponseEntity.ok(ApiResponse.success(card.get()));
      } else {
        return ResponseEntity.notFound().build();
      }
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Failed to get card: " + e.getMessage()));
    }
  }

  // Request DTOs
  public static class QRScanRequest {
    private Long guardianId;
    private String qrCode;

    public QRScanRequest() {}

    public QRScanRequest(Long guardianId, String qrCode) {
      this.guardianId = guardianId;
      this.qrCode = qrCode;
    }

    public Long getGuardianId() {
      return guardianId;
    }

    public void setGuardianId(Long guardianId) {
      this.guardianId = guardianId;
    }

    public String getQrCode() {
      return qrCode;
    }

    public void setQrCode(String qrCode) {
      this.qrCode = qrCode;
    }
  }
}

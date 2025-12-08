package com.guardianes.cards.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.guardianes.cards.domain.model.*;
import com.guardianes.cards.domain.repository.CardCollectionRepository;
import com.guardianes.cards.domain.repository.CardRepository;
import com.guardianes.cards.domain.service.QRValidationService;
import com.guardianes.cards.domain.service.QRValidationService.QRValidationResult;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CardCollectionServiceTest {

  @Mock private CardRepository cardRepository;
  @Mock private CardCollectionRepository collectionRepository;
  @Mock private QRValidationService qrValidationService;

  private CardCollectionService cardCollectionService;
  private Card testCard;

  @BeforeEach
  void setUp() {
    cardCollectionService =
        new CardCollectionService(cardRepository, collectionRepository, qrValidationService);

    testCard =
        new Card(
            100L,
            "Test Card",
            "Description",
            CardElement.FIRE,
            CardRarity.COMMON,
            10,
            10,
            5,
            null,
            "FIRESTRIKE000001",
            null,
            LocalDateTime.now(),
            true);
  }

  @Nested
  @DisplayName("Scan QR Code Tests")
  class ScanQRCodeTests {

    @Test
    @DisplayName("should return invalid result when QR code is invalid")
    void shouldReturnInvalidResultWhenQrCodeIsInvalid() {
      // Given
      String invalidQr = "INVALID";
      when(qrValidationService.validateQRCode(invalidQr))
          .thenReturn(QRValidationResult.invalid("Format error"));

      // When
      CardCollectionService.CardScanResult result = cardCollectionService.scanQRCode(1L, invalidQr);

      // Then
      assertFalse(result.isSuccess());
      assertEquals("Format error", result.getMessage());
      verify(cardRepository, never()).findByQrCode(any());
    }

    @Test
    @DisplayName("should return card not found when QR code does not match any card")
    void shouldReturnCardNotFoundWhenQrCodeDoesNotMatch() {
      // Given
      String qrCode = "GDGFCMISSINGCARD";
      when(qrValidationService.validateQRCode(qrCode)).thenReturn(QRValidationResult.valid(qrCode));
      when(cardRepository.findByQrCode(qrCode)).thenReturn(Optional.empty());

      // When
      CardCollectionService.CardScanResult result = cardCollectionService.scanQRCode(1L, qrCode);

      // Then
      assertFalse(result.isSuccess());
      assertTrue(result.getMessage().contains("Card not found"));
    }

    @Test
    @DisplayName("should return card inactive when card is not active")
    void shouldReturnCardInactiveWhenCardIsNotActive() {
      // Given
      String qrCode = "FIRESTRIKE000001";
      Card inactiveCard =
          new Card(
              101L,
              "Inactive Card",
              "Desc",
              CardElement.WATER,
              CardRarity.COMMON,
              10,
              10,
              5,
              null,
              qrCode,
              null,
              LocalDateTime.now(),
              false); // Active = false

      when(qrValidationService.validateQRCode(qrCode)).thenReturn(QRValidationResult.valid(qrCode));
      when(cardRepository.findByQrCode(qrCode)).thenReturn(Optional.of(inactiveCard));

      // When
      CardCollectionService.CardScanResult result = cardCollectionService.scanQRCode(1L, qrCode);

      // Then
      assertFalse(result.isSuccess());
      assertTrue(result.getMessage().toLowerCase().contains("no longer active"));
    }

    @Test
    @DisplayName("should add new card to collection when valid and not owned")
    void shouldAddNewCardToCollection() {
      // Given
      String qrCode = "FIRESTRIKE000001";
      when(qrValidationService.validateQRCode(qrCode)).thenReturn(QRValidationResult.valid(qrCode));
      when(cardRepository.findByQrCode(qrCode)).thenReturn(Optional.of(testCard));
      when(collectionRepository.guardianOwnsCard(1L, testCard.getId())).thenReturn(false);

      // When
      CardCollectionService.CardScanResult result = cardCollectionService.scanQRCode(1L, qrCode);

      // Then
      assertTrue(result.isSuccess());
      assertTrue(result.isNew());
      assertEquals(testCard, result.getCard());
      verify(collectionRepository).addCardToCollection(1L, testCard.getId(), 1);
    }

    @Test
    @DisplayName("should increment count when card is already owned")
    void shouldIncrementCountWhenCardIsAlreadyOwned() {
      // Given
      String qrCode = "FIRESTRIKE000001";
      when(qrValidationService.validateQRCode(qrCode)).thenReturn(QRValidationResult.valid(qrCode));
      when(cardRepository.findByQrCode(qrCode)).thenReturn(Optional.of(testCard));
      when(collectionRepository.guardianOwnsCard(1L, testCard.getId())).thenReturn(true);
      when(collectionRepository.getCardCount(1L, testCard.getId())).thenReturn(5); // Now 5

      // When
      CardCollectionService.CardScanResult result = cardCollectionService.scanQRCode(1L, qrCode);

      // Then
      assertTrue(result.isSuccess());
      assertFalse(result.isNew()); // Not new
      assertEquals(5, result.getCount());
      verify(collectionRepository).addCardToCollection(1L, testCard.getId(), 1);
    }
  }

  @Nested
  @DisplayName("Collection Statistics Tests")
  class CollectionStatisticsTests {

    @Test
    @DisplayName("should return correct collection statistics")
    void shouldReturnCorrectCollectionStatistics() {
      // Given
      Long guardianId = 1L;
      when(cardRepository.countActive()).thenReturn(100L);
      when(collectionRepository.getUniqueCardCount(guardianId)).thenReturn(20);
      when(collectionRepository.getTotalCardCount(guardianId)).thenReturn(50);
      when(collectionRepository.getCompletionPercentage(guardianId, 100)).thenReturn(20.0);
      when(collectionRepository.getTotalTradeValue(guardianId)).thenReturn(500);
      when(collectionRepository.hasElementalBalance(guardianId)).thenReturn(true);

      Map<CardElement, Integer> elemCounts = new HashMap<>();
      elemCounts.put(CardElement.FIRE, 10);
      when(collectionRepository.getCardCountsByElement(guardianId)).thenReturn(elemCounts);

      Map<CardRarity, Integer> rarityCounts = new HashMap<>();
      rarityCounts.put(CardRarity.COMMON, 30);
      when(collectionRepository.getCardCountsByRarity(guardianId)).thenReturn(rarityCounts);

      // When
      CardCollectionService.CollectionStatistics stats =
          cardCollectionService.getCollectionStatistics(guardianId);

      // Then
      assertEquals(20, stats.getUniqueCardCount());
      assertEquals(50, stats.getTotalCardCount());
      assertEquals(20.0, stats.getCompletionPercentage());
      assertEquals(500, stats.getTotalTradeValue());
      assertTrue(stats.hasElementalBalance());
      assertEquals(10, stats.getCardCountsByElement().get(CardElement.FIRE));
      assertEquals(30, stats.getCardCountsByRarity().get(CardRarity.COMMON));
    }
  }

  @Nested
  @DisplayName("Search and Retrieval Tests")
  class SearchRetrievalTests {
    @Test
    @DisplayName("should search cards by name")
    void shouldSearchCardsByName() {
      // Given
      String query = "Fire";
      when(cardRepository.findByNameContainingIgnoreCase("Fire"))
          .thenReturn(Collections.singletonList(testCard));

      // When
      var results = cardCollectionService.searchCards(query);

      // Then
      assertEquals(1, results.size());
      assertEquals(testCard, results.get(0));
    }

    @Test
    @DisplayName("should return all active cards when search query is empty")
    void shouldReturnAllActiveCardsWhenSearchQueryIsEmpty() {
      // Given
      when(cardRepository.findAllActive()).thenReturn(Collections.singletonList(testCard));

      // When
      var results = cardCollectionService.searchCards("  ");

      // Then
      assertEquals(1, results.size());
      assertEquals(testCard, results.get(0));
    }
  }
}

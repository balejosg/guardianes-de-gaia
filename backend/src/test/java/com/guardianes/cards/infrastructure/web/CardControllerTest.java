package com.guardianes.cards.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.guardianes.cards.application.service.CardCollectionService;
import com.guardianes.cards.domain.model.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class CardControllerTest {

  @Mock private CardCollectionService cardCollectionService;

  private CardController cardController;

  private Card testCard;

  @BeforeEach
  void setUp() {
    cardController = new CardController(cardCollectionService);

    testCard =
        new Card(
            1L,
            "Fire Dragon",
            "A powerful fire-breathing dragon",
            CardElement.FIRE,
            CardRarity.RARE,
            80,
            60,
            5,
            "https://example.com/fire-dragon.jpg",
            "ABC123DEF4567890",
            null,
            LocalDateTime.now(),
            true);
  }

  @Test
  public void shouldScanQRCodeSuccessfully() {
    // Given
    Long guardianId = 1L;
    String qrCode = "ABC123DEF4567890";
    CardCollectionService.CardScanResult successResult =
        CardCollectionService.CardScanResult.newCard(testCard);

    when(cardCollectionService.scanQRCode(eq(guardianId), eq(qrCode))).thenReturn(successResult);

    // When
    ResponseEntity<CardCollectionService.CardScanResult> response =
        cardController.scanCard(guardianId, new CardController.QRScanRequest(qrCode));

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().isSuccess()).isTrue();
    assertThat(response.getBody().getCard()).isEqualTo(testCard);
    assertThat(response.getBody().isNew()).isTrue();
  }

  @Test
  public void shouldReturnBadRequestForInvalidQRCode() {
    // Given
    Long guardianId = 1L;
    String invalidQrCode = "INVALID";
    CardCollectionService.CardScanResult failureResult =
        CardCollectionService.CardScanResult.invalidQR("Invalid QR code format");

    when(cardCollectionService.scanQRCode(eq(guardianId), eq(invalidQrCode)))
        .thenReturn(failureResult);

    // When
    ResponseEntity<CardCollectionService.CardScanResult> response =
        cardController.scanCard(guardianId, new CardController.QRScanRequest(invalidQrCode));

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().isSuccess()).isFalse();
    assertThat(response.getBody().getMessage()).isEqualTo("Invalid QR code format");
  }

  @Test
  public void shouldReturnNotFoundForNonExistentCard() {
    // Given
    Long guardianId = 1L;
    String qrCode = "NONEXISTENT123456";
    CardCollectionService.CardScanResult notFoundResult =
        CardCollectionService.CardScanResult.cardNotFound("Card not found for QR code: " + qrCode);

    when(cardCollectionService.scanQRCode(eq(guardianId), eq(qrCode))).thenReturn(notFoundResult);

    // When
    ResponseEntity<CardCollectionService.CardScanResult> response =
        cardController.scanCard(guardianId, new CardController.QRScanRequest(qrCode));

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().isSuccess()).isFalse();
    assertThat(response.getBody().getMessage()).contains("Card not found");
  }

  @Test
  public void shouldGetGuardianCardsSuccessfully() {
    // Given
    Long guardianId = 1L;
    CollectedCard collectedCard = CollectedCard.create(testCard, LocalDateTime.now());
    List<CollectedCard> expectedCards = Arrays.asList(collectedCard);

    when(cardCollectionService.getGuardianCards(eq(guardianId))).thenReturn(expectedCards);

    // When
    ResponseEntity<List<CollectedCard>> response = cardController.getGuardianCards(guardianId);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()).hasSize(1);
    assertThat(response.getBody().get(0).getCard()).isEqualTo(testCard);
  }

  @Test
  public void shouldReturnEmptyListForGuardianWithNoCards() {
    // Given
    Long guardianId = 999L;
    List<CollectedCard> emptyCards = Arrays.asList();

    when(cardCollectionService.getGuardianCards(eq(guardianId))).thenReturn(emptyCards);

    // When
    ResponseEntity<List<CollectedCard>> response = cardController.getGuardianCards(guardianId);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()).isEmpty();
  }

  @Test
  public void shouldHandleExceptionDuringCardScan() {
    // Given
    Long guardianId = 1L;
    String qrCode = "ABC123DEF4567890";

    when(cardCollectionService.scanQRCode(eq(guardianId), eq(qrCode)))
        .thenThrow(new RuntimeException("Database connection failed"));

    // When
    ResponseEntity<CardCollectionService.CardScanResult> response =
        cardController.scanCard(guardianId, new CardController.QRScanRequest(qrCode));

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  public void shouldHandleExceptionDuringGetCards() {
    // Given
    Long guardianId = 1L;

    when(cardCollectionService.getGuardianCards(eq(guardianId)))
        .thenThrow(new RuntimeException("Database connection failed"));

    // When
    ResponseEntity<List<CollectedCard>> response = cardController.getGuardianCards(guardianId);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }
}

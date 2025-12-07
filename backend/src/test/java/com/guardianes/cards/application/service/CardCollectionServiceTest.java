package com.guardianes.cards.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.guardianes.cards.domain.model.*;
import com.guardianes.cards.domain.repository.CardCollectionRepository;
import com.guardianes.cards.domain.repository.CardRepository;
import com.guardianes.cards.domain.service.QRValidationService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CardCollectionServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private CardCollectionRepository collectionRepository;
    @Mock
    private QRValidationService qrValidationService;

    private CardCollectionService cardCollectionService;
    private Card testCard;

    @BeforeEach
    void setUp() {
        cardCollectionService = new CardCollectionService(cardRepository, collectionRepository, qrValidationService);

        testCard = new Card(
                1L,
                "Fire Dragon",
                "A powerful fire-breathing dragon",
                CardElement.FIRE,
                CardRarity.RARE,
                80,
                60,
                5,
                "https://example.com/fire-dragon.jpg",
                "GDGFR0000000001X",
                null,
                LocalDateTime.now(),
                true);
    }

    @Test
    public void scanQRCode_shouldReturnNewCard_whenValidQRAndCardNotOwned() {
        // Given
        Long guardianId = 1L;
        String qrCode = "GDGFR0000000001X";
        QRValidationService.QRValidationResult validResult = QRValidationService.QRValidationResult.valid(qrCode);

        when(qrValidationService.validateQRCode(qrCode)).thenReturn(validResult);
        when(cardRepository.findByQrCode(qrCode)).thenReturn(Optional.of(testCard));
        when(collectionRepository.guardianOwnsCard(guardianId, testCard.getId())).thenReturn(false);

        // When
        CardCollectionService.CardScanResult result = cardCollectionService.scanQRCode(guardianId, qrCode);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isNew()).isTrue();
        assertThat(result.getCard()).isEqualTo(testCard);
        assertThat(result.getMessage()).contains("New card");
        verify(collectionRepository).addCardToCollection(guardianId, testCard.getId(), 1);
    }

    @Test
    public void scanQRCode_shouldReturnDuplicate_whenCardAlreadyOwned() {
        // Given
        Long guardianId = 1L;
        String qrCode = "GDGFR0000000001X";
        QRValidationService.QRValidationResult validResult = QRValidationService.QRValidationResult.valid(qrCode);

        when(qrValidationService.validateQRCode(qrCode)).thenReturn(validResult);
        when(cardRepository.findByQrCode(qrCode)).thenReturn(Optional.of(testCard));
        when(collectionRepository.guardianOwnsCard(guardianId, testCard.getId())).thenReturn(true);
        when(collectionRepository.getCardCount(guardianId, testCard.getId())).thenReturn(2);

        // When
        CardCollectionService.CardScanResult result = cardCollectionService.scanQRCode(guardianId, qrCode);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.isNew()).isFalse();
        assertThat(result.getCard()).isEqualTo(testCard);
        assertThat(result.getCount()).isEqualTo(2);
        assertThat(result.getMessage()).contains("already owned");
        verify(collectionRepository).addCardToCollection(guardianId, testCard.getId(), 1);
    }

    @Test
    public void scanQRCode_shouldReturnInvalidQR_whenQRValidationFails() {
        // Given
        Long guardianId = 1L;
        String invalidQrCode = "INVALID123";
        QRValidationService.QRValidationResult invalidResult = QRValidationService.QRValidationResult
                .invalid("Invalid QR code format");

        when(qrValidationService.validateQRCode(invalidQrCode)).thenReturn(invalidResult);

        // When
        CardCollectionService.CardScanResult result = cardCollectionService.scanQRCode(guardianId, invalidQrCode);

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("Invalid QR code format");
        assertThat(result.getCard()).isNull();
    }

    @Test
    public void scanQRCode_shouldReturnCardNotFound_whenNoCardForQRCode() {
        // Given
        Long guardianId = 1L;
        String qrCode = "GDGFR0000000002X";
        QRValidationService.QRValidationResult validResult = QRValidationService.QRValidationResult.valid(qrCode);

        when(qrValidationService.validateQRCode(qrCode)).thenReturn(validResult);
        when(cardRepository.findByQrCode(qrCode)).thenReturn(Optional.empty());

        // When
        CardCollectionService.CardScanResult result = cardCollectionService.scanQRCode(guardianId, qrCode);

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("Card not found");
    }

    @Test
    public void scanQRCode_shouldReturnCardInactive_whenCardIsNotActive() {
        // Given
        Long guardianId = 1L;
        String qrCode = "GDGFR0000000001X";
        Card inactiveCard = new Card(
                1L,
                "Inactive Card",
                "An inactive card",
                CardElement.FIRE,
                CardRarity.COMMON,
                50,
                40,
                3,
                "https://example.com/card.jpg",
                qrCode,
                null,
                LocalDateTime.now(),
                false); // inactive

        QRValidationService.QRValidationResult validResult = QRValidationService.QRValidationResult.valid(qrCode);

        when(qrValidationService.validateQRCode(qrCode)).thenReturn(validResult);
        when(cardRepository.findByQrCode(qrCode)).thenReturn(Optional.of(inactiveCard));

        // When
        CardCollectionService.CardScanResult result = cardCollectionService.scanQRCode(guardianId, qrCode);

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).contains("no longer active");
    }

    @Test
    public void getGuardianCards_shouldReturnCardsForGuardian() {
        // Given
        Long guardianId = 1L;
        CollectedCard collectedCard = CollectedCard.create(testCard, LocalDateTime.now());
        List<CollectedCard> expectedCards = Arrays.asList(collectedCard);

        when(collectionRepository.getGuardianCards(guardianId)).thenReturn(expectedCards);

        // When
        List<CollectedCard> result = cardCollectionService.getGuardianCards(guardianId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCard()).isEqualTo(testCard);
    }

    @Test
    public void searchCards_shouldReturnMatchingCards() {
        // Given
        String searchName = "Dragon";
        List<Card> expectedCards = Arrays.asList(testCard);

        when(cardRepository.findByNameContainingIgnoreCase(searchName)).thenReturn(expectedCards);

        // When
        List<Card> result = cardCollectionService.searchCards(searchName);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Fire Dragon");
    }

    @Test
    public void searchCards_shouldReturnAllActive_whenNameIsEmpty() {
        // Given
        List<Card> allCards = Arrays.asList(testCard);

        when(cardRepository.findAllActive()).thenReturn(allCards);

        // When
        List<Card> result = cardCollectionService.searchCards("");

        // Then
        assertThat(result).hasSize(1);
        verify(cardRepository).findAllActive();
    }
}

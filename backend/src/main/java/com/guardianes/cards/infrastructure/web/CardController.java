package com.guardianes.cards.infrastructure.web;

import com.guardianes.cards.application.service.CardCollectionService;
import com.guardianes.cards.domain.model.CollectedCard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/guardians/{guardianId}/cards")
@Tag(name = "Card Collection", description = "Guardian card collection management endpoints")
public class CardController {

  private final CardCollectionService cardCollectionService;

  public CardController(CardCollectionService cardCollectionService) {
    this.cardCollectionService = cardCollectionService;
  }

  @PostMapping("/scan")
  @Operation(
      summary = "Scan QR code to collect card",
      description = "Scans a QR code and adds the corresponding card to the guardian's collection")
  public ResponseEntity<CardCollectionService.CardScanResult> scanCard(
      @PathVariable Long guardianId, @Valid @RequestBody QRScanRequest request) {

    try {
      CardCollectionService.CardScanResult result =
          cardCollectionService.scanQRCode(guardianId, request.getQrCode());

      if (result.isSuccess()) {
        return ResponseEntity.ok(result);
      } else {
        // Determine appropriate HTTP status based on result type
        if (result.getMessage().contains("not found")) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        } else {
          return ResponseEntity.badRequest().body(result);
        }
      }
    } catch (Exception e) {
      // Create error result for exceptions
      CardCollectionService.CardScanResult errorResult =
          CardCollectionService.CardScanResult.invalidQR("System error: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
    }
  }

  @GetMapping
  @Operation(
      summary = "Get guardian's cards",
      description = "Returns all cards in the guardian's collection")
  public ResponseEntity<List<CollectedCard>> getGuardianCards(@PathVariable Long guardianId) {

    try {
      List<CollectedCard> cards = cardCollectionService.getGuardianCards(guardianId);
      return ResponseEntity.ok(cards);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  // Request DTO
  public static class QRScanRequest {
    @NotBlank(message = "QR code is required")
    private String qrCode;

    public QRScanRequest() {}

    public QRScanRequest(String qrCode) {
      this.qrCode = qrCode;
    }

    public String getQrCode() {
      return qrCode;
    }

    public void setQrCode(String qrCode) {
      this.qrCode = qrCode;
    }
  }
}

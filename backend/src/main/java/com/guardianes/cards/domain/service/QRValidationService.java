package com.guardianes.cards.domain.service;

import com.guardianes.cards.domain.model.Card;
import com.guardianes.cards.domain.model.CardElement;
import com.guardianes.cards.domain.model.CardRarity;
import java.util.Objects;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/** Service responsible for validating QR codes and NFC codes for card collection. */
@Service
public class QRValidationService {

  private static final Pattern QR_CODE_PATTERN = Pattern.compile("^[A-Z0-9]{16}$");
  private static final Pattern NFC_CODE_PATTERN = Pattern.compile("^[A-F0-9]{24}$");
  private static final String QR_PREFIX = "GDG"; // Guardianes de Gaia prefix

  /** Validates a QR code format and structure. */
  public QRValidationResult validateQRCode(String qrCode) {
    if (qrCode == null || qrCode.trim().isEmpty()) {
      return QRValidationResult.invalid("QR code cannot be null or empty");
    }

    String cleanCode = qrCode.trim().toUpperCase();

    // Check format
    if (!QR_CODE_PATTERN.matcher(cleanCode).matches()) {
      return QRValidationResult.invalid(
          "Invalid QR code format. Must be 16 alphanumeric characters");
    }

    // Check prefix (first 3 characters should be "GDG")
    if (!cleanCode.startsWith(QR_PREFIX)) {
      return QRValidationResult.invalid("Invalid QR code prefix. Must start with " + QR_PREFIX);
    }

    // Check checksum (simple validation)
    if (!isValidChecksum(cleanCode)) {
      return QRValidationResult.invalid("Invalid QR code checksum");
    }

    return QRValidationResult.valid(cleanCode);
  }

  /** Validates an NFC code format and structure. */
  public QRValidationResult validateNFCCode(String nfcCode) {
    if (nfcCode == null || nfcCode.trim().isEmpty()) {
      return QRValidationResult.validEmpty(); // NFC is optional
    }

    String cleanCode = nfcCode.trim().toUpperCase();

    // Check format
    if (!NFC_CODE_PATTERN.matcher(cleanCode).matches()) {
      return QRValidationResult.invalid(
          "Invalid NFC code format. Must be 24 hexadecimal characters");
    }

    // NFC codes don't need prefix validation as they're more secure
    return QRValidationResult.valid(cleanCode);
  }

  /** Validates that a card's QR code matches its expected format. */
  public boolean isValidCardQRCode(Card card, String scannedQRCode) {
    if (card == null || scannedQRCode == null) {
      return false;
    }

    QRValidationResult validationResult = validateQRCode(scannedQRCode);
    if (!validationResult.isValid()) {
      return false;
    }

    return Objects.equals(card.getQrCode(), validationResult.getCleanCode());
  }

  /** Generates a QR code for a card (used in card creation). */
  public String generateQRCode(Long cardId, CardElement element, CardRarity rarity) {
    if (cardId == null || cardId <= 0) {
      throw new IllegalArgumentException("Card ID must be positive");
    }
    if (element == null || rarity == null) {
      throw new IllegalArgumentException("Element and rarity cannot be null");
    }

    // Format: GDG + ElementCode(1) + RarityCode(1) + CardId(10) + Checksum(1)
    String elementCode = getElementCode(element);
    String rarityCode = getRarityCode(rarity);
    String cardIdPadded = String.format("%010d", cardId); // Pad to 10 digits

    String baseCode = QR_PREFIX + elementCode + rarityCode + cardIdPadded;
    String checksum = calculateChecksum(baseCode);

    return baseCode + checksum;
  }

  /** Extracts card information from a valid QR code. */
  public QRCodeInfo extractQRCodeInfo(String qrCode) {
    QRValidationResult validation = validateQRCode(qrCode);
    if (!validation.isValid()) {
      throw new IllegalArgumentException("Invalid QR code: " + validation.getErrorMessage());
    }

    String cleanCode = validation.getCleanCode();

    // Parse components
    String elementCode = cleanCode.substring(3, 4);
    String rarityCode = cleanCode.substring(4, 5);
    String cardIdStr = cleanCode.substring(5, 15);

    CardElement element = parseElementCode(elementCode);
    CardRarity rarity = parseRarityCode(rarityCode);
    Long cardId = Long.parseLong(cardIdStr);

    return new QRCodeInfo(cardId, element, rarity);
  }

  // Private helper methods
  private boolean isValidChecksum(String qrCode) {
    if (qrCode.length() != 16) return false;

    String baseCode = qrCode.substring(0, 15);
    String actualChecksum = qrCode.substring(15);
    String expectedChecksum = calculateChecksum(baseCode);

    return expectedChecksum.equals(actualChecksum);
  }

  private String calculateChecksum(String baseCode) {
    int sum = 0;
    for (char c : baseCode.toCharArray()) {
      sum += Character.isDigit(c) ? Character.getNumericValue(c) : (c - 'A' + 10);
    }
    return String.valueOf(sum % 36 < 10 ? (char) ('0' + sum % 36) : (char) ('A' + sum % 36 - 10));
  }

  private String getElementCode(CardElement element) {
    return switch (element) {
      case FIRE -> "F";
      case EARTH -> "E";
      case WATER -> "W";
      case AIR -> "A";
    };
  }

  private String getRarityCode(CardRarity rarity) {
    return switch (rarity) {
      case COMMON -> "C";
      case UNCOMMON -> "U";
      case RARE -> "R";
      case EPIC -> "P";
      case LEGENDARY -> "L";
    };
  }

  private CardElement parseElementCode(String code) {
    return switch (code) {
      case "F" -> CardElement.FIRE;
      case "E" -> CardElement.EARTH;
      case "W" -> CardElement.WATER;
      case "A" -> CardElement.AIR;
      default -> throw new IllegalArgumentException("Invalid element code: " + code);
    };
  }

  private CardRarity parseRarityCode(String code) {
    return switch (code) {
      case "C" -> CardRarity.COMMON;
      case "U" -> CardRarity.UNCOMMON;
      case "R" -> CardRarity.RARE;
      case "P" -> CardRarity.EPIC;
      case "L" -> CardRarity.LEGENDARY;
      default -> throw new IllegalArgumentException("Invalid rarity code: " + code);
    };
  }

  // Inner classes
  public static class QRValidationResult {
    private final boolean valid;
    private final String cleanCode;
    private final String errorMessage;

    private QRValidationResult(boolean valid, String cleanCode, String errorMessage) {
      this.valid = valid;
      this.cleanCode = cleanCode;
      this.errorMessage = errorMessage;
    }

    public static QRValidationResult valid(String cleanCode) {
      return new QRValidationResult(true, cleanCode, null);
    }

    public static QRValidationResult validEmpty() {
      return new QRValidationResult(true, null, null);
    }

    public static QRValidationResult invalid(String errorMessage) {
      return new QRValidationResult(false, null, errorMessage);
    }

    public boolean isValid() {
      return valid;
    }

    public String getCleanCode() {
      return cleanCode;
    }

    public String getErrorMessage() {
      return errorMessage;
    }
  }

  public static class QRCodeInfo {
    private final Long cardId;
    private final CardElement element;
    private final CardRarity rarity;

    public QRCodeInfo(Long cardId, CardElement element, CardRarity rarity) {
      this.cardId = cardId;
      this.element = element;
      this.rarity = rarity;
    }

    public Long getCardId() {
      return cardId;
    }

    public CardElement getElement() {
      return element;
    }

    public CardRarity getRarity() {
      return rarity;
    }
  }
}

package com.guardianes.cards.domain.service;

import static org.junit.jupiter.api.Assertions.*;

import com.guardianes.cards.domain.model.CardElement;
import com.guardianes.cards.domain.model.CardRarity;
import com.guardianes.cards.domain.service.QRValidationService.QRValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class QRValidationServiceTest {

  private QRValidationService qrValidationService;

  @BeforeEach
  void setUp() {
    qrValidationService = new QRValidationService();
  }

  @Nested
  @DisplayName("Validate QR Code Tests")
  class ValidateQRCodeTests {

    @Test
    @DisplayName("should return invalid when QR code is null or empty")
    void shouldReturnInvalidWhenQrCodeIsNullOrEmpty() {
      assertFalse(qrValidationService.validateQRCode(null).isValid());
      assertFalse(qrValidationService.validateQRCode("").isValid());
      assertFalse(qrValidationService.validateQRCode("  ").isValid());
    }

    @Test
    @DisplayName("should return invalid when format doesn't match regex")
    void shouldReturnInvalidWhenFormatDoesNotMatch() {
      // Too short
      assertFalse(qrValidationService.validateQRCode("SHORT").isValid());
      // Non alphanumeric
      assertFalse(qrValidationService.validateQRCode("GDGFC000000000-").isValid());
    }

    @Test
    @DisplayName("should return invalid when prefix is wrong")
    void shouldReturnInvalidWhenPrefixIsWrong() {
      // Correct length (16), but wrong prefix (ABC)
      String wrongPrefix = "ABCFC0000000001X";
      QRValidationResult result = qrValidationService.validateQRCode(wrongPrefix);
      assertFalse(result.isValid());
      assertTrue(result.getErrorMessage().contains("prefix"));
    }

    @Test
    @DisplayName("should return valid for correct QR code")
    void shouldReturnValidForCorrectQrCode() {
      // Generate a valid code first to ensure test pass
      String validCode =
          qrValidationService.generateQRCode(1L, CardElement.FIRE, CardRarity.COMMON);

      QRValidationResult result = qrValidationService.validateQRCode(validCode);
      assertTrue(result.isValid());
      assertEquals(validCode, result.getCleanCode());
    }
  }

  @Nested
  @DisplayName("Generate QR Code Tests")
  class GenerateQRCodeTests {

    @Test
    @DisplayName("should generate valid QR code")
    void shouldGenerateValidQrCode() {
      String code = qrValidationService.generateQRCode(123L, CardElement.WATER, CardRarity.RARE);

      assertNotNull(code);
      assertEquals(16, code.length());
      assertTrue(code.startsWith("GDG")); // Prefix
      assertTrue(code.contains("W")); // Water
      assertTrue(code.contains("R")); // Rare

      // Verify it passes validation
      assertTrue(qrValidationService.validateQRCode(code).isValid());
    }
  }

  @Nested
  @DisplayName("Extract QR Info Tests")
  class ExtractQRInfoTests {

    @Test
    @DisplayName("should extract correct info from valid QR code")
    void shouldExtractCorrectInfo() {
      // 100L, EARTH, EPIC
      String code = qrValidationService.generateQRCode(100L, CardElement.EARTH, CardRarity.EPIC);

      QRValidationService.QRCodeInfo info = qrValidationService.extractQRCodeInfo(code);

      assertEquals(100L, info.getCardId());
      assertEquals(CardElement.EARTH, info.getElement());
      assertEquals(CardRarity.EPIC, info.getRarity());
    }

    @Test
    @DisplayName("should throw exception when extracting info from invalid code")
    void shouldThrowExceptionWhenExtractingFromInvalidCode() {
      assertThrows(
          IllegalArgumentException.class,
          () -> qrValidationService.extractQRCodeInfo("INVALIDCODE12345"));
    }
  }
}

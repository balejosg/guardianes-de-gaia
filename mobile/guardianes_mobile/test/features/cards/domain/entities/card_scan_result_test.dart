import 'package:flutter_test/flutter_test.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/card.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/card_scan_result.dart';

void main() {
  Card createTestCard() {
    return Card(
      id: 1,
      name: 'Fire Dragon',
      description: 'A fierce dragon',
      element: CardElement.fire,
      rarity: CardRarity.legendary,
      attackPower: 100,
      defensePower: 80,
      energyCost: 5,
      qrCode: 'dragon_qr',
      createdAt: DateTime(2025, 1, 1),
      isActive: true,
    );
  }

  group('CardScanResult', () {
    group('success factory', () {
      test('should create success result with isNew true', () {
        final card = createTestCard();
        final result = CardScanResult.success(
          card: card,
          count: 1,
          isNew: true,
        );

        expect(result.success, true);
        expect(result.message, '¡Nueva carta coleccionada!');
        expect(result.card, card);
        expect(result.count, 1);
        expect(result.isNew, true);
      });

      test('should create success result with isNew false', () {
        final card = createTestCard();
        final result = CardScanResult.success(
          card: card,
          count: 3,
          isNew: false,
        );

        expect(result.success, true);
        expect(result.message, '¡Carta ya poseída - cantidad aumentada!');
        expect(result.card, card);
        expect(result.count, 3);
        expect(result.isNew, false);
      });
    });

    group('error factory', () {
      test('should create error result with message', () {
        final result = CardScanResult.error('Código QR inválido');

        expect(result.success, false);
        expect(result.message, 'Código QR inválido');
        expect(result.card, isNull);
        expect(result.count, isNull);
        expect(result.isNew, false);
      });

      test('should create error result with empty message', () {
        final result = CardScanResult.error('');

        expect(result.success, false);
        expect(result.message, '');
        expect(result.isNew, false);
      });
    });

    group('Equatable', () {
      test('should be equal when all props match', () {
        final card = createTestCard();
        final result1 = CardScanResult.success(card: card, count: 1, isNew: true);
        final result2 = CardScanResult.success(card: card, count: 1, isNew: true);

        expect(result1, equals(result2));
      });

      test('should not be equal when success differs', () {
        final result1 = CardScanResult.error('Error');
        final card = createTestCard();
        final result2 = CardScanResult.success(card: card, count: 1, isNew: true);

        expect(result1, isNot(equals(result2)));
      });

      test('should not be equal when count differs', () {
        final card = createTestCard();
        final result1 = CardScanResult.success(card: card, count: 1, isNew: true);
        final result2 = CardScanResult.success(card: card, count: 2, isNew: true);

        expect(result1, isNot(equals(result2)));
      });
    });
  });
}

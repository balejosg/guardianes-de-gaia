import 'package:flutter_test/flutter_test.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/card.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/collected_card.dart';

void main() {
  Card createTestCard({int id = 1}) {
    return Card(
      id: id,
      name: 'Test Card $id',
      description: 'A test card',
      element: CardElement.fire,
      rarity: CardRarity.common,
      attackPower: 50,
      defensePower: 30,
      energyCost: 3,
      qrCode: 'test_qr_$id',
      createdAt: DateTime(2025, 1, 1),
      isActive: true,
    );
  }

  group('CollectedCard', () {
    test('should have correct properties', () {
      final card = createTestCard();
      final collectedCard = CollectedCard(
        card: card,
        count: 3,
        firstCollectedAt: DateTime(2025, 1, 1),
        lastCollectedAt: DateTime(2025, 1, 5),
      );

      expect(collectedCard.card, card);
      expect(collectedCard.count, 3);
      expect(collectedCard.firstCollectedAt, DateTime(2025, 1, 1));
      expect(collectedCard.lastCollectedAt, DateTime(2025, 1, 5));
    });

    test('should be equal with same values', () {
      final card = createTestCard();
      final collectedCard1 = CollectedCard(
        card: card,
        count: 2,
        firstCollectedAt: DateTime(2025, 1, 1),
        lastCollectedAt: DateTime(2025, 1, 2),
      );
      final collectedCard2 = CollectedCard(
        card: card,
        count: 2,
        firstCollectedAt: DateTime(2025, 1, 1),
        lastCollectedAt: DateTime(2025, 1, 2),
      );

      expect(collectedCard1, collectedCard2);
    });

    test('should not be equal with different count', () {
      final card = createTestCard();
      final collectedCard1 = CollectedCard(
        card: card,
        count: 1,
        firstCollectedAt: DateTime(2025, 1, 1),
        lastCollectedAt: DateTime(2025, 1, 2),
      );
      final collectedCard2 = CollectedCard(
        card: card,
        count: 5,
        firstCollectedAt: DateTime(2025, 1, 1),
        lastCollectedAt: DateTime(2025, 1, 2),
      );

      expect(collectedCard1, isNot(collectedCard2));
    });

    test('should not be equal with different card', () {
      final collectedCard1 = CollectedCard(
        card: createTestCard(id: 1),
        count: 1,
        firstCollectedAt: DateTime(2025, 1, 1),
        lastCollectedAt: DateTime(2025, 1, 2),
      );
      final collectedCard2 = CollectedCard(
        card: createTestCard(id: 2),
        count: 1,
        firstCollectedAt: DateTime(2025, 1, 1),
        lastCollectedAt: DateTime(2025, 1, 2),
      );

      expect(collectedCard1, isNot(collectedCard2));
    });

    test('props contains all fields', () {
      final card = createTestCard();
      final firstCollected = DateTime(2025, 1, 1);
      final lastCollected = DateTime(2025, 1, 2);
      final collectedCard = CollectedCard(
        card: card,
        count: 3,
        firstCollectedAt: firstCollected,
        lastCollectedAt: lastCollected,
      );

      expect(collectedCard.props, [card, 3, firstCollected, lastCollected]);
    });
  });
}

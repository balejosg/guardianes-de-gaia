import 'package:flutter_test/flutter_test.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/card.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/card_collection.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/collected_card.dart';

void main() {
  Card createTestCard({
    int id = 1,
    String name = 'Test Card',
    CardElement element = CardElement.fire,
    CardRarity rarity = CardRarity.common,
  }) {
    return Card(
      id: id,
      name: name,
      description: 'A test card',
      element: element,
      rarity: rarity,
      attackPower: 50,
      defensePower: 30,
      energyCost: 3,
      qrCode: 'test_qr_$id',
      createdAt: DateTime(2025, 1, 1),
      isActive: true,
    );
  }

  CollectedCard createCollectedCard({
    Card? card,
    int count = 1,
    DateTime? lastCollectedAt,
  }) {
    return CollectedCard(
      card: card ?? createTestCard(),
      count: count,
      firstCollectedAt: DateTime(2025, 1, 1),
      lastCollectedAt: lastCollectedAt ?? DateTime(2025, 1, 2),
    );
  }

  group('CardCollection', () {
    test('uniqueCardCount returns number of unique cards', () {
      final collection = CardCollection(
        guardianId: 1,
        cards: [
          createCollectedCard(card: createTestCard(id: 1)),
          createCollectedCard(card: createTestCard(id: 2)),
          createCollectedCard(card: createTestCard(id: 3)),
        ],
        createdAt: DateTime(2025, 1, 1),
      );

      expect(collection.uniqueCardCount, 3);
    });

    test('totalCardCount sums all card counts', () {
      final collection = CardCollection(
        guardianId: 1,
        cards: [
          createCollectedCard(card: createTestCard(id: 1), count: 2),
          createCollectedCard(card: createTestCard(id: 2), count: 3),
          createCollectedCard(card: createTestCard(id: 3), count: 5),
        ],
        createdAt: DateTime(2025, 1, 1),
      );

      expect(collection.totalCardCount, 10);
    });

    test('getCardsByElement filters correctly', () {
      final collection = CardCollection(
        guardianId: 1,
        cards: [
          createCollectedCard(card: createTestCard(id: 1, element: CardElement.fire)),
          createCollectedCard(card: createTestCard(id: 2, element: CardElement.water)),
          createCollectedCard(card: createTestCard(id: 3, element: CardElement.fire)),
        ],
        createdAt: DateTime(2025, 1, 1),
      );

      expect(collection.getCardsByElement(CardElement.fire).length, 2);
      expect(collection.getCardsByElement(CardElement.water).length, 1);
      expect(collection.getCardsByElement(CardElement.earth).length, 0);
    });

    test('getCardsByRarity filters correctly', () {
      final collection = CardCollection(
        guardianId: 1,
        cards: [
          createCollectedCard(card: createTestCard(id: 1, rarity: CardRarity.common)),
          createCollectedCard(card: createTestCard(id: 2, rarity: CardRarity.legendary)),
          createCollectedCard(card: createTestCard(id: 3, rarity: CardRarity.common)),
        ],
        createdAt: DateTime(2025, 1, 1),
      );

      expect(collection.getCardsByRarity(CardRarity.common).length, 2);
      expect(collection.getCardsByRarity(CardRarity.legendary).length, 1);
    });

    test('cardCountsByElement returns correct counts', () {
      final collection = CardCollection(
        guardianId: 1,
        cards: [
          createCollectedCard(card: createTestCard(id: 1, element: CardElement.fire), count: 2),
          createCollectedCard(card: createTestCard(id: 2, element: CardElement.water), count: 3),
          createCollectedCard(card: createTestCard(id: 3, element: CardElement.fire), count: 1),
        ],
        createdAt: DateTime(2025, 1, 1),
      );

      final counts = collection.cardCountsByElement;
      expect(counts[CardElement.fire], 3);
      expect(counts[CardElement.water], 3);
      expect(counts[CardElement.earth], 0);
      expect(counts[CardElement.air], 0);
    });

    test('cardCountsByRarity returns correct counts', () {
      final collection = CardCollection(
        guardianId: 1,
        cards: [
          createCollectedCard(card: createTestCard(id: 1, rarity: CardRarity.common), count: 5),
          createCollectedCard(card: createTestCard(id: 2, rarity: CardRarity.legendary), count: 1),
        ],
        createdAt: DateTime(2025, 1, 1),
      );

      final counts = collection.cardCountsByRarity;
      expect(counts[CardRarity.common], 5);
      expect(counts[CardRarity.legendary], 1);
    });

    test('hasElementalBalance returns true when all elements present', () {
      final collection = CardCollection(
        guardianId: 1,
        cards: [
          createCollectedCard(card: createTestCard(id: 1, element: CardElement.fire)),
          createCollectedCard(card: createTestCard(id: 2, element: CardElement.water)),
          createCollectedCard(card: createTestCard(id: 3, element: CardElement.earth)),
          createCollectedCard(card: createTestCard(id: 4, element: CardElement.air)),
        ],
        createdAt: DateTime(2025, 1, 1),
      );

      expect(collection.hasElementalBalance, true);
    });

    test('hasElementalBalance returns false when missing element', () {
      final collection = CardCollection(
        guardianId: 1,
        cards: [
          createCollectedCard(card: createTestCard(id: 1, element: CardElement.fire)),
          createCollectedCard(card: createTestCard(id: 2, element: CardElement.water)),
        ],
        createdAt: DateTime(2025, 1, 1),
      );

      expect(collection.hasElementalBalance, false);
    });

    test('totalTradeValue calculates correctly', () {
      final collection = CardCollection(
        guardianId: 1,
        cards: [
          createCollectedCard(card: createTestCard(id: 1, rarity: CardRarity.common), count: 2),
          createCollectedCard(card: createTestCard(id: 2, rarity: CardRarity.legendary), count: 1),
        ],
        createdAt: DateTime(2025, 1, 1),
      );

      // common = 1, legendary = 100
      expect(collection.totalTradeValue, 2 * 1 + 1 * 100);
    });

    test('rarestCard returns null for empty collection', () {
      final collection = CardCollection(
        guardianId: 1,
        cards: [],
        createdAt: DateTime(2025, 1, 1),
      );

      expect(collection.rarestCard, isNull);
    });

    test('rarestCard returns highest rarity card', () {
      final collection = CardCollection(
        guardianId: 1,
        cards: [
          createCollectedCard(card: createTestCard(id: 1, rarity: CardRarity.common)),
          createCollectedCard(card: createTestCard(id: 2, rarity: CardRarity.legendary)),
          createCollectedCard(card: createTestCard(id: 3, rarity: CardRarity.rare)),
        ],
        createdAt: DateTime(2025, 1, 1),
      );

      expect(collection.rarestCard?.card.rarity, CardRarity.legendary);
    });

    test('getRecentlyCollected returns sorted by lastCollectedAt', () {
      final collection = CardCollection(
        guardianId: 1,
        cards: [
          createCollectedCard(
            card: createTestCard(id: 1),
            lastCollectedAt: DateTime(2025, 1, 1),
          ),
          createCollectedCard(
            card: createTestCard(id: 2),
            lastCollectedAt: DateTime(2025, 1, 3),
          ),
          createCollectedCard(
            card: createTestCard(id: 3),
            lastCollectedAt: DateTime(2025, 1, 2),
          ),
        ],
        createdAt: DateTime(2025, 1, 1),
      );

      final recent = collection.getRecentlyCollected(2);
      expect(recent.length, 2);
      expect(recent[0].card.id, 2);
      expect(recent[1].card.id, 3);
    });

    test('getCompletionPercentage calculates correctly', () {
      final collection = CardCollection(
        guardianId: 1,
        cards: [
          createCollectedCard(card: createTestCard(id: 1)),
          createCollectedCard(card: createTestCard(id: 2)),
        ],
        createdAt: DateTime(2025, 1, 1),
      );

      expect(collection.getCompletionPercentage(10), 20.0);
    });

    test('getCompletionPercentage returns 0 for zero total', () {
      final collection = CardCollection(
        guardianId: 1,
        cards: [createCollectedCard()],
        createdAt: DateTime(2025, 1, 1),
      );

      expect(collection.getCompletionPercentage(0), 0.0);
    });
  });
}

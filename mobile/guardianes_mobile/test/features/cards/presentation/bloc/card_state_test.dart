import 'package:flutter_test/flutter_test.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/card.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/card_collection.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/card_scan_result.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/collected_card.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/collection_statistics.dart';
import 'package:guardianes_mobile/features/cards/presentation/bloc/card_state.dart';

void main() {
  Card createTestCard({int id = 1}) {
    return Card(
      id: id,
      name: 'Test Card',
      description: 'A test card',
      element: CardElement.fire,
      rarity: CardRarity.common,
      attackPower: 50,
      defensePower: 30,
      energyCost: 3,
      qrCode: 'test_qr',
      createdAt: DateTime(2025, 1, 1),
      isActive: true,
    );
  }

  group('CardState', () {
    test('CardInitial has empty props', () {
      final state = CardInitial();
      expect(state.props, isEmpty);
    });

    test('CardLoading has empty props', () {
      final state = CardLoading();
      expect(state.props, isEmpty);
    });

    test('CardScanSuccess has result in props', () {
      final result = CardScanResult.success(
        card: createTestCard(),
        count: 1,
        isNew: true,
      );
      final state = CardScanSuccess(result: result);
      expect(state.props, [result]);
    });

    test('CardScanFailure has error in props', () {
      const state = CardScanFailure(error: 'Error message');
      expect(state.props, ['Error message']);
    });

    test('CollectionLoaded has collection in props', () {
      final collection = CardCollection(
        guardianId: 1,
        cards: [],
        createdAt: DateTime(2025, 1, 1),
      );
      final state = CollectionLoaded(collection: collection);
      expect(state.props, [collection]);
    });

    test('CollectionStatisticsLoaded has statistics in props', () {
      const statistics = CollectionStatistics(
        uniqueCardCount: 10,
        totalCardCount: 20,
        completionPercentage: 50.0,
        cardCountsByElement: {},
        cardCountsByRarity: {},
        totalTradeValue: 100,
        hasElementalBalance: true,
      );
      const state = CollectionStatisticsLoaded(statistics: statistics);
      expect(state.props, [statistics]);
    });

    test('CardsSearchResults has cards in props', () {
      final cards = [createTestCard(id: 1), createTestCard(id: 2)];
      final state = CardsSearchResults(cards: cards);
      expect(state.props, [cards]);
    });

    test('RecentCardsLoaded has recentCards in props', () {
      final recentCards = [
        CollectedCard(
          card: createTestCard(),
          count: 1,
          firstCollectedAt: DateTime(2025, 1, 1),
          lastCollectedAt: DateTime(2025, 1, 2),
        ),
      ];
      final state = RecentCardsLoaded(recentCards: recentCards);
      expect(state.props, [recentCards]);
    });

    test('CardError has error in props', () {
      const state = CardError(error: 'Network error');
      expect(state.props, ['Network error']);
    });
  });

  group('CardState equality', () {
    test('CardInitial instances are equal', () {
      expect(CardInitial(), CardInitial());
    });

    test('CardLoading instances are equal', () {
      expect(CardLoading(), CardLoading());
    });

    test('CardError with same error are equal', () {
      const state1 = CardError(error: 'Error');
      const state2 = CardError(error: 'Error');
      expect(state1, state2);
    });

    test('CardError with different error are not equal', () {
      const state1 = CardError(error: 'Error 1');
      const state2 = CardError(error: 'Error 2');
      expect(state1, isNot(state2));
    });
  });
}

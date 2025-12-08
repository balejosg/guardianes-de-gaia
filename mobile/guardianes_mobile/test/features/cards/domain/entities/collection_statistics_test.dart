import 'package:flutter_test/flutter_test.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/card.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/collection_statistics.dart';

void main() {
  group('CollectionStatistics', () {
    test('should create statistics with all required fields', () {
      final stats = CollectionStatistics(
        uniqueCardCount: 15,
        totalCardCount: 25,
        completionPercentage: 31.25,
        cardCountsByElement: {
          CardElement.fire: 5,
          CardElement.water: 4,
          CardElement.earth: 3,
          CardElement.air: 3,
        },
        cardCountsByRarity: {
          CardRarity.common: 10,
          CardRarity.uncommon: 3,
          CardRarity.rare: 1,
          CardRarity.epic: 1,
          CardRarity.legendary: 0,
        },
        totalTradeValue: 50,
        hasElementalBalance: true,
      );

      expect(stats.uniqueCardCount, 15);
      expect(stats.totalCardCount, 25);
      expect(stats.completionPercentage, 31.25);
      expect(stats.cardCountsByElement[CardElement.fire], 5);
      expect(stats.cardCountsByRarity[CardRarity.common], 10);
      expect(stats.totalTradeValue, 50);
      expect(stats.hasElementalBalance, true);
    });

    test('should handle empty collections', () {
      final stats = CollectionStatistics(
        uniqueCardCount: 0,
        totalCardCount: 0,
        completionPercentage: 0.0,
        cardCountsByElement: {},
        cardCountsByRarity: {},
        totalTradeValue: 0,
        hasElementalBalance: false,
      );

      expect(stats.uniqueCardCount, 0);
      expect(stats.completionPercentage, 0.0);
      expect(stats.cardCountsByElement, isEmpty);
    });

    group('Equatable', () {
      test('should be equal when all props match', () {
        final stats1 = CollectionStatistics(
          uniqueCardCount: 10,
          totalCardCount: 15,
          completionPercentage: 20.0,
          cardCountsByElement: {CardElement.fire: 5},
          cardCountsByRarity: {CardRarity.common: 5},
          totalTradeValue: 30,
          hasElementalBalance: true,
        );

        final stats2 = CollectionStatistics(
          uniqueCardCount: 10,
          totalCardCount: 15,
          completionPercentage: 20.0,
          cardCountsByElement: {CardElement.fire: 5},
          cardCountsByRarity: {CardRarity.common: 5},
          totalTradeValue: 30,
          hasElementalBalance: true,
        );

        expect(stats1, equals(stats2));
      });

      test('should not be equal when uniqueCardCount differs', () {
        final stats1 = CollectionStatistics(
          uniqueCardCount: 10,
          totalCardCount: 15,
          completionPercentage: 20.0,
          cardCountsByElement: {},
          cardCountsByRarity: {},
          totalTradeValue: 30,
          hasElementalBalance: true,
        );

        final stats2 = CollectionStatistics(
          uniqueCardCount: 20,
          totalCardCount: 15,
          completionPercentage: 20.0,
          cardCountsByElement: {},
          cardCountsByRarity: {},
          totalTradeValue: 30,
          hasElementalBalance: true,
        );

        expect(stats1, isNot(equals(stats2)));
      });
    });
  });
}

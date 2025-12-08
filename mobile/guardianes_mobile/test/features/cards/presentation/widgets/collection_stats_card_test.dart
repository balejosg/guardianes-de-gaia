import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/collection_statistics.dart';
import 'package:guardianes_mobile/features/cards/presentation/widgets/collection_stats_card.dart';

void main() {
  CollectionStatistics createStatistics({
    int uniqueCardCount = 15,
    int totalCardCount = 25,
    double completionPercentage = 31.25,
    Map<String, int> cardCountsByElement = const {},
    Map<String, int> cardCountsByRarity = const {},
    int totalTradeValue = 150,
    bool hasElementalBalance = true,
  }) {
    return CollectionStatistics(
      uniqueCardCount: uniqueCardCount,
      totalCardCount: totalCardCount,
      completionPercentage: completionPercentage,
      cardCountsByElement: cardCountsByElement,
      cardCountsByRarity: cardCountsByRarity,
      totalTradeValue: totalTradeValue,
      hasElementalBalance: hasElementalBalance,
    );
  }

  Widget createWidgetUnderTest({required CollectionStatistics statistics}) {
    return MaterialApp(
      home: Scaffold(
        body: CollectionStatsCard(statistics: statistics),
      ),
    );
  }

  group('CollectionStatsCard', () {
    testWidgets('should display title', (WidgetTester tester) async {
      // Arrange
      final stats = createStatistics();

      // Act
      await tester.pumpWidget(createWidgetUnderTest(statistics: stats));

      // Assert
      expect(find.text('Estadísticas de Colección'), findsOneWidget);
    });

    testWidgets('should display total card count',
        (WidgetTester tester) async {
      // Arrange
      final stats = createStatistics(totalCardCount: 42);

      // Act
      await tester.pumpWidget(createWidgetUnderTest(statistics: stats));

      // Assert
      expect(find.text('42'), findsOneWidget);
      expect(find.text('Total de Cartas'), findsOneWidget);
    });

    testWidgets('should display unique card count',
        (WidgetTester tester) async {
      // Arrange
      final stats = createStatistics(uniqueCardCount: 18);

      // Act
      await tester.pumpWidget(createWidgetUnderTest(statistics: stats));

      // Assert
      expect(find.text('18'), findsOneWidget);
      expect(find.text('Únicas'), findsOneWidget);
    });

    testWidgets('should display completion percentage',
        (WidgetTester tester) async {
      // Arrange
      final stats = createStatistics(completionPercentage: 75.5);

      // Act
      await tester.pumpWidget(createWidgetUnderTest(statistics: stats));

      // Assert
      expect(find.text('75.5%'), findsOneWidget);
      expect(find.text('Completitud'), findsOneWidget);
    });

    testWidgets('should display elemental balance - Yes',
        (WidgetTester tester) async {
      // Arrange
      final stats = createStatistics(hasElementalBalance: true);

      // Act
      await tester.pumpWidget(createWidgetUnderTest(statistics: stats));

      // Assert
      expect(find.text('Sí'), findsOneWidget);
      expect(find.text('Balance'), findsOneWidget);
    });

    testWidgets('should display elemental balance - No',
        (WidgetTester tester) async {
      // Arrange
      final stats = createStatistics(hasElementalBalance: false);

      // Act
      await tester.pumpWidget(createWidgetUnderTest(statistics: stats));

      // Assert
      expect(find.text('No'), findsOneWidget);
    });

    testWidgets('should show collection icon',
        (WidgetTester tester) async {
      // Arrange
      final stats = createStatistics();

      // Act
      await tester.pumpWidget(createWidgetUnderTest(statistics: stats));

      // Assert
      expect(find.byIcon(Icons.auto_awesome), findsOneWidget);
    });

    testWidgets('should show all stat icons',
        (WidgetTester tester) async {
      // Arrange
      final stats = createStatistics();

      // Act
      await tester.pumpWidget(createWidgetUnderTest(statistics: stats));

      // Assert
      expect(find.byIcon(Icons.collections), findsOneWidget);
      expect(find.byIcon(Icons.star), findsOneWidget);
      expect(find.byIcon(Icons.trending_up), findsOneWidget);
      expect(find.byIcon(Icons.favorite), findsOneWidget);
    });
  });
}

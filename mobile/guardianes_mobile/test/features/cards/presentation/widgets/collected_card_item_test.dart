import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/card.dart' as game_card;
import 'package:guardianes_mobile/features/cards/domain/entities/collected_card.dart';
import 'package:guardianes_mobile/features/cards/presentation/widgets/collected_card_item.dart';

void main() {
  game_card.Card createTestCard({
    int id = 1,
    String name = 'Fire Dragon',
    game_card.CardElement element = game_card.CardElement.fire,
    game_card.CardRarity rarity = game_card.CardRarity.legendary,
    int attackPower = 100,
    int defensePower = 80,
    int energyCost = 5,
  }) {
    return game_card.Card(
      id: id,
      name: name,
      description: 'A test card',
      element: element,
      rarity: rarity,
      attackPower: attackPower,
      defensePower: defensePower,
      energyCost: energyCost,
      qrCode: 'test_qr_$id',
      createdAt: DateTime(2025, 1, 1),
      isActive: true,
    );
  }

  CollectedCard createCollectedCard({
    game_card.Card? card,
    int count = 1,
  }) {
    return CollectedCard(
      card: card ?? createTestCard(),
      count: count,
      firstCollectedAt: DateTime(2025, 1, 1),
      lastCollectedAt: DateTime(2025, 1, 2),
    );
  }

  Widget createWidgetUnderTest({
    required CollectedCard collectedCard,
    VoidCallback? onTap,
  }) {
    return MaterialApp(
      home: Scaffold(
        body: SizedBox(
          height: 200,
          width: 150,
          child: CollectedCardItem(
            collectedCard: collectedCard,
            onTap: onTap ?? () {},
          ),
        ),
      ),
    );
  }

  group('CollectedCardItem', () {
    testWidgets('should display card name', (WidgetTester tester) async {
      // Arrange
      final card = createTestCard(name: 'Water Spirit');
      final collectedCard = createCollectedCard(card: card);

      // Act
      await tester.pumpWidget(createWidgetUnderTest(collectedCard: collectedCard));

      // Assert
      expect(find.text('Water Spirit'), findsOneWidget);
    });

    testWidgets('should display element name and emoji',
        (WidgetTester tester) async {
      // Arrange
      final card = createTestCard(element: game_card.CardElement.earth);
      final collectedCard = createCollectedCard(card: card);

      // Act
      await tester.pumpWidget(createWidgetUnderTest(collectedCard: collectedCard));

      // Assert
      expect(find.text('Tierra'), findsOneWidget);
      expect(find.text('🌍'), findsOneWidget);
    });

    testWidgets('should display rarity badge', (WidgetTester tester) async {
      // Arrange
      final card = createTestCard(rarity: game_card.CardRarity.epic);
      final collectedCard = createCollectedCard(card: card);

      // Act
      await tester.pumpWidget(createWidgetUnderTest(collectedCard: collectedCard));

      // Assert
      expect(find.text('Épico'), findsOneWidget);
    });

    testWidgets('should display count badge when count > 1',
        (WidgetTester tester) async {
      // Arrange
      final collectedCard = createCollectedCard(count: 3);

      // Act
      await tester.pumpWidget(createWidgetUnderTest(collectedCard: collectedCard));

      // Assert
      expect(find.text('x3'), findsOneWidget);
    });

    testWidgets('should not display count badge when count is 1',
        (WidgetTester tester) async {
      // Arrange
      final collectedCard = createCollectedCard(count: 1);

      // Act
      await tester.pumpWidget(createWidgetUnderTest(collectedCard: collectedCard));

      // Assert
      expect(find.text('x1'), findsNothing);
    });

    testWidgets('should display attack power stat',
        (WidgetTester tester) async {
      // Arrange
      final card = createTestCard(attackPower: 75);
      final collectedCard = createCollectedCard(card: card);

      // Act
      await tester.pumpWidget(createWidgetUnderTest(collectedCard: collectedCard));

      // Assert
      expect(find.text('75'), findsOneWidget);
      expect(find.text('⚔️'), findsOneWidget);
    });

    testWidgets('should display defense power stat',
        (WidgetTester tester) async {
      // Arrange
      final card = createTestCard(defensePower: 60);
      final collectedCard = createCollectedCard(card: card);

      // Act
      await tester.pumpWidget(createWidgetUnderTest(collectedCard: collectedCard));

      // Assert
      expect(find.text('60'), findsOneWidget);
      expect(find.text('🛡️'), findsOneWidget);
    });

    testWidgets('should display energy cost stat', (WidgetTester tester) async {
      // Arrange
      final card = createTestCard(energyCost: 4);
      final collectedCard = createCollectedCard(card: card);

      // Act
      await tester.pumpWidget(createWidgetUnderTest(collectedCard: collectedCard));

      // Assert
      expect(find.text('4'), findsOneWidget);
      expect(find.text('⚡'), findsOneWidget);
    });

    testWidgets('should call onTap when card is tapped',
        (WidgetTester tester) async {
      // Arrange
      var tapped = false;
      final collectedCard = createCollectedCard();

      await tester.pumpWidget(createWidgetUnderTest(
        collectedCard: collectedCard,
        onTap: () => tapped = true,
      ));

      // Act
      await tester.tap(find.byType(InkWell));
      await tester.pumpAndSettle();

      // Assert
      expect(tapped, true);
    });

    testWidgets('should display fire element gradient',
        (WidgetTester tester) async {
      // Arrange
      final card = createTestCard(element: game_card.CardElement.fire);
      final collectedCard = createCollectedCard(card: card);

      // Act
      await tester.pumpWidget(createWidgetUnderTest(collectedCard: collectedCard));

      // Assert - just verify it renders without error
      expect(find.text('Fuego'), findsOneWidget);
      expect(find.text('🔥'), findsOneWidget);
    });

    testWidgets('should display air element data', (WidgetTester tester) async {
      // Arrange
      final card = createTestCard(element: game_card.CardElement.air);
      final collectedCard = createCollectedCard(card: card);

      // Act
      await tester.pumpWidget(createWidgetUnderTest(collectedCard: collectedCard));

      // Assert
      expect(find.text('Aire'), findsOneWidget);
      expect(find.text('🌪️'), findsOneWidget);
    });
  });
}

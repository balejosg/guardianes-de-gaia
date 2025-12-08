import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/card.dart' as game_card;
import 'package:guardianes_mobile/features/cards/domain/entities/card_collection.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/collected_card.dart';
import 'package:guardianes_mobile/features/cards/presentation/widgets/card_collection_grid.dart';

void main() {
  game_card.Card createTestCard({int id = 1, String name = 'Test Card'}) {
    return game_card.Card(
      id: id,
      name: name,
      description: 'A test card',
      element: game_card.CardElement.fire,
      rarity: game_card.CardRarity.common,
      attackPower: 50,
      defensePower: 30,
      energyCost: 3,
      qrCode: 'test_qr_$id',
      createdAt: DateTime(2025, 1, 1),
      isActive: true,
    );
  }

  CollectedCard createCollectedCard({game_card.Card? card, int count = 1}) {
    return CollectedCard(
      card: card ?? createTestCard(),
      count: count,
      firstCollectedAt: DateTime(2025, 1, 1),
      lastCollectedAt: DateTime(2025, 1, 2),
    );
  }

  Widget createWidgetUnderTest({required CardCollection collection}) {
    return MaterialApp(
      home: Scaffold(
        body: SingleChildScrollView(
          child: CardCollectionGrid(
            collection: collection,
            onCardTap: (_) {},
          ),
        ),
      ),
    );
  }

  group('CardCollectionGrid', () {
    testWidgets('should show empty state when no cards',
        (WidgetTester tester) async {
      // Arrange
      final collection = CardCollection(
        guardianId: 1,
        cards: [],
        createdAt: DateTime(2025, 1, 1),
      );

      // Act
      await tester.pumpWidget(createWidgetUnderTest(collection: collection));

      // Assert
      expect(find.text('No tienes cartas aún'), findsOneWidget);
      expect(find.text('Escanea códigos QR para comenzar tu colección'), findsOneWidget);
      expect(find.text('Escanear Carta'), findsOneWidget);
    });

    testWidgets('should show grid when collection has cards',
        (WidgetTester tester) async {
      // Arrange
      final collection = CardCollection(
        guardianId: 1,
        cards: [
          createCollectedCard(card: createTestCard(id: 1, name: 'Fire Dragon')),
          createCollectedCard(card: createTestCard(id: 2, name: 'Water Spirit')),
        ],
        createdAt: DateTime(2025, 1, 1),
      );

      // Act
      await tester.pumpWidget(createWidgetUnderTest(collection: collection));

      // Assert
      expect(find.byType(GridView), findsOneWidget);
      expect(find.text('Fire Dragon'), findsOneWidget);
      expect(find.text('Water Spirit'), findsOneWidget);
    });

    testWidgets('should show empty state icon',
        (WidgetTester tester) async {
      // Arrange
      final collection = CardCollection(
        guardianId: 1,
        cards: [],
        createdAt: DateTime(2025, 1, 1),
      );

      // Act
      await tester.pumpWidget(createWidgetUnderTest(collection: collection));

      // Assert
      expect(find.byIcon(Icons.collections_bookmark_outlined), findsOneWidget);
    });

    testWidgets('should call onCardTap when card tapped',
        (WidgetTester tester) async {
      // Arrange
      CollectedCard? tappedCard;
      final collection = CardCollection(
        guardianId: 1,
        cards: [
          createCollectedCard(card: createTestCard(id: 1, name: 'Test Card')),
        ],
        createdAt: DateTime(2025, 1, 1),
      );

      await tester.pumpWidget(MaterialApp(
        home: Scaffold(
          body: SingleChildScrollView(
            child: CardCollectionGrid(
              collection: collection,
              onCardTap: (card) => tappedCard = card,
            ),
          ),
        ),
      ));

      // Act
      await tester.tap(find.text('Test Card'));
      await tester.pumpAndSettle();

      // Assert
      expect(tappedCard, isNotNull);
      expect(tappedCard!.card.name, 'Test Card');
    });
  });
}

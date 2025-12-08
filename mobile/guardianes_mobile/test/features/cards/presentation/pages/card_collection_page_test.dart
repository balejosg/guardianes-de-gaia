import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:mockito/mockito.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/card.dart' as game_card;
import 'package:guardianes_mobile/features/cards/domain/entities/card_collection.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/collected_card.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/collection_statistics.dart';
import 'package:guardianes_mobile/features/cards/presentation/bloc/card_bloc.dart';
import 'package:guardianes_mobile/features/cards/presentation/bloc/card_state.dart';
import 'package:guardianes_mobile/features/cards/presentation/pages/card_collection_page.dart';

// Import existing mock
import '../bloc/card_bloc_test.mocks.dart';

void main() {
  late MockCardBloc mockCardBloc;

  setUp(() {
    mockCardBloc = MockCardBloc();
    when(mockCardBloc.stream).thenAnswer((_) => Stream.value(CardInitial()));
  });

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

  Widget createWidgetUnderTest({int guardianId = 1}) {
    return MaterialApp(
      home: BlocProvider<CardBloc>.value(
        value: mockCardBloc,
        child: CardCollectionPage(guardianId: guardianId),
      ),
    );
  }

  group('CardCollectionPage', () {
    testWidgets('should display app bar with title',
        (WidgetTester tester) async {
      // Arrange
      when(mockCardBloc.state).thenReturn(CardInitial());

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.text('Mi Colección'), findsOneWidget);
    });

    testWidgets('should show loading indicator when loading state',
        (WidgetTester tester) async {
      // Arrange
      when(mockCardBloc.state).thenReturn(CardLoading());

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.byType(CircularProgressIndicator), findsOneWidget);
    });

    testWidgets('should show error state with retry button',
        (WidgetTester tester) async {
      // Arrange
      when(mockCardBloc.state).thenReturn(const CardError(error: 'Network error'));

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.text('Error al cargar la colección'), findsOneWidget);
      expect(find.text('Network error'), findsOneWidget);
      expect(find.text('Intentar de nuevo'), findsOneWidget);
    });

    testWidgets('should show collection loaded state',
        (WidgetTester tester) async {
      // Arrange
      final card = createTestCard();
      final collectedCard = CollectedCard(
        card: card,
        count: 2,
        firstCollectedAt: DateTime(2025, 1, 1),
        lastCollectedAt: DateTime(2025, 1, 2),
      );
      final collection = CardCollection(
        guardianId: 1,
        cards: [collectedCard],
        createdAt: DateTime(2025, 1, 1),
      );

      when(mockCardBloc.state).thenReturn(CollectionLoaded(collection: collection));

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.text('Cartas Coleccionadas'), findsOneWidget);
      expect(find.text('1 cartas'), findsOneWidget);
    });

    testWidgets('should show search icon in app bar',
        (WidgetTester tester) async {
      // Arrange
      when(mockCardBloc.state).thenReturn(CardInitial());

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.byIcon(Icons.search), findsOneWidget);
    });

    testWidgets('should show FAB with scanner icon',
        (WidgetTester tester) async {
      // Arrange
      when(mockCardBloc.state).thenReturn(CardInitial());

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.byType(FloatingActionButton), findsOneWidget);
      expect(find.byIcon(Icons.qr_code_scanner), findsOneWidget);
    });

    testWidgets('should show placeholder when initial state',
        (WidgetTester tester) async {
      // Arrange
      when(mockCardBloc.state).thenReturn(CardInitial());

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.text('Carga la colección para empezar'), findsOneWidget);
    });
  });
}

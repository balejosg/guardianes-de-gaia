import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/card.dart' as game_card;
import 'package:guardianes_mobile/features/cards/domain/entities/card_scan_result.dart';
import 'package:guardianes_mobile/features/cards/presentation/widgets/scan_result_dialog.dart';

void main() {
  game_card.Card createTestCard({
    String name = 'Fire Dragon',
    game_card.CardElement element = game_card.CardElement.fire,
    game_card.CardRarity rarity = game_card.CardRarity.legendary,
    int attackPower = 100,
    int defensePower = 80,
    int energyCost = 5,
  }) {
    return game_card.Card(
      id: 1,
      name: name,
      description: 'A test card',
      element: element,
      rarity: rarity,
      attackPower: attackPower,
      defensePower: defensePower,
      energyCost: energyCost,
      qrCode: 'test_qr',
      createdAt: DateTime(2025, 1, 1),
      isActive: true,
    );
  }

  Widget createWidgetUnderTest({
    required CardScanResult result,
    VoidCallback? onContinue,
    VoidCallback? onClose,
    int guardianId = 1,
  }) {
    return MaterialApp(
      home: Scaffold(
        body: Builder(
          builder: (context) => Center(
            child: ElevatedButton(
              onPressed: () {
                showDialog(
                  context: context,
                  builder: (_) => ScanResultDialog(
                    result: result,
                    onContinue: onContinue ?? () {},
                    onClose: onClose ?? () {},
                    guardianId: guardianId,
                  ),
                );
              },
              child: const Text('Show Dialog'),
            ),
          ),
        ),
      ),
    );
  }

  group('ScanResultDialog', () {
    testWidgets('should display success state with card info',
        (WidgetTester tester) async {
      // Arrange
      final card = createTestCard();
      final result = CardScanResult.success(card: card, count: 1, isNew: true);

      await tester.pumpWidget(createWidgetUnderTest(result: result));
      await tester.tap(find.text('Show Dialog'));
      await tester.pumpAndSettle();

      // Assert
      expect(find.text('¡Éxito!'), findsOneWidget);
      expect(find.text('Fire Dragon'), findsOneWidget);
      expect(find.text('Escanear otra'), findsOneWidget);
      expect(find.text('Ver colección'), findsOneWidget);
    });

    testWidgets('should display error state', (WidgetTester tester) async {
      // Arrange
      final result = CardScanResult.error('QR code not found');

      await tester.pumpWidget(createWidgetUnderTest(result: result));
      await tester.tap(find.text('Show Dialog'));
      await tester.pumpAndSettle();

      // Assert
      expect(find.text('Error'), findsOneWidget);
      expect(find.text('QR code not found'), findsOneWidget);
      expect(find.text('Intentar de nuevo'), findsOneWidget);
      expect(find.text('Cerrar'), findsOneWidget);
    });

    testWidgets('should show rarity badge for legendary card',
        (WidgetTester tester) async {
      // Arrange
      final card = createTestCard(rarity: game_card.CardRarity.legendary);
      final result = CardScanResult.success(card: card, count: 1, isNew: true);

      await tester.pumpWidget(createWidgetUnderTest(result: result));
      await tester.tap(find.text('Show Dialog'));
      await tester.pumpAndSettle();

      // Assert
      expect(find.text('Legendario'), findsOneWidget);
    });

    testWidgets('should show element info', (WidgetTester tester) async {
      // Arrange
      final card = createTestCard(element: game_card.CardElement.water);
      final result = CardScanResult.success(card: card, count: 1, isNew: true);

      await tester.pumpWidget(createWidgetUnderTest(result: result));
      await tester.tap(find.text('Show Dialog'));
      await tester.pumpAndSettle();

      // Assert
      expect(find.text('Agua'), findsOneWidget);
      expect(find.text('💧'), findsOneWidget);
    });

    testWidgets('should show card stats', (WidgetTester tester) async {
      // Arrange
      final card = createTestCard(
        attackPower: 75,
        defensePower: 50,
        energyCost: 3,
      );
      final result = CardScanResult.success(card: card, count: 1, isNew: true);

      await tester.pumpWidget(createWidgetUnderTest(result: result));
      await tester.tap(find.text('Show Dialog'));
      await tester.pumpAndSettle();

      // Assert - stats should be visible
      expect(find.textContaining('75'), findsOneWidget);
      expect(find.textContaining('50'), findsOneWidget);
      expect(find.textContaining('3'), findsWidgets);
    });

    testWidgets('should show count when multiple copies',
        (WidgetTester tester) async {
      // Arrange
      final card = createTestCard();
      final result = CardScanResult.success(card: card, count: 5, isNew: false);

      await tester.pumpWidget(createWidgetUnderTest(result: result));
      await tester.tap(find.text('Show Dialog'));
      await tester.pumpAndSettle();

      // Assert
      expect(find.text('Tienes 5 copias'), findsOneWidget);
    });

    testWidgets('should call onContinue when "Escanear otra" pressed',
        (WidgetTester tester) async {
      // Arrange
      var continuePressed = false;
      final card = createTestCard();
      final result = CardScanResult.success(card: card, count: 1, isNew: true);

      await tester.pumpWidget(createWidgetUnderTest(
        result: result,
        onContinue: () => continuePressed = true,
      ));
      await tester.tap(find.text('Show Dialog'));
      await tester.pumpAndSettle();

      // Act
      await tester.tap(find.text('Escanear otra'));
      await tester.pumpAndSettle();

      // Assert
      expect(continuePressed, true);
    });

    testWidgets('should call onClose when "Cerrar" pressed on error',
        (WidgetTester tester) async {
      // Arrange
      var closePressed = false;
      final result = CardScanResult.error('Error occurred');

      await tester.pumpWidget(createWidgetUnderTest(
        result: result,
        onClose: () => closePressed = true,
      ));
      await tester.tap(find.text('Show Dialog'));
      await tester.pumpAndSettle();

      // Act
      await tester.tap(find.text('Cerrar'));
      await tester.pumpAndSettle();

      // Assert
      expect(closePressed, true);
    });
  });
}

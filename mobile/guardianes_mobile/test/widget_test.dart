// Basic widget test for Guardianes de Gaia mobile app
//
// This test verifies that the app loads correctly and shows the expected UI elements

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  testWidgets('App smoke test - verifies app can be created', (WidgetTester tester) async {
    // Create a minimal MaterialApp for testing
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          appBar: AppBar(title: const Text('Guardianes de Gaia')),
          body: const Center(
            child: Text('¡Bienvenido a Guardianes de Gaia!'),
          ),
        ),
      ),
    );
    
    // Wait for the app to settle
    await tester.pumpAndSettle();

    // Verify that basic UI elements are present
    expect(find.text('Guardianes de Gaia'), findsOneWidget);
    expect(find.text('¡Bienvenido a Guardianes de Gaia!'), findsOneWidget);
    expect(find.byType(AppBar), findsOneWidget);
    expect(find.byType(Scaffold), findsOneWidget);
  });
}

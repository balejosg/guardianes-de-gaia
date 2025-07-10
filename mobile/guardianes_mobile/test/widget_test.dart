// This is a basic Flutter widget test.
//
// To perform an interaction with a widget in your test, use the WidgetTester
// utility in the flutter_test package. For example, you can send tap and scroll
// gestures. You can also use WidgetTester to find child widgets in the widget
// tree, read text, and verify that the values of widget properties are correct.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:guardianes_mobile/main.dart';

void main() {
  testWidgets('App loads and shows login page', (WidgetTester tester) async {
    // Build our app and trigger a frame.
    await tester.pumpWidget(const GuardianesApp());

    // Wait for async operations to complete
    await tester.pumpAndSettle();

    // Verify that login elements are present
    expect(find.text('Guardianes de Gaia'), findsOneWidget);
    expect(find.text('Username'), findsOneWidget);
    expect(find.text('Password'), findsOneWidget);
  });
}

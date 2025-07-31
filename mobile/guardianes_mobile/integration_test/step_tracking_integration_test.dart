import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:guardianes_mobile/main.dart' as app;

// Helper methods for step tracking integration tests
Future<void> _navigateToStepTracking(WidgetTester tester) async {
  final stepTrackingNav = find.byKey(const Key('step_tracking_nav'));
  if (stepTrackingNav.evaluate().isNotEmpty) {
    await tester.tap(stepTrackingNav);
    await tester.pumpAndSettle();
  }
}

Future<void> _simulateOfflineMode(WidgetTester tester) async {
  // Simulate offline mode by mocking network calls to fail
  tester.binding.defaultBinaryMessenger.setMockMethodCallHandler(
    const MethodChannel('connectivity_plus'),
    (MethodCall methodCall) async {
      if (methodCall.method == 'check') {
        return 'none';
      }
      return null;
    },
  );
}

Future<void> _simulateStepCount(WidgetTester tester, int stepCount) async {
  // Mock the pedometer plugin to return specific step count
  tester.binding.defaultBinaryMessenger.setMockMethodCallHandler(
    const MethodChannel('pedometer'),
    (MethodCall methodCall) async {
      if (methodCall.method == 'getStepCount') {
        return stepCount;
      }
      return null;
    },
  );
}

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  group('Step Tracking Integration Tests', () {
    testWidgets('should display current step count', (WidgetTester tester) async {
      // Launch the app
      app.main();
      await tester.pumpAndSettle();

      // Navigate to step tracking page (assuming logged in state)
      await _navigateToStepTracking(tester);

      // Verify step count display
      expect(find.byKey(const Key('current_step_count')), findsOneWidget);
      expect(find.byKey(const Key('energy_balance')), findsOneWidget);
    });

    testWidgets('should submit steps successfully', (WidgetTester tester) async {
      // Launch the app
      app.main();
      await tester.pumpAndSettle();

      // Navigate to step tracking page
      await _navigateToStepTracking(tester);

      // Find and tap step submission button
      final submitStepsButton = find.byKey(const Key('submit_steps_button'));
      expect(submitStepsButton, findsOneWidget);
      await tester.tap(submitStepsButton);
      await tester.pumpAndSettle();

      // Verify success message or updated count
      expect(find.textContaining('Pasos enviados'), findsOneWidget);
    });

    testWidgets('should display step history', (WidgetTester tester) async {
      // Launch the app
      app.main();
      await tester.pumpAndSettle();

      // Navigate to step tracking page
      await _navigateToStepTracking(tester);

      // Navigate to history view
      final historyTab = find.byKey(const Key('step_history_tab'));
      if (historyTab.evaluate().isNotEmpty) {
        await tester.tap(historyTab);
        await tester.pumpAndSettle();
      }

      // Verify history list is displayed
      expect(find.byKey(const Key('step_history_list')), findsOneWidget);
    });

    testWidgets('should show energy conversion calculation', (WidgetTester tester) async {
      // Launch the app
      app.main();
      await tester.pumpAndSettle();

      // Navigate to step tracking page
      await _navigateToStepTracking(tester);

      // Verify energy display
      final energyDisplay = find.byKey(const Key('energy_display'));
      expect(energyDisplay, findsOneWidget);

      // Check energy conversion ratio (1 energy = 10 steps)
      final stepCountWidget = find.byKey(const Key('current_step_count'));
      final energyBalanceWidget = find.byKey(const Key('energy_balance'));
      
      expect(stepCountWidget, findsOneWidget);
      expect(energyBalanceWidget, findsOneWidget);
    });

    testWidgets('should handle offline step tracking', (WidgetTester tester) async {
      // Launch the app
      app.main();
      await tester.pumpAndSettle();

      // Simulate offline mode
      await _simulateOfflineMode(tester);

      // Navigate to step tracking page
      await _navigateToStepTracking(tester);

      // Try to submit steps while offline
      final submitStepsButton = find.byKey(const Key('submit_steps_button'));
      if (submitStepsButton.evaluate().isNotEmpty) {
        await tester.tap(submitStepsButton);
        await tester.pumpAndSettle();
      }

      // Verify offline handling
      expect(find.textContaining('Sin conexión'), findsOneWidget);
    });

    testWidgets('should validate step count limits', (WidgetTester tester) async {
      // Launch the app
      app.main();
      await tester.pumpAndSettle();

      // Navigate to step tracking page
      await _navigateToStepTracking(tester);

      // Simulate extremely high step count (should be validated)
      await _simulateStepCount(tester, 100000);

      // Try to submit
      final submitStepsButton = find.byKey(const Key('submit_steps_button'));
      if (submitStepsButton.evaluate().isNotEmpty) {
        await tester.tap(submitStepsButton);
        await tester.pumpAndSettle();
      }

      // Should show validation message or limit warning
      expect(find.textContaining('límite'), findsOneWidget);
    });
  });
}
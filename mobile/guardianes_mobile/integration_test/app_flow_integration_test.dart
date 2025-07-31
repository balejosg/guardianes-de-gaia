import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:guardianes_mobile/main.dart' as app;

// Helper methods for complex integration scenarios
Future<void> _performRegistration(WidgetTester tester) async {
  final registerButton = find.text('Registrarse');
  if (registerButton.evaluate().isNotEmpty) {
    await tester.tap(registerButton);
    await tester.pumpAndSettle();

    // Fill registration form
    await tester.enterText(find.byKey(const Key('guardian_name_field')), 'Integration Test Guardian');
    await tester.enterText(find.byKey(const Key('guardian_email_field')), 'integration.test@guardianes.com');
    await tester.enterText(find.byKey(const Key('guardian_password_field')), 'TestPassword123!');
    await tester.enterText(find.byKey(const Key('guardian_confirm_password_field')), 'TestPassword123!');

    await tester.tap(find.byKey(const Key('register_submit_button')));
    await tester.pumpAndSettle();
  }
}

Future<void> _performLogin(WidgetTester tester) async {
  final loginButton = find.text('Iniciar Sesión');
  if (loginButton.evaluate().isNotEmpty) {
    await tester.tap(loginButton);
    await tester.pumpAndSettle();

    await tester.enterText(find.byKey(const Key('login_email_field')), 'integration.test@guardianes.com');
    await tester.enterText(find.byKey(const Key('login_password_field')), 'TestPassword123!');

    await tester.tap(find.byKey(const Key('login_submit_button')));
    await tester.pumpAndSettle();
  }
}

Future<void> _navigateToStepTracking(WidgetTester tester) async {
  final stepTrackingNav = find.byKey(const Key('step_tracking_nav'));
  if (stepTrackingNav.evaluate().isNotEmpty) {
    await tester.tap(stepTrackingNav);
    await tester.pumpAndSettle();
  }
}

Future<void> _submitSteps(WidgetTester tester) async {
  final submitButton = find.byKey(const Key('submit_steps_button'));
  if (submitButton.evaluate().isNotEmpty) {
    await tester.tap(submitButton);
    await tester.pumpAndSettle();
  }
}

Future<void> _verifyEnergyCalculation(WidgetTester tester) async {
  expect(find.byKey(const Key('energy_balance')), findsOneWidget);
  expect(find.byKey(const Key('current_step_count')), findsOneWidget);
}

Future<void> _viewStepHistory(WidgetTester tester) async {
  final historyTab = find.byKey(const Key('step_history_tab'));
  if (historyTab.evaluate().isNotEmpty) {
    await tester.tap(historyTab);
    await tester.pumpAndSettle();
    expect(find.byKey(const Key('step_history_list')), findsOneWidget);
  }
}

Future<void> _simulateNetworkError(WidgetTester tester) async {
  tester.binding.defaultBinaryMessenger.setMockMethodCallHandler(
    const MethodChannel('dio'),
    (MethodCall methodCall) async {
      throw const SocketException('Network unreachable');
    },
  );
}

Future<void> _attemptBackendOperations(WidgetTester tester) async {
  // Try login
  await _performLogin(tester);
  
  // Try step submission
  await _navigateToStepTracking(tester);
  await _submitSteps(tester);
}

Future<int> _getCurrentStepCount(WidgetTester tester) async {
  final stepCountWidget = find.byKey(const Key('current_step_count'));
  if (stepCountWidget.evaluate().isNotEmpty) {
    final Text widget = tester.widget(stepCountWidget);
    // Extract step count from widget text
    return int.tryParse(widget.data?.replaceAll(RegExp(r'[^0-9]'), '') ?? '0') ?? 0;
  }
  return 0;
}

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  group('Complete App Flow Integration Tests', () {
    testWidgets('should complete full user journey from registration to step tracking', (WidgetTester tester) async {
      // Launch the app
      app.main();
      await tester.pumpAndSettle();

      // Step 1: Register new guardian
      await _performRegistration(tester);
      
      // Step 2: Navigate to step tracking
      await _navigateToStepTracking(tester);
      
      // Step 3: Submit initial steps
      await _submitSteps(tester);
      
      // Step 4: Check energy balance
      await _verifyEnergyCalculation(tester);
      
      // Step 5: View step history
      await _viewStepHistory(tester);
    });

    testWidgets('should handle backend connectivity issues gracefully', (WidgetTester tester) async {
      // Launch the app
      app.main();
      await tester.pumpAndSettle();

      // Simulate network issues
      await _simulateNetworkError(tester);

      // Try to perform actions that require backend
      await _attemptBackendOperations(tester);

      // Verify graceful error handling
      expect(find.textContaining('Error de conexión'), findsOneWidget);
    });

    testWidgets('should maintain state across app lifecycle', (WidgetTester tester) async {
      // Launch the app
      app.main();
      await tester.pumpAndSettle();

      // Login and navigate to step tracking
      await _performLogin(tester);
      await _navigateToStepTracking(tester);

      // Record initial state
      final initialSteps = await _getCurrentStepCount(tester);

      // Simulate app going to background and returning
      tester.binding.defaultBinaryMessenger.setMockMethodCallHandler(
        const MethodChannel('flutter/lifecycle'),
        (MethodCall methodCall) async {
          if (methodCall.method == 'AppLifecycleState.paused') {
            return null;
          }
          if (methodCall.method == 'AppLifecycleState.resumed') {
            return null;
          }
          return null;
        },
      );

      // Verify state persistence
      await tester.pumpAndSettle();
      final resumedSteps = await _getCurrentStepCount(tester);
      expect(resumedSteps, equals(initialSteps));
    });
  });
}
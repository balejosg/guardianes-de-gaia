import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:guardianes_mobile/main.dart' as app;

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  group('Guardian Authentication Integration Tests', () {
    testWidgets('should complete full guardian registration flow', (WidgetTester tester) async {
      // Launch the app
      app.main();
      await tester.pumpAndSettle();

      // Verify initial state - should show login/register options
      expect(find.text('Guardianes de Gaia'), findsOneWidget);
      
      // Navigate to registration
      final registerButton = find.text('Registrarse');
      expect(registerButton, findsOneWidget);
      await tester.tap(registerButton);
      await tester.pumpAndSettle();

      // Fill registration form
      final nameField = find.byKey(const Key('guardian_name_field'));
      final emailField = find.byKey(const Key('guardian_email_field'));
      final passwordField = find.byKey(const Key('guardian_password_field'));
      final confirmPasswordField = find.byKey(const Key('guardian_confirm_password_field'));
      
      await tester.enterText(nameField, 'Test Guardian');
      await tester.enterText(emailField, 'test.guardian@example.com');
      await tester.enterText(passwordField, 'TestPassword123!');
      await tester.enterText(confirmPasswordField, 'TestPassword123!');
      
      // Submit registration
      final submitButton = find.byKey(const Key('register_submit_button'));
      await tester.tap(submitButton);
      await tester.pumpAndSettle();

      // Verify successful registration leads to home screen or login
      expect(find.text('Registro exitoso'), findsOneWidget);
    });

    testWidgets('should complete guardian login flow', (WidgetTester tester) async {
      // Launch the app
      app.main();
      await tester.pumpAndSettle();

      // Navigate to login
      final loginButton = find.text('Iniciar Sesión');
      if (loginButton.evaluate().isNotEmpty) {
        await tester.tap(loginButton);
        await tester.pumpAndSettle();
      }

      // Fill login form
      final emailField = find.byKey(const Key('login_email_field'));
      final passwordField = find.byKey(const Key('login_password_field'));
      
      await tester.enterText(emailField, 'test.guardian@example.com');
      await tester.enterText(passwordField, 'TestPassword123!');
      
      // Submit login
      final submitButton = find.byKey(const Key('login_submit_button'));
      await tester.tap(submitButton);
      await tester.pumpAndSettle();

      // Verify successful login leads to home screen
      expect(find.text('Bienvenido'), findsOneWidget);
    });

    testWidgets('should handle authentication errors gracefully', (WidgetTester tester) async {
      // Launch the app
      app.main();
      await tester.pumpAndSettle();

      // Navigate to login
      final loginButton = find.text('Iniciar Sesión');
      if (loginButton.evaluate().isNotEmpty) {
        await tester.tap(loginButton);
        await tester.pumpAndSettle();
      }

      // Try login with invalid credentials
      final emailField = find.byKey(const Key('login_email_field'));
      final passwordField = find.byKey(const Key('login_password_field'));
      
      await tester.enterText(emailField, 'invalid@example.com');
      await tester.enterText(passwordField, 'WrongPassword');
      
      // Submit login
      final submitButton = find.byKey(const Key('login_submit_button'));
      await tester.tap(submitButton);
      await tester.pumpAndSettle();

      // Verify error message is displayed
      expect(find.textContaining('Error'), findsOneWidget);
    });
  });
}
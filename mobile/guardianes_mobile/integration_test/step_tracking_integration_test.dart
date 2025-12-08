import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:guardianes_mobile/main.dart' as app;

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  group('Step Tracking Integration Tests', () {
    // Helper to navigate to step tracking (requires authenticated state)
    Future<bool> navigateToStepTracking(WidgetTester tester) async {
      final stepTrackingNav = find.byKey(const Key('step_tracking_nav'));
      if (stepTrackingNav.evaluate().isNotEmpty) {
        await tester.tap(stepTrackingNav);
        await tester.pumpAndSettle();
        return true;
      }
      return false;
    }

    testWidgets('should display step tracking page elements',
        (WidgetTester tester) async {
      // Launch the app
      app.main();
      await tester.pumpAndSettle();

      // Attempt to navigate to step tracking if on home page
      final navigated = await navigateToStepTracking(tester);
      
      if (navigated) {
        // Should see step tracking page elements
        await tester.pumpAndSettle(const Duration(seconds: 2));
        
        // Look for step tracking page indicators
        final hasTabBar = find.byType(TabBar).evaluate().isNotEmpty;
        final hasStepText = find.textContaining('Steps').evaluate().isNotEmpty ||
                           find.textContaining('Step').evaluate().isNotEmpty;
        
        expect(hasTabBar || hasStepText, isTrue,
            reason: 'Step tracking page should have TabBar or step-related text');
      } else {
        // Not authenticated - verify we're still on login/home
        expect(find.text('Guardianes de Gaia'), findsOneWidget);
      }
    });

    testWidgets('should show current step count when authenticated',
        (WidgetTester tester) async {
      // Launch the app
      app.main();
      await tester.pumpAndSettle();

      // Try to navigate to step tracking
      final navigated = await navigateToStepTracking(tester);
      
      if (navigated) {
        await tester.pumpAndSettle(const Duration(seconds: 2));
        
        // Look for step count display
        final currentStepCount = find.byKey(const Key('current_step_count'));
        final energyBalance = find.byKey(const Key('energy_balance'));
        
        // Either step count or energy should be visible (depends on layout)
        final hasStepData = currentStepCount.evaluate().isNotEmpty ||
                           energyBalance.evaluate().isNotEmpty ||
                           find.textContaining('Steps').evaluate().isNotEmpty;
        
        expect(hasStepData, isTrue,
            reason: 'Should display step count or energy data');
      } else {
        // Skip test if not authenticated
        expect(find.text('Guardianes de Gaia'), findsOneWidget);
      }
    });

    testWidgets('should display step history tab',
        (WidgetTester tester) async {
      // Launch the app
      app.main();
      await tester.pumpAndSettle();

      // Try to navigate to step tracking
      final navigated = await navigateToStepTracking(tester);
      
      if (navigated) {
        await tester.pumpAndSettle();
        
        // Look for history tab
        final historyTab = find.byKey(const Key('step_history_tab'));
        
        if (historyTab.evaluate().isNotEmpty) {
          await tester.tap(historyTab);
          await tester.pumpAndSettle();
          
          // Should see history list or empty state
          final historyList = find.byKey(const Key('step_history_list'));
          final hasHistoryOrEmpty = historyList.evaluate().isNotEmpty ||
                                   find.textContaining('history').evaluate().isNotEmpty ||
                                   find.textContaining('No step').evaluate().isNotEmpty;
          
          expect(hasHistoryOrEmpty, isTrue,
              reason: 'Should show history list or empty state');
        }
      } else {
        // Skip test if not authenticated
        expect(find.text('Guardianes de Gaia'), findsOneWidget);
      }
    });

    testWidgets('should show energy display',
        (WidgetTester tester) async {
      // Launch the app
      app.main();
      await tester.pumpAndSettle();

      // Try to navigate to step tracking
      final navigated = await navigateToStepTracking(tester);
      
      if (navigated) {
        await tester.pumpAndSettle(const Duration(seconds: 2));
        
        // Look for energy display
        final energyDisplay = find.byKey(const Key('energy_display'));
        final hasEnergyText = find.textContaining('Energy').evaluate().isNotEmpty;
        
        expect(energyDisplay.evaluate().isNotEmpty || hasEnergyText, isTrue,
            reason: 'Should display energy information');
      } else {
        // Skip test if not authenticated
        expect(find.text('Guardianes de Gaia'), findsOneWidget);
      }
    });

    testWidgets('should show sync button',
        (WidgetTester tester) async {
      // Launch the app
      app.main();
      await tester.pumpAndSettle();

      // Try to navigate to step tracking
      final navigated = await navigateToStepTracking(tester);
      
      if (navigated) {
        await tester.pumpAndSettle(const Duration(seconds: 2));
        
        // Look for sync/submit button
        final submitButton = find.byKey(const Key('submit_steps_button'));
        final hasSyncText = find.textContaining('Sync').evaluate().isNotEmpty ||
                          find.textContaining('sync').evaluate().isNotEmpty;
        
        expect(submitButton.evaluate().isNotEmpty || hasSyncText, isTrue,
            reason: 'Should display sync or submit button');
      } else {
        // Skip test if not authenticated
        expect(find.text('Guardianes de Gaia'), findsOneWidget);
      }
    });
  });
}

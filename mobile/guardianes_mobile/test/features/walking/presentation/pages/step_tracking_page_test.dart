import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:mockito/mockito.dart';
import 'package:mockito/annotations.dart';
import 'package:guardianes_mobile/features/walking/domain/entities/daily_step_aggregate.dart';
import 'package:guardianes_mobile/features/walking/presentation/bloc/step_bloc.dart';
import 'package:guardianes_mobile/features/walking/presentation/bloc/step_event.dart';
import 'package:guardianes_mobile/features/walking/presentation/bloc/step_state.dart';
import 'package:guardianes_mobile/features/walking/presentation/pages/step_tracking_page.dart';
import 'package:guardianes_mobile/features/walking/presentation/widgets/step_counter_widget.dart';
import 'package:guardianes_mobile/features/walking/presentation/widgets/step_history_widget.dart';

import 'step_tracking_page_test.mocks.dart';

@GenerateMocks([StepBloc])
void main() {
  late MockStepBloc mockStepBloc;

  setUp(() {
    mockStepBloc = MockStepBloc();
    when(mockStepBloc.stream).thenAnswer((_) => Stream.value(StepInitial()));
  });

  Widget createWidgetUnderTest() {
    return MaterialApp(
      home: BlocProvider<StepBloc>.value(
        value: mockStepBloc,
        child: const StepTrackingPage(),
      ),
    );
  }

  group('StepTrackingPage', () {
    testWidgets('should display app bar with correct title',
        (WidgetTester tester) async {
      // Arrange
      when(mockStepBloc.state).thenReturn(StepInitial());

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.text('Step Tracking'), findsOneWidget);
      expect(find.byType(AppBar), findsOneWidget);
    });

    testWidgets('should display step counter widget',
        (WidgetTester tester) async {
      // Arrange
      when(mockStepBloc.state).thenReturn(StepInitial());

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.byType(StepCounterWidget), findsOneWidget);
    });

    testWidgets('should display floating action button for manual step entry',
        (WidgetTester tester) async {
      // Arrange
      when(mockStepBloc.state).thenReturn(StepInitial());

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.byType(FloatingActionButton), findsOneWidget);
      expect(find.byIcon(Icons.add), findsOneWidget);
    });

    testWidgets('should display step history widget in tab',
        (WidgetTester tester) async {
      // Arrange
      when(mockStepBloc.state).thenReturn(StepInitial());

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.byType(TabBar), findsOneWidget);
      expect(find.text('Today'), findsOneWidget);
      expect(find.text('History'), findsOneWidget);
    });

    testWidgets('should switch between tabs correctly',
        (WidgetTester tester) async {
      // Arrange
      when(mockStepBloc.state).thenReturn(StepInitial());

      // Act
      await tester.pumpWidget(createWidgetUnderTest());
      await tester.tap(find.text('History'));
      await tester.pumpAndSettle();

      // Assert
      expect(find.byType(StepHistoryWidget), findsOneWidget);
    });

    testWidgets('should trigger GetCurrentStepsEvent on page initialization',
        (WidgetTester tester) async {
      // Arrange
      when(mockStepBloc.state).thenReturn(StepInitial());

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      verify(mockStepBloc.add(any)).called(1);
    });

    testWidgets('should show manual step entry dialog when FAB is pressed',
        (WidgetTester tester) async {
      // Arrange
      when(mockStepBloc.state).thenReturn(StepInitial());

      // Act
      await tester.pumpWidget(createWidgetUnderTest());
      await tester.tap(find.byType(FloatingActionButton));
      await tester.pumpAndSettle();

      // Assert
      expect(find.byType(AlertDialog), findsOneWidget);
      expect(find.text('Add Steps'), findsOneWidget);
      expect(find.byType(TextFormField), findsOneWidget);
    });

    testWidgets('should validate step input in manual entry dialog',
        (WidgetTester tester) async {
      // Arrange
      when(mockStepBloc.state).thenReturn(StepInitial());

      // Act
      await tester.pumpWidget(createWidgetUnderTest());
      await tester.tap(find.byType(FloatingActionButton));
      await tester.pumpAndSettle();

      // Enter invalid input
      await tester.enterText(find.byType(TextFormField), '-100');
      await tester.tap(find.text('Add Steps'));
      await tester.pumpAndSettle();

      // Assert
      expect(find.text('Please enter a positive number'), findsOneWidget);
    });

    testWidgets('should submit steps when valid input is provided',
        (WidgetTester tester) async {
      // Arrange
      when(mockStepBloc.state).thenReturn(StepInitial());

      // Act
      await tester.pumpWidget(createWidgetUnderTest());
      await tester.tap(find.byType(FloatingActionButton));
      await tester.pumpAndSettle();

      // Enter valid input
      await tester.enterText(find.byType(TextFormField), '2500');
      await tester.tap(find.text('Add Steps'));
      await tester.pumpAndSettle();

      // Assert
      verify(mockStepBloc.add(argThat(isA<SubmitStepsEvent>()))).called(1);
    });

    testWidgets('should display success message when steps are submitted',
        (WidgetTester tester) async {
      // Arrange
      when(mockStepBloc.state).thenReturn(StepSubmitted());

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.byType(SnackBar), findsOneWidget);
      expect(find.text('Steps submitted successfully!'), findsOneWidget);
    });

    testWidgets('should display error message when submission fails',
        (WidgetTester tester) async {
      // Arrange
      when(mockStepBloc.state).thenReturn(const StepSubmissionError(
        message: 'Network error',
      ));

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.byType(SnackBar), findsOneWidget);
      expect(find.text('Error: Network error'), findsOneWidget);
    });

    testWidgets('should refresh data when pull to refresh is triggered',
        (WidgetTester tester) async {
      // Arrange
      when(mockStepBloc.state).thenReturn(const StepLoaded(
        currentSteps: DailyStepAggregate(
          guardianId: 1,
          date: '2025-07-16',
          totalSteps: 5000,
        ),
      ));

      // Act
      await tester.pumpWidget(createWidgetUnderTest());
      await tester.fling(find.byType(RefreshIndicator), const Offset(0, 300), 1000);
      await tester.pumpAndSettle();

      // Assert
      verify(mockStepBloc.add(any)).called(2); // Initial load + refresh
    });

    testWidgets('should handle different guardian IDs correctly',
        (WidgetTester tester) async {
      // Arrange
      when(mockStepBloc.state).thenReturn(const StepLoaded(
        currentSteps: DailyStepAggregate(
          guardianId: 42,
          date: '2025-07-16',
          totalSteps: 5000,
        ),
      ));

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.byType(StepCounterWidget), findsOneWidget);
      // Widget should display data for guardian 42
    });

    testWidgets('should display loading state during step submission',
        (WidgetTester tester) async {
      // Arrange
      when(mockStepBloc.state).thenReturn(StepSubmitting());

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.byType(CircularProgressIndicator), findsAtLeastNWidgets(1));
    });

    testWidgets('should handle step history loading correctly',
        (WidgetTester tester) async {
      // Arrange
      when(mockStepBloc.state).thenReturn(StepLoading());

      // Act
      await tester.pumpWidget(createWidgetUnderTest());
      await tester.tap(find.text('History'));
      await tester.pumpAndSettle();

      // Assert
      expect(find.byType(CircularProgressIndicator), findsOneWidget);
    });

    testWidgets('should trigger GetStepHistoryEvent when history tab is selected',
        (WidgetTester tester) async {
      // Arrange
      when(mockStepBloc.state).thenReturn(StepInitial());

      // Act
      await tester.pumpWidget(createWidgetUnderTest());
      await tester.tap(find.text('History'));
      await tester.pumpAndSettle();

      // Assert
      verify(mockStepBloc.add(argThat(isA<GetStepHistoryEvent>()))).called(1);
    });

    testWidgets('should close manual entry dialog when cancelled',
        (WidgetTester tester) async {
      // Arrange
      when(mockStepBloc.state).thenReturn(StepInitial());

      // Act
      await tester.pumpWidget(createWidgetUnderTest());
      await tester.tap(find.byType(FloatingActionButton));
      await tester.pumpAndSettle();

      // Tap cancel
      await tester.tap(find.text('Cancel'));
      await tester.pumpAndSettle();

      // Assert
      expect(find.byType(AlertDialog), findsNothing);
    });

    testWidgets('should maintain tab state across rebuilds',
        (WidgetTester tester) async {
      // Arrange
      when(mockStepBloc.state).thenReturn(StepInitial());

      // Act
      await tester.pumpWidget(createWidgetUnderTest());
      await tester.tap(find.text('History'));
      await tester.pumpAndSettle();

      // Trigger rebuild
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.byType(StepHistoryWidget), findsOneWidget);
    });
  });
}
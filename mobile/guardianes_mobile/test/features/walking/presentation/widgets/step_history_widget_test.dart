import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:mockito/mockito.dart';
import 'package:mockito/annotations.dart';
import 'package:guardianes_mobile/features/walking/domain/entities/daily_step_aggregate.dart';
import 'package:guardianes_mobile/features/walking/presentation/bloc/step_bloc.dart';
import 'package:guardianes_mobile/features/walking/presentation/bloc/step_state.dart';
import 'package:guardianes_mobile/features/walking/presentation/widgets/step_history_widget.dart';

import 'step_history_widget_test.mocks.dart';

@GenerateMocks([StepBloc])
void main() {
  late MockStepBloc mockStepBloc;

  setUp(() {
    mockStepBloc = MockStepBloc();
    when(mockStepBloc.stream).thenAnswer((_) => Stream.value(StepInitial()));
  });

  Widget createWidgetUnderTest() {
    return MaterialApp(
      home: Scaffold(
        body: BlocProvider<StepBloc>.value(
          value: mockStepBloc,
          child: const StepHistoryWidget(),
        ),
      ),
    );
  }

  group('StepHistoryWidget', () {
    testWidgets('should display loading indicator when state is StepLoading',
        (WidgetTester tester) async {
      // Arrange
      when(mockStepBloc.state).thenReturn(StepLoading());

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.byType(CircularProgressIndicator), findsOneWidget);
    });

    testWidgets('should display step history when state is StepHistoryLoaded',
        (WidgetTester tester) async {
      // Arrange
      const stepHistory = [
        DailyStepAggregate(
          guardianId: 1,
          date: '2025-07-14',
          totalSteps: 5000,
        ),
        DailyStepAggregate(
          guardianId: 1,
          date: '2025-07-15',
          totalSteps: 6000,
        ),
        DailyStepAggregate(
          guardianId: 1,
          date: '2025-07-16',
          totalSteps: 7000,
        ),
      ];
      when(mockStepBloc.state).thenReturn(const StepHistoryLoaded(stepHistory: stepHistory));

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.text('2025-07-14'), findsOneWidget);
      expect(find.text('5,000 steps'), findsOneWidget);
      expect(find.text('2025-07-15'), findsOneWidget);
      expect(find.text('6,000 steps'), findsOneWidget);
      expect(find.text('2025-07-16'), findsOneWidget);
      expect(find.text('7,000 steps'), findsOneWidget);
    });

    testWidgets('should display empty state when no history available',
        (WidgetTester tester) async {
      // Arrange
      when(mockStepBloc.state).thenReturn(const StepHistoryLoaded(stepHistory: []));

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.text('No step history available'), findsOneWidget);
      expect(find.byIcon(Icons.history), findsOneWidget);
    });

    testWidgets('should display goal reached indicator for days with 8000+ steps',
        (WidgetTester tester) async {
      // Arrange
      const stepHistory = [
        DailyStepAggregate(
          guardianId: 1,
          date: '2025-07-14',
          totalSteps: 7999,
        ),
        DailyStepAggregate(
          guardianId: 1,
          date: '2025-07-15',
          totalSteps: 8000,
        ),
        DailyStepAggregate(
          guardianId: 1,
          date: '2025-07-16',
          totalSteps: 10000,
        ),
      ];
      when(mockStepBloc.state).thenReturn(const StepHistoryLoaded(stepHistory: stepHistory));

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.byIcon(Icons.check_circle), findsNWidgets(2)); // Only last 2 days reached goal
    });

    testWidgets('should display energy calculation correctly',
        (WidgetTester tester) async {
      // Arrange
      const stepHistory = [
        DailyStepAggregate(
          guardianId: 1,
          date: '2025-07-14',
          totalSteps: 2509,
        ),
        DailyStepAggregate(
          guardianId: 1,
          date: '2025-07-15',
          totalSteps: 5000,
        ),
      ];
      when(mockStepBloc.state).thenReturn(const StepHistoryLoaded(stepHistory: stepHistory));

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.text('250 energy'), findsOneWidget); // 2509 steps = 250 energy
      expect(find.text('500 energy'), findsOneWidget); // 5000 steps = 500 energy
    });

    testWidgets('should display error message when state is StepError',
        (WidgetTester tester) async {
      // Arrange
      when(mockStepBloc.state).thenReturn(const StepError(message: 'Network error'));

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.text('Error: Network error'), findsOneWidget);
      expect(find.byIcon(Icons.error), findsOneWidget);
    });

    testWidgets('should display initial state correctly',
        (WidgetTester tester) async {
      // Arrange
      when(mockStepBloc.state).thenReturn(StepInitial());

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.text('No step history available'), findsOneWidget);
    });

    testWidgets('should display scrollable list for many entries',
        (WidgetTester tester) async {
      // Arrange
      final stepHistory = List.generate(
        20,
        (index) => DailyStepAggregate(
          guardianId: 1,
          date: '2025-07-${(index + 1).toString().padLeft(2, '0')}',
          totalSteps: (index + 1) * 1000,
        ),
      );
      when(mockStepBloc.state).thenReturn(StepHistoryLoaded(stepHistory: stepHistory));

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.byType(ListView), findsOneWidget);
      expect(find.text('2025-07-01'), findsOneWidget);
      expect(find.text('1,000 steps'), findsOneWidget);
    });

    testWidgets('should format dates correctly',
        (WidgetTester tester) async {
      // Arrange
      const stepHistory = [
        DailyStepAggregate(
          guardianId: 1,
          date: '2025-12-25',
          totalSteps: 5000,
        ),
        DailyStepAggregate(
          guardianId: 1,
          date: '2025-01-01',
          totalSteps: 6000,
        ),
      ];
      when(mockStepBloc.state).thenReturn(const StepHistoryLoaded(stepHistory: stepHistory));

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.text('2025-12-25'), findsOneWidget);
      expect(find.text('2025-01-01'), findsOneWidget);
    });

    testWidgets('should handle zero steps correctly',
        (WidgetTester tester) async {
      // Arrange
      const stepHistory = [
        DailyStepAggregate(
          guardianId: 1,
          date: '2025-07-14',
          totalSteps: 0,
        ),
      ];
      when(mockStepBloc.state).thenReturn(const StepHistoryLoaded(stepHistory: stepHistory));

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.text('0 steps'), findsOneWidget);
      expect(find.text('0 energy'), findsOneWidget);
    });

    testWidgets('should display weekly summary when available',
        (WidgetTester tester) async {
      // Arrange
      const stepHistory = [
        DailyStepAggregate(guardianId: 1, date: '2025-07-14', totalSteps: 5000),
        DailyStepAggregate(guardianId: 1, date: '2025-07-15', totalSteps: 6000),
        DailyStepAggregate(guardianId: 1, date: '2025-07-16', totalSteps: 7000),
        DailyStepAggregate(guardianId: 1, date: '2025-07-17', totalSteps: 8000),
        DailyStepAggregate(guardianId: 1, date: '2025-07-18', totalSteps: 4000),
        DailyStepAggregate(guardianId: 1, date: '2025-07-19', totalSteps: 9000),
        DailyStepAggregate(guardianId: 1, date: '2025-07-20', totalSteps: 6000),
      ];
      when(mockStepBloc.state).thenReturn(const StepHistoryLoaded(stepHistory: stepHistory));

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.text('Weekly Total: 45,000 steps'), findsOneWidget);
      expect(find.text('Average: 6,429 steps/day'), findsOneWidget);
    });
  });
}
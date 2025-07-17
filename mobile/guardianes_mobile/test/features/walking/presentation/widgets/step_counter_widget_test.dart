import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:mockito/mockito.dart';
import 'package:mockito/annotations.dart';
import 'package:guardianes_mobile/features/walking/domain/entities/daily_step_aggregate.dart';
import 'package:guardianes_mobile/features/walking/presentation/bloc/step_bloc.dart';
import 'package:guardianes_mobile/features/walking/presentation/bloc/step_state.dart';
import 'package:guardianes_mobile/features/walking/presentation/widgets/step_counter_widget.dart';

import 'step_counter_widget_test.mocks.dart';

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
          child: const StepCounterWidget(),
        ),
      ),
    );
  }

  group('StepCounterWidget', () {
    testWidgets('should display loading indicator when state is StepLoading',
        (WidgetTester tester) async {
      // Arrange
      when(mockStepBloc.state).thenReturn(StepLoading());

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.byType(CircularProgressIndicator), findsOneWidget);
    });

    testWidgets('should display step count when state is StepLoaded',
        (WidgetTester tester) async {
      // Arrange
      const dailyStepAggregate = DailyStepAggregate(
        guardianId: 1,
        date: '2025-07-16',
        totalSteps: 5000,
      );
      when(mockStepBloc.state).thenReturn(const StepLoaded(currentSteps: dailyStepAggregate));

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.text('5,000 Steps'), findsOneWidget);
      expect(find.text('500 Energy'), findsOneWidget);
    });

    testWidgets('should display energy calculation correctly',
        (WidgetTester tester) async {
      // Arrange
      const dailyStepAggregate = DailyStepAggregate(
        guardianId: 1,
        date: '2025-07-16',
        totalSteps: 2509,
      );
      when(mockStepBloc.state).thenReturn(const StepLoaded(currentSteps: dailyStepAggregate));

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.text('2,509 Steps'), findsOneWidget);
      expect(find.text('250 Energy'), findsOneWidget); // 2509 steps = 250 energy (rounded down)
    });

    testWidgets('should display goal reached indicator when steps >= 8000',
        (WidgetTester tester) async {
      // Arrange
      const dailyStepAggregate = DailyStepAggregate(
        guardianId: 1,
        date: '2025-07-16',
        totalSteps: 8000,
      );
      when(mockStepBloc.state).thenReturn(const StepLoaded(currentSteps: dailyStepAggregate));

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.text('Goal Reached!'), findsOneWidget);
      expect(find.byIcon(Icons.check_circle), findsOneWidget);
    });

    testWidgets('should display progress bar with correct value',
        (WidgetTester tester) async {
      // Arrange
      const dailyStepAggregate = DailyStepAggregate(
        guardianId: 1,
        date: '2025-07-16',
        totalSteps: 4000,
      );
      when(mockStepBloc.state).thenReturn(const StepLoaded(currentSteps: dailyStepAggregate));

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      final progressIndicator = tester.widget<LinearProgressIndicator>(
        find.byType(LinearProgressIndicator),
      );
      expect(progressIndicator.value, equals(4000 / 8000)); // 50% progress
    });

    testWidgets('should display zero steps initially',
        (WidgetTester tester) async {
      // Arrange
      const dailyStepAggregate = DailyStepAggregate(
        guardianId: 1,
        date: '2025-07-16',
        totalSteps: 0,
      );
      when(mockStepBloc.state).thenReturn(const StepLoaded(currentSteps: dailyStepAggregate));

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.text('0 Steps'), findsOneWidget);
      expect(find.text('0 Energy'), findsOneWidget);
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
      expect(find.text('-- Steps'), findsOneWidget);
      expect(find.text('-- Energy'), findsOneWidget);
    });

    testWidgets('should format large numbers correctly',
        (WidgetTester tester) async {
      // Arrange
      const dailyStepAggregate = DailyStepAggregate(
        guardianId: 1,
        date: '2025-07-16',
        totalSteps: 15000,
      );
      when(mockStepBloc.state).thenReturn(const StepLoaded(currentSteps: dailyStepAggregate));

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.text('15,000 Steps'), findsOneWidget);
      expect(find.text('1,500 Energy'), findsOneWidget);
    });

    testWidgets('should handle very large numbers',
        (WidgetTester tester) async {
      // Arrange
      const dailyStepAggregate = DailyStepAggregate(
        guardianId: 1,
        date: '2025-07-16',
        totalSteps: 999999,
      );
      when(mockStepBloc.state).thenReturn(const StepLoaded(currentSteps: dailyStepAggregate));

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert
      expect(find.text('999,999 Steps'), findsOneWidget);
      expect(find.text('99,999 Energy'), findsOneWidget);
    });

    testWidgets('should be responsive to state changes',
        (WidgetTester tester) async {
      // Arrange
      const initialAggregate = DailyStepAggregate(
        guardianId: 1,
        date: '2025-07-16',
        totalSteps: 1000,
      );
      const updatedAggregate = DailyStepAggregate(
        guardianId: 1,
        date: '2025-07-16',
        totalSteps: 2000,
      );
      
      when(mockStepBloc.state).thenReturn(const StepLoaded(currentSteps: initialAggregate));

      // Act
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert initial state
      expect(find.text('1,000 Steps'), findsOneWidget);
      expect(find.text('100 Energy'), findsOneWidget);

      // Change state
      when(mockStepBloc.state).thenReturn(const StepLoaded(currentSteps: updatedAggregate));
      await tester.pumpWidget(createWidgetUnderTest());

      // Assert updated state
      expect(find.text('2,000 Steps'), findsOneWidget);
      expect(find.text('200 Energy'), findsOneWidget);
    });
  });
}
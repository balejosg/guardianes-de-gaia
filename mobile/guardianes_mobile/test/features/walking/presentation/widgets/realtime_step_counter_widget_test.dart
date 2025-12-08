import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:mockito/mockito.dart';
import 'package:mockito/annotations.dart';
import 'package:guardianes_mobile/features/walking/data/services/pedometer_service.dart';
import 'package:guardianes_mobile/features/walking/presentation/bloc/step_bloc.dart';
import 'package:guardianes_mobile/features/walking/presentation/bloc/step_state.dart';
import 'package:guardianes_mobile/features/walking/presentation/widgets/realtime_step_counter_widget.dart';

// Reuse existing StepBloc mock
import 'step_counter_widget_test.mocks.dart';

@GenerateMocks([PedometerService])
import 'realtime_step_counter_widget_test.mocks.dart';

void main() {
  late MockStepBloc mockStepBloc;
  late MockPedometerService mockPedometerService;
  late StreamController<int> stepStreamController;

  setUp(() {
    mockStepBloc = MockStepBloc();
    mockPedometerService = MockPedometerService();
    stepStreamController = StreamController<int>.broadcast();

    when(mockStepBloc.stream).thenAnswer((_) => Stream.value(StepInitial()));
    when(mockStepBloc.state).thenReturn(StepInitial());
  });

  tearDown(() {
    stepStreamController.close();
  });

  Widget createWidgetUnderTest() {
    return MaterialApp(
      home: Scaffold(
        body: BlocProvider<StepBloc>.value(
          value: mockStepBloc,
          child: RealtimeStepCounterWidget(
            guardianId: 1,
            pedometerService: mockPedometerService,
          ),
        ),
      ),
    );
  }

  group('RealtimeStepCounterWidget', () {
    testWidgets('should show permission card when permissions not granted',
        (WidgetTester tester) async {
      // Arrange
      when(mockPedometerService.requestPermissions())
          .thenAnswer((_) async => false);

      // Act
      await tester.pumpWidget(createWidgetUnderTest());
      await tester.pumpAndSettle();

      // Assert
      expect(find.text('Permission Required'), findsOneWidget);
      expect(find.text('Grant Permission'), findsOneWidget);
    });

    testWidgets('should show loading card during initialization',
        (WidgetTester tester) async {
      // Arrange - slow initialization
      final completer = Completer<bool>();
      when(mockPedometerService.requestPermissions())
          .thenAnswer((_) => completer.future);

      // Act
      await tester.pumpWidget(createWidgetUnderTest());
      await tester.pump();

      // Assert
      expect(find.byType(CircularProgressIndicator), findsOneWidget);
      expect(find.text('Initializing Step Counter...'), findsOneWidget);

      // Complete to avoid hanging
      completer.complete(false);
      await tester.pumpAndSettle();
    });

    testWidgets('should show step counter when initialized successfully',
        (WidgetTester tester) async {
      // Arrange
      when(mockPedometerService.requestPermissions())
          .thenAnswer((_) async => true);
      when(mockPedometerService.initialize()).thenAnswer((_) async => true);
      when(mockPedometerService.getCurrentStepCount())
          .thenAnswer((_) async => 1500);
      when(mockPedometerService.stepCountStream)
          .thenAnswer((_) => stepStreamController.stream);

      // Act
      await tester.pumpWidget(createWidgetUnderTest());
      await tester.pumpAndSettle();

      // Assert
      expect(find.text('Daily Steps (Live)'), findsOneWidget);
      expect(find.text('1,500'), findsOneWidget);
      expect(find.text('Steps'), findsOneWidget);
      expect(find.text('150 Energy'), findsOneWidget);
    });

    testWidgets('should update step count when stream emits',
        (WidgetTester tester) async {
      // Arrange
      when(mockPedometerService.requestPermissions())
          .thenAnswer((_) async => true);
      when(mockPedometerService.initialize()).thenAnswer((_) async => true);
      when(mockPedometerService.getCurrentStepCount())
          .thenAnswer((_) async => 1000);
      when(mockPedometerService.stepCountStream)
          .thenAnswer((_) => stepStreamController.stream);

      // Act
      await tester.pumpWidget(createWidgetUnderTest());
      await tester.pumpAndSettle();

      // Verify initial
      expect(find.text('1,000'), findsOneWidget);

      // Emit new steps
      stepStreamController.add(2500);
      await tester.pump();

      // Assert updated
      expect(find.text('2,500'), findsOneWidget);
      expect(find.text('250 Energy'), findsOneWidget);
    });

    testWidgets('should show goal reached badge when steps >= 8000',
        (WidgetTester tester) async {
      // Arrange
      when(mockPedometerService.requestPermissions())
          .thenAnswer((_) async => true);
      when(mockPedometerService.initialize()).thenAnswer((_) async => true);
      when(mockPedometerService.getCurrentStepCount())
          .thenAnswer((_) async => 8500);
      when(mockPedometerService.stepCountStream)
          .thenAnswer((_) => stepStreamController.stream);

      // Act
      await tester.pumpWidget(createWidgetUnderTest());
      await tester.pumpAndSettle();

      // Assert
      expect(find.textContaining('Goal Reached'), findsOneWidget);
    });

    testWidgets('should show sync button enabled when new steps available',
        (WidgetTester tester) async {
      // Arrange
      when(mockPedometerService.requestPermissions())
          .thenAnswer((_) async => true);
      when(mockPedometerService.initialize()).thenAnswer((_) async => true);
      when(mockPedometerService.getCurrentStepCount())
          .thenAnswer((_) async => 100);
      when(mockPedometerService.stepCountStream)
          .thenAnswer((_) => stepStreamController.stream);

      // Act
      await tester.pumpWidget(createWidgetUnderTest());
      await tester.pumpAndSettle();

      // Assert - should show sync button with step count
      expect(find.textContaining('Sync'), findsOneWidget);
      expect(find.byIcon(Icons.sync), findsOneWidget);
    });

    testWidgets('should show error card when initialization fails',
        (WidgetTester tester) async {
      // Arrange
      when(mockPedometerService.requestPermissions())
          .thenAnswer((_) async => true);
      when(mockPedometerService.initialize()).thenAnswer((_) async => false);

      // Act
      await tester.pumpWidget(createWidgetUnderTest());
      await tester.pumpAndSettle();

      // Assert
      expect(find.text('Failed to initialize step counter'), findsOneWidget);
      expect(find.text('Retry'), findsOneWidget);
    });

    testWidgets('should show progress bar with correct percentage',
        (WidgetTester tester) async {
      // Arrange
      when(mockPedometerService.requestPermissions())
          .thenAnswer((_) async => true);
      when(mockPedometerService.initialize()).thenAnswer((_) async => true);
      when(mockPedometerService.getCurrentStepCount())
          .thenAnswer((_) async => 4000); // 50% of 8000 goal
      when(mockPedometerService.stepCountStream)
          .thenAnswer((_) => stepStreamController.stream);

      // Act
      await tester.pumpWidget(createWidgetUnderTest());
      await tester.pumpAndSettle();

      // Assert
      expect(find.text('Daily Goal Progress'), findsOneWidget);
      expect(find.text('50%'), findsOneWidget);
      expect(find.byType(LinearProgressIndicator), findsOneWidget);
    });

    testWidgets('should calculate energy correctly (steps / 10)',
        (WidgetTester tester) async {
      // Arrange
      when(mockPedometerService.requestPermissions())
          .thenAnswer((_) async => true);
      when(mockPedometerService.initialize()).thenAnswer((_) async => true);
      when(mockPedometerService.getCurrentStepCount())
          .thenAnswer((_) async => 2567);
      when(mockPedometerService.stepCountStream)
          .thenAnswer((_) => stepStreamController.stream);

      // Act
      await tester.pumpWidget(createWidgetUnderTest());
      await tester.pumpAndSettle();

      // Assert - 2567 / 10 = 256 energy (floor)
      expect(find.text('256 Energy'), findsOneWidget);
    });

    testWidgets('should retry initialization when retry button pressed',
        (WidgetTester tester) async {
      // Arrange - first fails, second succeeds
      var callCount = 0;
      when(mockPedometerService.requestPermissions())
          .thenAnswer((_) async => true);
      when(mockPedometerService.initialize()).thenAnswer((_) async {
        callCount++;
        return callCount > 1;
      });
      when(mockPedometerService.getCurrentStepCount())
          .thenAnswer((_) async => 500);
      when(mockPedometerService.stepCountStream)
          .thenAnswer((_) => stepStreamController.stream);

      // Act - first render shows error
      await tester.pumpWidget(createWidgetUnderTest());
      await tester.pumpAndSettle();
      expect(find.text('Retry'), findsOneWidget);

      // Tap retry
      await tester.tap(find.text('Retry'));
      await tester.pumpAndSettle();

      // Assert - now shows step counter
      expect(find.text('500'), findsOneWidget);
    });
  });
}

import 'package:flutter_test/flutter_test.dart';
import 'package:bloc_test/bloc_test.dart';
import 'package:mockito/mockito.dart';
import 'package:mockito/annotations.dart';
import 'package:guardianes_mobile/features/walking/domain/entities/step_record.dart';
import 'package:guardianes_mobile/features/walking/domain/entities/daily_step_aggregate.dart';
import 'package:guardianes_mobile/features/walking/domain/usecases/submit_steps.dart';
import 'package:guardianes_mobile/features/walking/domain/usecases/get_current_steps.dart';
import 'package:guardianes_mobile/features/walking/domain/usecases/get_step_history.dart';
import 'package:guardianes_mobile/features/walking/presentation/bloc/step_bloc.dart';
import 'package:guardianes_mobile/features/walking/presentation/bloc/step_event.dart';
import 'package:guardianes_mobile/features/walking/presentation/bloc/step_state.dart';

import 'step_bloc_test.mocks.dart';

@GenerateMocks([
  SubmitSteps,
  GetCurrentSteps,
  GetStepHistory,
])
void main() {
  late StepBloc stepBloc;
  late MockSubmitSteps mockSubmitSteps;
  late MockGetCurrentSteps mockGetCurrentSteps;
  late MockGetStepHistory mockGetStepHistory;

  setUp(() {
    mockSubmitSteps = MockSubmitSteps();
    mockGetCurrentSteps = MockGetCurrentSteps();
    mockGetStepHistory = MockGetStepHistory();
    stepBloc = StepBloc(
      submitSteps: mockSubmitSteps,
      getCurrentSteps: mockGetCurrentSteps,
      getStepHistory: mockGetStepHistory,
    );
  });

  tearDown(() {
    stepBloc.close();
  });

  group('StepBloc', () {
    test('initial state should be StepInitial', () {
      expect(stepBloc.state, equals(StepInitial()));
    });

    group('SubmitStepsEvent', () {
      const tStepRecord = StepRecord(
        guardianId: 1,
        stepCount: 2500,
        timestamp: '2025-07-16T14:30:00',
      );

      blocTest<StepBloc, StepState>(
        'should emit [StepSubmitting, StepSubmitted] when steps are submitted successfully',
        build: () {
          when(mockSubmitSteps.call(any)).thenAnswer((_) async => {});
          return stepBloc;
        },
        act: (bloc) =>
            bloc.add(const SubmitStepsEvent(stepRecord: tStepRecord)),
        expect: () => [
          StepSubmitting(),
          StepSubmitted(),
        ],
        verify: (bloc) {
          verify(mockSubmitSteps.call(tStepRecord));
        },
      );

      blocTest<StepBloc, StepState>(
        'should emit [StepSubmitting, StepSubmissionError] when submission fails',
        build: () {
          when(mockSubmitSteps.call(any)).thenThrow(Exception('Network error'));
          return stepBloc;
        },
        act: (bloc) =>
            bloc.add(const SubmitStepsEvent(stepRecord: tStepRecord)),
        expect: () => [
          StepSubmitting(),
          const StepSubmissionError(
              message: 'Failed to submit steps: Exception: Network error'),
        ],
      );

      blocTest<StepBloc, StepState>(
        'should emit [StepSubmitting, StepSubmissionError] when validation fails',
        build: () {
          when(mockSubmitSteps.call(any))
              .thenThrow(ArgumentError('Step count must be positive'));
          return stepBloc;
        },
        act: (bloc) =>
            bloc.add(const SubmitStepsEvent(stepRecord: tStepRecord)),
        expect: () => [
          StepSubmitting(),
          const StepSubmissionError(
              message: 'Validation error: Step count must be positive'),
        ],
      );
    });

    group('GetCurrentStepsEvent', () {
      const tGuardianId = 1;
      const tDailyStepAggregate = DailyStepAggregate(
        guardianId: tGuardianId,
        date: '2025-07-16',
        totalSteps: 5000,
      );

      blocTest<StepBloc, StepState>(
        'should emit [StepLoading, StepLoaded] when current steps are retrieved successfully',
        build: () {
          when(mockGetCurrentSteps.call(tGuardianId))
              .thenAnswer((_) async => tDailyStepAggregate);
          return stepBloc;
        },
        act: (bloc) =>
            bloc.add(const GetCurrentStepsEvent(guardianId: tGuardianId)),
        expect: () => [
          StepLoading(),
          const StepLoaded(currentSteps: tDailyStepAggregate),
        ],
        verify: (bloc) {
          verify(mockGetCurrentSteps.call(tGuardianId));
        },
      );

      blocTest<StepBloc, StepState>(
        'should emit [StepLoading, StepError] when retrieval fails',
        build: () {
          when(mockGetCurrentSteps.call(tGuardianId))
              .thenThrow(Exception('Network error'));
          return stepBloc;
        },
        act: (bloc) =>
            bloc.add(const GetCurrentStepsEvent(guardianId: tGuardianId)),
        expect: () => [
          StepLoading(),
          const StepError(
              message: 'Failed to get current steps: Exception: Network error'),
        ],
      );

      blocTest<StepBloc, StepState>(
        'should emit [StepLoading, StepError] when validation fails',
        build: () {
          when(mockGetCurrentSteps.call(tGuardianId))
              .thenThrow(ArgumentError('Guardian ID must be positive'));
          return stepBloc;
        },
        act: (bloc) =>
            bloc.add(const GetCurrentStepsEvent(guardianId: tGuardianId)),
        expect: () => [
          StepLoading(),
          const StepError(
              message: 'Validation error: Guardian ID must be positive'),
        ],
      );
    });

    group('GetStepHistoryEvent', () {
      const tGuardianId = 1;
      const tFromDate = '2025-07-14';
      const tToDate = '2025-07-16';
      const tStepHistory = [
        DailyStepAggregate(
          guardianId: tGuardianId,
          date: '2025-07-14',
          totalSteps: 5000,
        ),
        DailyStepAggregate(
          guardianId: tGuardianId,
          date: '2025-07-15',
          totalSteps: 6000,
        ),
        DailyStepAggregate(
          guardianId: tGuardianId,
          date: '2025-07-16',
          totalSteps: 7000,
        ),
      ];

      blocTest<StepBloc, StepState>(
        'should emit [StepLoading, StepHistoryLoaded] when history is retrieved successfully',
        build: () {
          when(mockGetStepHistory.call(tGuardianId, tFromDate, tToDate))
              .thenAnswer((_) async => tStepHistory);
          return stepBloc;
        },
        act: (bloc) => bloc.add(const GetStepHistoryEvent(
          guardianId: tGuardianId,
          fromDate: tFromDate,
          toDate: tToDate,
        )),
        expect: () => [
          StepLoading(),
          const StepHistoryLoaded(stepHistory: tStepHistory),
        ],
        verify: (bloc) {
          verify(mockGetStepHistory.call(tGuardianId, tFromDate, tToDate));
        },
      );

      blocTest<StepBloc, StepState>(
        'should emit [StepLoading, StepHistoryLoaded] with empty list when no history found',
        build: () {
          when(mockGetStepHistory.call(tGuardianId, tFromDate, tToDate))
              .thenAnswer((_) async => []);
          return stepBloc;
        },
        act: (bloc) => bloc.add(const GetStepHistoryEvent(
          guardianId: tGuardianId,
          fromDate: tFromDate,
          toDate: tToDate,
        )),
        expect: () => [
          StepLoading(),
          const StepHistoryLoaded(stepHistory: []),
        ],
      );

      blocTest<StepBloc, StepState>(
        'should emit [StepLoading, StepError] when history retrieval fails',
        build: () {
          when(mockGetStepHistory.call(tGuardianId, tFromDate, tToDate))
              .thenThrow(Exception('Network error'));
          return stepBloc;
        },
        act: (bloc) => bloc.add(const GetStepHistoryEvent(
          guardianId: tGuardianId,
          fromDate: tFromDate,
          toDate: tToDate,
        )),
        expect: () => [
          StepLoading(),
          const StepError(
              message: 'Failed to get step history: Exception: Network error'),
        ],
      );
    });

    group('State Transitions', () {
      blocTest<StepBloc, StepState>(
        'should handle multiple events correctly',
        build: () {
          when(mockGetCurrentSteps.call(1))
              .thenAnswer((_) async => const DailyStepAggregate(
                    guardianId: 1,
                    date: '2025-07-16',
                    totalSteps: 5000,
                  ));
          when(mockSubmitSteps.call(any)).thenAnswer((_) async => {});
          return stepBloc;
        },
        act: (bloc) async {
          bloc.add(const GetCurrentStepsEvent(guardianId: 1));
          await Future.delayed(const Duration(milliseconds: 100));
          bloc.add(const SubmitStepsEvent(
              stepRecord: StepRecord(
            guardianId: 1,
            stepCount: 1000,
            timestamp: '2025-07-16T15:00:00',
          )));
        },
        expect: () => [
          StepLoading(),
          const StepLoaded(
              currentSteps: DailyStepAggregate(
            guardianId: 1,
            date: '2025-07-16',
            totalSteps: 5000,
          )),
          StepSubmitting(),
          StepSubmitted(),
        ],
      );

      blocTest<StepBloc, StepState>(
        'should maintain state isolation between different operations',
        build: () {
          when(mockGetCurrentSteps.call(1))
              .thenAnswer((_) async => const DailyStepAggregate(
                    guardianId: 1,
                    date: '2025-07-16',
                    totalSteps: 5000,
                  ));
          when(mockGetStepHistory.call(1, '2025-07-14', '2025-07-16'))
              .thenAnswer((_) async => []);
          return stepBloc;
        },
        act: (bloc) async {
          bloc.add(const GetCurrentStepsEvent(guardianId: 1));
          await Future.delayed(const Duration(milliseconds: 100));
          bloc.add(const GetStepHistoryEvent(
            guardianId: 1,
            fromDate: '2025-07-14',
            toDate: '2025-07-16',
          ));
        },
        expect: () => [
          StepLoading(),
          const StepLoaded(
              currentSteps: DailyStepAggregate(
            guardianId: 1,
            date: '2025-07-16',
            totalSteps: 5000,
          )),
          StepLoading(),
          const StepHistoryLoaded(stepHistory: []),
        ],
      );
    });
  });
}

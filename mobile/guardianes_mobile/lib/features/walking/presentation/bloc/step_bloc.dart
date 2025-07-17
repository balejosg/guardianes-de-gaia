import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:guardianes_mobile/features/walking/domain/usecases/submit_steps.dart';
import 'package:guardianes_mobile/features/walking/domain/usecases/get_current_steps.dart';
import 'package:guardianes_mobile/features/walking/domain/usecases/get_step_history.dart';
import 'package:guardianes_mobile/features/walking/presentation/bloc/step_event.dart';
import 'package:guardianes_mobile/features/walking/presentation/bloc/step_state.dart';

class StepBloc extends Bloc<StepEvent, StepState> {
  final SubmitSteps submitSteps;
  final GetCurrentSteps getCurrentSteps;
  final GetStepHistory getStepHistory;

  StepBloc({
    required this.submitSteps,
    required this.getCurrentSteps,
    required this.getStepHistory,
  }) : super(StepInitial()) {
    on<SubmitStepsEvent>(_onSubmitSteps);
    on<GetCurrentStepsEvent>(_onGetCurrentSteps);
    on<GetStepHistoryEvent>(_onGetStepHistory);
  }

  Future<void> _onSubmitSteps(
    SubmitStepsEvent event,
    Emitter<StepState> emit,
  ) async {
    emit(StepSubmitting());
    try {
      await submitSteps.call(event.stepRecord);
      emit(StepSubmitted());
    } on ArgumentError catch (e) {
      emit(StepSubmissionError(message: 'Validation error: ${e.message}'));
    } catch (e) {
      emit(StepSubmissionError(message: 'Failed to submit steps: $e'));
    }
  }

  Future<void> _onGetCurrentSteps(
    GetCurrentStepsEvent event,
    Emitter<StepState> emit,
  ) async {
    emit(StepLoading());
    try {
      final currentSteps = await getCurrentSteps.call(event.guardianId);
      emit(StepLoaded(currentSteps: currentSteps));
    } on ArgumentError catch (e) {
      emit(StepError(message: 'Validation error: ${e.message}'));
    } catch (e) {
      emit(StepError(message: 'Failed to get current steps: $e'));
    }
  }

  Future<void> _onGetStepHistory(
    GetStepHistoryEvent event,
    Emitter<StepState> emit,
  ) async {
    emit(StepLoading());
    try {
      final stepHistory = await getStepHistory.call(
        event.guardianId,
        event.fromDate,
        event.toDate,
      );
      emit(StepHistoryLoaded(stepHistory: stepHistory));
    } on ArgumentError catch (e) {
      emit(StepError(message: 'Validation error: ${e.message}'));
    } catch (e) {
      emit(StepError(message: 'Failed to get step history: $e'));
    }
  }
}
import 'package:equatable/equatable.dart';
import '../../domain/entities/current_step_count.dart';
import '../../domain/entities/energy_balance.dart';
import '../../domain/entities/step_history.dart';
import '../../domain/entities/step_submission_result.dart';

abstract class StepTrackingState extends Equatable {
  @override
  List<Object?> get props => [];
}

class StepTrackingInitial extends StepTrackingState {}

class StepTrackingLoading extends StepTrackingState {}

class StepTrackingLoaded extends StepTrackingState {
  final CurrentStepCount? currentStepCount;
  final EnergyBalance? energyBalance;
  final StepHistory? stepHistory;
  final StepSubmissionResult? lastSubmissionResult;

  StepTrackingLoaded({
    this.currentStepCount,
    this.energyBalance,
    this.stepHistory,
    this.lastSubmissionResult,
  });

  StepTrackingLoaded copyWith({
    CurrentStepCount? currentStepCount,
    EnergyBalance? energyBalance,
    StepHistory? stepHistory,
    StepSubmissionResult? lastSubmissionResult,
  }) {
    return StepTrackingLoaded(
      currentStepCount: currentStepCount ?? this.currentStepCount,
      energyBalance: energyBalance ?? this.energyBalance,
      stepHistory: stepHistory ?? this.stepHistory,
      lastSubmissionResult: lastSubmissionResult ?? this.lastSubmissionResult,
    );
  }

  @override
  List<Object?> get props => [
        currentStepCount,
        energyBalance,
        stepHistory,
        lastSubmissionResult,
      ];
}

class StepTrackingError extends StepTrackingState {
  final String message;

  StepTrackingError(this.message);

  @override
  List<Object?> get props => [message];
}

class StepSubmissionInProgress extends StepTrackingState {}

class StepSubmissionSuccess extends StepTrackingState {
  final StepSubmissionResult result;

  StepSubmissionSuccess(this.result);

  @override
  List<Object?> get props => [result];
}
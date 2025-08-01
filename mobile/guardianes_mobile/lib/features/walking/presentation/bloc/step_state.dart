import 'package:equatable/equatable.dart';
import 'package:guardianes_mobile/features/walking/domain/entities/daily_step_aggregate.dart';

abstract class StepState extends Equatable {
  const StepState();

  @override
  List<Object> get props => [];
}

class StepInitial extends StepState {}

class StepLoading extends StepState {}

class StepSubmitting extends StepState {}

class StepSubmitted extends StepState {}

class StepSubmissionError extends StepState {
  final String message;

  const StepSubmissionError({required this.message});

  @override
  List<Object> get props => [message];
}

class StepLoaded extends StepState {
  final DailyStepAggregate currentSteps;

  const StepLoaded({required this.currentSteps});

  @override
  List<Object> get props => [currentSteps];
}

class StepHistoryLoaded extends StepState {
  final List<DailyStepAggregate> stepHistory;

  const StepHistoryLoaded({required this.stepHistory});

  @override
  List<Object> get props => [stepHistory];
}

class StepError extends StepState {
  final String message;

  const StepError({required this.message});

  @override
  List<Object> get props => [message];
}

import 'package:equatable/equatable.dart';
import 'package:guardianes_mobile/features/walking/domain/entities/step_record.dart';

abstract class StepEvent extends Equatable {
  const StepEvent();

  @override
  List<Object> get props => [];
}

class SubmitStepsEvent extends StepEvent {
  final StepRecord stepRecord;

  const SubmitStepsEvent({required this.stepRecord});

  @override
  List<Object> get props => [stepRecord];
}

class GetCurrentStepsEvent extends StepEvent {
  final int guardianId;

  const GetCurrentStepsEvent({required this.guardianId});

  @override
  List<Object> get props => [guardianId];
}

class GetStepHistoryEvent extends StepEvent {
  final int guardianId;
  final String fromDate;
  final String toDate;

  const GetStepHistoryEvent({
    required this.guardianId,
    required this.fromDate,
    required this.toDate,
  });

  @override
  List<Object> get props => [guardianId, fromDate, toDate];
}

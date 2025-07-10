import 'package:equatable/equatable.dart';

abstract class StepTrackingEvent extends Equatable {
  @override
  List<Object?> get props => [];
}

class LoadCurrentStepCount extends StepTrackingEvent {
  final String guardianId;

  LoadCurrentStepCount(this.guardianId);

  @override
  List<Object?> get props => [guardianId];
}

class LoadEnergyBalance extends StepTrackingEvent {
  final String guardianId;

  LoadEnergyBalance(this.guardianId);

  @override
  List<Object?> get props => [guardianId];
}

class LoadStepHistory extends StepTrackingEvent {
  final String guardianId;
  final int? days;

  LoadStepHistory(this.guardianId, {this.days});

  @override
  List<Object?> get props => [guardianId, days];
}

class SubmitSteps extends StepTrackingEvent {
  final String guardianId;
  final int stepCount;

  SubmitSteps(this.guardianId, this.stepCount);

  @override
  List<Object?> get props => [guardianId, stepCount];
}

class RefreshData extends StepTrackingEvent {
  final String guardianId;

  RefreshData(this.guardianId);

  @override
  List<Object?> get props => [guardianId];
}
import 'package:equatable/equatable.dart';

class StepRecord extends Equatable {
  final int guardianId;
  final int stepCount;
  final String timestamp;

  const StepRecord({
    required this.guardianId,
    required this.stepCount,
    required this.timestamp,
  });

  @override
  List<Object?> get props => [guardianId, stepCount, timestamp];

  /// Calculate energy generated from steps (1 energy per 10 steps)
  int calculateEnergy() {
    return stepCount ~/ 10;
  }
}

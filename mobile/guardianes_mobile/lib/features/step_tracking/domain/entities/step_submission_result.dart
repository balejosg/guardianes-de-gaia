import 'package:equatable/equatable.dart';

class StepSubmissionResult extends Equatable {
  final bool success;
  final String message;
  final int energyEarned;
  final int totalSteps;
  final int totalEnergy;

  const StepSubmissionResult({
    required this.success,
    required this.message,
    required this.energyEarned,
    required this.totalSteps,
    required this.totalEnergy,
  });

  @override
  List<Object?> get props => [
        success,
        message,
        energyEarned,
        totalSteps,
        totalEnergy,
      ];
}
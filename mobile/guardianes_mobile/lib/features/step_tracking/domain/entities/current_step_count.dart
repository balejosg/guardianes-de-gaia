import 'package:equatable/equatable.dart';

class CurrentStepCount extends Equatable {
  final int currentSteps;
  final int totalEnergy;
  final DateTime lastUpdated;

  const CurrentStepCount({
    required this.currentSteps,
    required this.totalEnergy,
    required this.lastUpdated,
  });

  @override
  List<Object?> get props => [
        currentSteps,
        totalEnergy,
        lastUpdated,
      ];
}
import 'package:equatable/equatable.dart';

class StepHistoryEntry extends Equatable {
  final DateTime date;
  final int stepCount;
  final int energyEarned;

  const StepHistoryEntry({
    required this.date,
    required this.stepCount,
    required this.energyEarned,
  });

  @override
  List<Object?> get props => [
        date,
        stepCount,
        energyEarned,
      ];
}

class StepHistory extends Equatable {
  final List<StepHistoryEntry> entries;
  final int totalSteps;
  final int totalEnergy;

  const StepHistory({
    required this.entries,
    required this.totalSteps,
    required this.totalEnergy,
  });

  @override
  List<Object?> get props => [
        entries,
        totalSteps,
        totalEnergy,
      ];
}
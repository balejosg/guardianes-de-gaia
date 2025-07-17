import 'package:equatable/equatable.dart';

class DailyStepAggregate extends Equatable {
  final int guardianId;
  final String date;
  final int totalSteps;

  const DailyStepAggregate({
    required this.guardianId,
    required this.date,
    required this.totalSteps,
  });

  @override
  List<Object?> get props => [guardianId, date, totalSteps];

  /// Calculate total energy from daily steps (1 energy per 10 steps)
  int calculateTotalEnergy() {
    return totalSteps ~/ 10;
  }

  /// Check if daily goal is reached (8000 steps)
  bool isGoalReached() {
    return totalSteps >= 8000;
  }
}
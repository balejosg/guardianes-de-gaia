import 'package:guardianes_mobile/features/walking/domain/entities/daily_step_aggregate.dart';
import 'package:guardianes_mobile/features/walking/domain/repositories/step_repository.dart';

class GetCurrentSteps {
  final StepRepository repository;

  GetCurrentSteps(this.repository);

  Future<DailyStepAggregate> call(int guardianId) async {
    // Validate input
    if (guardianId <= 0) {
      throw ArgumentError('Guardian ID must be positive');
    }

    // Get current steps from repository
    return await repository.getCurrentStepCount(guardianId);
  }
}
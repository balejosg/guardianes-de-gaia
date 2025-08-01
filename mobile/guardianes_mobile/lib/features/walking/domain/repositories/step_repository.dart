import 'package:guardianes_mobile/features/walking/domain/entities/step_record.dart';
import 'package:guardianes_mobile/features/walking/domain/entities/daily_step_aggregate.dart';

abstract class StepRepository {
  /// Submit steps for a guardian
  Future<void> submitSteps(StepRecord stepRecord);

  /// Get current daily step count for a guardian
  Future<DailyStepAggregate> getCurrentStepCount(int guardianId);

  /// Get step history for a guardian within a date range
  Future<List<DailyStepAggregate>> getStepHistory(
    int guardianId,
    String fromDate,
    String toDate,
  );
}

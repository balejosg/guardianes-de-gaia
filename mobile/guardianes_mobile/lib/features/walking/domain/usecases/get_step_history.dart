import 'package:guardianes_mobile/features/walking/domain/entities/daily_step_aggregate.dart';
import 'package:guardianes_mobile/features/walking/domain/repositories/step_repository.dart';

class GetStepHistory {
  final StepRepository repository;

  GetStepHistory(this.repository);

  Future<List<DailyStepAggregate>> call(
      int guardianId, String fromDate, String toDate) async {
    // Validate input
    if (guardianId <= 0) {
      throw ArgumentError('Guardian ID must be positive');
    }

    if (fromDate.isEmpty) {
      throw ArgumentError('From date cannot be empty');
    }

    if (toDate.isEmpty) {
      throw ArgumentError('To date cannot be empty');
    }

    // Validate date format (YYYY-MM-DD)
    final dateRegex = RegExp(r'^\d{4}-\d{2}-\d{2}$');
    if (!dateRegex.hasMatch(fromDate)) {
      throw ArgumentError('From date must be in format YYYY-MM-DD');
    }

    if (!dateRegex.hasMatch(toDate)) {
      throw ArgumentError('To date must be in format YYYY-MM-DD');
    }

    // Validate date range
    if (fromDate.compareTo(toDate) > 0) {
      throw ArgumentError('From date must be before or equal to to date');
    }

    // Get step history from repository
    return await repository.getStepHistory(guardianId, fromDate, toDate);
  }
}

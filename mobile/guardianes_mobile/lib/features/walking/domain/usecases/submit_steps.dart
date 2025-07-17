import 'package:guardianes_mobile/features/walking/domain/entities/step_record.dart';
import 'package:guardianes_mobile/features/walking/domain/repositories/step_repository.dart';

class SubmitSteps {
  final StepRepository repository;

  SubmitSteps(this.repository);

  Future<void> call(StepRecord stepRecord) async {
    // Validate input
    if (stepRecord.guardianId <= 0) {
      throw ArgumentError('Guardian ID must be positive');
    }
    
    if (stepRecord.stepCount <= 0) {
      throw ArgumentError('Step count must be positive');
    }
    
    if (stepRecord.timestamp.isEmpty) {
      throw ArgumentError('Timestamp cannot be empty');
    }

    // Submit steps through repository
    await repository.submitSteps(stepRecord);
  }
}
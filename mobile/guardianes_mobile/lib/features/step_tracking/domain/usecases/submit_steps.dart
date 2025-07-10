import '../entities/step_submission_result.dart';
import '../repositories/step_tracking_repository.dart';

class SubmitStepsUseCase {
  final StepTrackingRepository _repository;

  SubmitStepsUseCase(this._repository);

  Future<StepSubmissionResult> call(String guardianId, int stepCount) async {
    if (stepCount <= 0) {
      throw ArgumentError('Step count must be positive');
    }

    return await _repository.submitSteps(guardianId, stepCount, DateTime.now());
  }
}
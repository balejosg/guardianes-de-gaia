import '../entities/current_step_count.dart';
import '../repositories/step_tracking_repository.dart';

class GetCurrentStepCountUseCase {
  final StepTrackingRepository _repository;

  GetCurrentStepCountUseCase(this._repository);

  Future<CurrentStepCount> call(String guardianId) async {
    return await _repository.getCurrentStepCount(guardianId);
  }
}
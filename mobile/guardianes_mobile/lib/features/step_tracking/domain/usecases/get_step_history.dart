import '../entities/step_history.dart';
import '../repositories/step_tracking_repository.dart';

class GetStepHistoryUseCase {
  final StepTrackingRepository _repository;

  GetStepHistoryUseCase(this._repository);

  Future<StepHistory> call(String guardianId, {int? days}) async {
    return await _repository.getStepHistory(guardianId, days: days);
  }
}
import '../../domain/repositories/step_tracking_repository.dart';
import '../../domain/entities/step_submission_result.dart';
import '../../domain/entities/current_step_count.dart';
import '../../domain/entities/step_history.dart';
import '../../domain/entities/energy_balance.dart';
import '../datasources/step_tracking_remote_data_source.dart';

class StepTrackingRepositoryImpl implements StepTrackingRepository {
  final StepTrackingRemoteDataSource _remoteDataSource;

  StepTrackingRepositoryImpl(this._remoteDataSource);

  @override
  Future<StepSubmissionResult> submitSteps(String guardianId, int stepCount, DateTime timestamp) {
    return _remoteDataSource.submitSteps(guardianId, stepCount, timestamp);
  }

  @override
  Future<CurrentStepCount> getCurrentStepCount(String guardianId) {
    return _remoteDataSource.getCurrentStepCount(guardianId);
  }

  @override
  Future<StepHistory> getStepHistory(String guardianId, {int? days}) {
    return _remoteDataSource.getStepHistory(guardianId, days: days);
  }

  @override
  Future<EnergyBalance> getEnergyBalance(String guardianId) {
    return _remoteDataSource.getEnergyBalance(guardianId);
  }
}
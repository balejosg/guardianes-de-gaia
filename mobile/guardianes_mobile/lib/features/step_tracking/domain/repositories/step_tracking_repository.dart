import '../entities/step_submission_result.dart';
import '../entities/current_step_count.dart';
import '../entities/step_history.dart';
import '../entities/energy_balance.dart';

abstract class StepTrackingRepository {
  Future<StepSubmissionResult> submitSteps(String guardianId, int stepCount, DateTime timestamp);
  Future<CurrentStepCount> getCurrentStepCount(String guardianId);
  Future<StepHistory> getStepHistory(String guardianId, {int? days});
  Future<EnergyBalance> getEnergyBalance(String guardianId);
}
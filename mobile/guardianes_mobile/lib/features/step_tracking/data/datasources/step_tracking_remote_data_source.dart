import '../../domain/entities/step_submission_result.dart';
import '../../domain/entities/current_step_count.dart';
import '../../domain/entities/step_history.dart' as domain;
import '../../domain/entities/energy_balance.dart';
import '../models/step_submission_request.dart';
import '../models/step_submission_response.dart';
import '../models/current_step_count_response.dart';
import '../models/step_history_response.dart';
import '../models/energy_balance_response.dart';
import '../../../../core/api/api_client.dart';
import '../../../../core/constants/api_constants.dart';

abstract class StepTrackingRemoteDataSource {
  Future<StepSubmissionResult> submitSteps(String guardianId, int stepCount, DateTime timestamp);
  Future<CurrentStepCount> getCurrentStepCount(String guardianId);
  Future<domain.StepHistory> getStepHistory(String guardianId, {int? days});
  Future<EnergyBalance> getEnergyBalance(String guardianId);
}

class StepTrackingRemoteDataSourceImpl implements StepTrackingRemoteDataSource {
  final ApiClient _apiClient;

  StepTrackingRemoteDataSourceImpl(this._apiClient);

  @override
  Future<StepSubmissionResult> submitSteps(
      String guardianId, int stepCount, DateTime timestamp) async {
    final request = StepSubmissionRequest(
      guardianId: guardianId,
      stepCount: stepCount,
      timestamp: timestamp,
      source: 'mobile_app',
    );

    final response = await _apiClient.post(
      ApiConstants.getStepsPath(guardianId),
      data: request.toJson(),
    );

    final stepResponse = StepSubmissionResponse.fromJson(response.data);
    
    return StepSubmissionResult(
      success: stepResponse.success,
      message: stepResponse.message,
      energyEarned: stepResponse.energyEarned,
      totalSteps: stepResponse.totalSteps,
      totalEnergy: stepResponse.energyEarned, // Use energyEarned as totalEnergy
    );
  }

  @override
  Future<CurrentStepCount> getCurrentStepCount(String guardianId) async {
    final response = await _apiClient.get(
      ApiConstants.getCurrentStepsPath(guardianId),
    );

    final stepCountResponse = CurrentStepCountResponse.fromJson(response.data);
    
    return CurrentStepCount(
      currentSteps: stepCountResponse.currentSteps,
      totalEnergy: stepCountResponse.totalEnergy,
      lastUpdated: DateTime.parse(stepCountResponse.lastUpdated),
    );
  }

  @override
  Future<domain.StepHistory> getStepHistory(String guardianId, {int? days}) async {
    // Convert days to from/to date parameters as expected by backend
    final now = DateTime.now();
    final fromDate = now.subtract(Duration(days: days ?? 7));
    final toDate = now;
    
    final queryParams = {
      'from': fromDate.toIso8601String().split('T')[0], // YYYY-MM-DD format
      'to': toDate.toIso8601String().split('T')[0], // YYYY-MM-DD format
    };
    
    final response = await _apiClient.get(
      ApiConstants.getStepHistoryPath(guardianId),
      queryParameters: queryParams,
    );

    final historyResponse = StepHistoryResponse.fromJson(response.data);
    
    return domain.StepHistory(
      entries: historyResponse.history.map((entry) => domain.StepHistoryEntry(
        date: entry.date,
        stepCount: entry.stepCount,
        energyEarned: entry.energyEarned,
      )).toList(),
      totalSteps: historyResponse.totalSteps,
      totalEnergy: historyResponse.totalEnergy,
    );
  }

  @override
  Future<EnergyBalance> getEnergyBalance(String guardianId) async {
    final response = await _apiClient.get(
      ApiConstants.getEnergyBalancePath(guardianId),
    );

    final balanceResponse = EnergyBalanceResponse.fromJson(response.data);
    
    return EnergyBalance(
      currentBalance: balanceResponse.currentBalance,
      totalEarned: balanceResponse.totalEarned,
      totalSpent: balanceResponse.totalSpent,
      lastUpdated: balanceResponse.lastUpdated,
    );
  }
}
import 'package:guardianes_mobile/features/walking/data/datasources/step_remote_datasource.dart';
import 'package:guardianes_mobile/features/walking/data/models/step_submission_request_dto.dart';
import 'package:guardianes_mobile/features/walking/domain/entities/step_record.dart';
import 'package:guardianes_mobile/features/walking/domain/entities/daily_step_aggregate.dart';
import 'package:guardianes_mobile/features/walking/domain/repositories/step_repository.dart';

class StepRepositoryImpl implements StepRepository {
  final StepRemoteDataSource remoteDataSource;

  StepRepositoryImpl(this.remoteDataSource);

  @override
  Future<void> submitSteps(StepRecord stepRecord) async {
    final request = StepSubmissionRequestDto(
      stepCount: stepRecord.stepCount,
      timestamp: stepRecord.timestamp,
    );

    await remoteDataSource.submitSteps(stepRecord.guardianId, request);
  }

  @override
  Future<DailyStepAggregate> getCurrentStepCount(int guardianId) async {
    final response = await remoteDataSource.getCurrentStepCount(guardianId);
    return response.toDomainEntity();
  }

  @override
  Future<List<DailyStepAggregate>> getStepHistory(
    int guardianId,
    String fromDate,
    String toDate,
  ) async {
    final response =
        await remoteDataSource.getStepHistory(guardianId, fromDate, toDate);
    return response.toDomainEntities();
  }
}

import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:mockito/annotations.dart';
import 'package:guardianes_mobile/features/walking/data/datasources/step_remote_datasource.dart';
import 'package:guardianes_mobile/features/walking/data/repositories/step_repository_impl.dart';
import 'package:guardianes_mobile/features/walking/data/models/step_submission_request_dto.dart';
import 'package:guardianes_mobile/features/walking/data/models/step_submission_response_dto.dart';
import 'package:guardianes_mobile/features/walking/data/models/current_step_count_response_dto.dart';
import 'package:guardianes_mobile/features/walking/data/models/step_history_response_dto.dart';
import 'package:guardianes_mobile/features/walking/data/models/daily_step_aggregate_dto.dart';
import 'package:guardianes_mobile/features/walking/domain/entities/step_record.dart';
import 'package:guardianes_mobile/features/walking/domain/entities/daily_step_aggregate.dart';

import 'step_repository_impl_test.mocks.dart';

@GenerateMocks([StepRemoteDataSource])
void main() {
  late StepRepositoryImpl repository;
  late MockStepRemoteDataSource mockRemoteDataSource;

  setUp(() {
    mockRemoteDataSource = MockStepRemoteDataSource();
    repository = StepRepositoryImpl(mockRemoteDataSource);
  });

  group('StepRepositoryImpl', () {
    group('submitSteps', () {
      test('should submit steps through remote data source', () async {
        // Arrange
        const stepRecord = StepRecord(
          guardianId: 1,
          stepCount: 2500,
          timestamp: '2025-07-16T14:30:00',
        );

        const responseDto = StepSubmissionResponseDto(
          guardianId: 1,
          totalDailySteps: 2500,
          energyEarned: 250,
          message: 'Steps submitted successfully',
        );

        when(mockRemoteDataSource.submitSteps(any, any))
            .thenAnswer((_) async => responseDto);

        // Act
        await repository.submitSteps(stepRecord);

        // Assert
        verify(mockRemoteDataSource.submitSteps(
          1,
          const StepSubmissionRequestDto(
            stepCount: 2500,
            timestamp: '2025-07-16T14:30:00',
          ),
        ));
      });

      test('should handle remote data source errors', () async {
        // Arrange
        const stepRecord = StepRecord(
          guardianId: 1,
          stepCount: 2500,
          timestamp: '2025-07-16T14:30:00',
        );

        when(mockRemoteDataSource.submitSteps(any, any))
            .thenThrow(Exception('Network error'));

        // Act & Assert
        expect(
          () => repository.submitSteps(stepRecord),
          throwsA(isA<Exception>()),
        );
      });
    });

    group('getCurrentStepCount', () {
      test('should get current step count from remote data source', () async {
        // Arrange
        const guardianId = 1;
        const responseDto = CurrentStepCountResponseDto(
          guardianId: guardianId,
          currentSteps: 5000,
          availableEnergy: 500,
          date: '2025-07-16',
        );

        when(mockRemoteDataSource.getCurrentStepCount(guardianId))
            .thenAnswer((_) async => responseDto);

        // Act
        final result = await repository.getCurrentStepCount(guardianId);

        // Assert
        expect(result, isA<DailyStepAggregate>());
        expect(result.guardianId, equals(guardianId));
        expect(result.totalSteps, equals(5000));
        expect(result.date, equals('2025-07-16'));
        verify(mockRemoteDataSource.getCurrentStepCount(guardianId));
      });

      test('should handle remote data source errors', () async {
        // Arrange
        const guardianId = 1;

        when(mockRemoteDataSource.getCurrentStepCount(guardianId))
            .thenThrow(Exception('Network error'));

        // Act & Assert
        expect(
          () => repository.getCurrentStepCount(guardianId),
          throwsA(isA<Exception>()),
        );
      });
    });

    group('getStepHistory', () {
      test('should get step history from remote data source', () async {
        // Arrange
        const guardianId = 1;
        const fromDate = '2025-07-14';
        const toDate = '2025-07-16';
        const dailyStepDtos = [
          DailyStepAggregateDto(
            guardianId: guardianId,
            date: '2025-07-14',
            totalSteps: 5000,
          ),
          DailyStepAggregateDto(
            guardianId: guardianId,
            date: '2025-07-15',
            totalSteps: 6000,
          ),
          DailyStepAggregateDto(
            guardianId: guardianId,
            date: '2025-07-16',
            totalSteps: 7000,
          ),
        ];
        const responseDto = StepHistoryResponseDto(
          guardianId: guardianId,
          dailySteps: dailyStepDtos,
        );

        when(mockRemoteDataSource.getStepHistory(guardianId, fromDate, toDate))
            .thenAnswer((_) async => responseDto);

        // Act
        final result =
            await repository.getStepHistory(guardianId, fromDate, toDate);

        // Assert
        expect(result, isA<List<DailyStepAggregate>>());
        expect(result.length, equals(3));
        expect(result[0].guardianId, equals(guardianId));
        expect(result[0].date, equals('2025-07-14'));
        expect(result[0].totalSteps, equals(5000));
        expect(result[1].totalSteps, equals(6000));
        expect(result[2].totalSteps, equals(7000));
        verify(
            mockRemoteDataSource.getStepHistory(guardianId, fromDate, toDate));
      });

      test('should return empty list when no data found', () async {
        // Arrange
        const guardianId = 1;
        const fromDate = '2025-07-14';
        const toDate = '2025-07-16';
        const responseDto = StepHistoryResponseDto(
          guardianId: guardianId,
          dailySteps: [],
        );

        when(mockRemoteDataSource.getStepHistory(guardianId, fromDate, toDate))
            .thenAnswer((_) async => responseDto);

        // Act
        final result =
            await repository.getStepHistory(guardianId, fromDate, toDate);

        // Assert
        expect(result, isA<List<DailyStepAggregate>>());
        expect(result, isEmpty);
        verify(
            mockRemoteDataSource.getStepHistory(guardianId, fromDate, toDate));
      });

      test('should handle remote data source errors', () async {
        // Arrange
        const guardianId = 1;
        const fromDate = '2025-07-14';
        const toDate = '2025-07-16';

        when(mockRemoteDataSource.getStepHistory(guardianId, fromDate, toDate))
            .thenThrow(Exception('Network error'));

        // Act & Assert
        expect(
          () => repository.getStepHistory(guardianId, fromDate, toDate),
          throwsA(isA<Exception>()),
        );
      });
    });

    group('Data Mapping', () {
      test('should correctly map StepRecord to StepSubmissionRequestDto',
          () async {
        // Arrange
        const stepRecord = StepRecord(
          guardianId: 1,
          stepCount: 2500,
          timestamp: '2025-07-16T14:30:00',
        );

        const responseDto = StepSubmissionResponseDto(
          guardianId: 1,
          totalDailySteps: 2500,
          energyEarned: 250,
          message: 'Steps submitted successfully',
        );

        when(mockRemoteDataSource.submitSteps(any, any))
            .thenAnswer((_) async => responseDto);

        // Act
        await repository.submitSteps(stepRecord);

        // Assert
        final capturedRequest = verify(mockRemoteDataSource.submitSteps(
          1,
          captureAny,
        )).captured.last as StepSubmissionRequestDto;

        expect(capturedRequest.stepCount, equals(2500));
        expect(capturedRequest.timestamp, equals('2025-07-16T14:30:00'));
      });

      test(
          'should correctly map CurrentStepCountResponseDto to DailyStepAggregate',
          () async {
        // Arrange
        const guardianId = 1;
        const responseDto = CurrentStepCountResponseDto(
          guardianId: guardianId,
          currentSteps: 5000,
          availableEnergy: 500,
          date: '2025-07-16',
        );

        when(mockRemoteDataSource.getCurrentStepCount(guardianId))
            .thenAnswer((_) async => responseDto);

        // Act
        final result = await repository.getCurrentStepCount(guardianId);

        // Assert
        expect(result.guardianId, equals(guardianId));
        expect(result.totalSteps, equals(5000));
        expect(result.date, equals('2025-07-16'));
        expect(result.calculateTotalEnergy(), equals(500));
      });
    });
  });
}

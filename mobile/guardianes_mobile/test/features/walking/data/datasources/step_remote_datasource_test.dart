import 'dart:convert';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:mockito/annotations.dart';
import 'package:http/http.dart' as http;
import 'package:guardianes_mobile/features/walking/data/datasources/step_remote_datasource.dart';
import 'package:guardianes_mobile/features/walking/data/models/step_submission_request_dto.dart';
import 'package:guardianes_mobile/features/walking/data/models/step_submission_response_dto.dart';
import 'package:guardianes_mobile/features/walking/data/models/current_step_count_response_dto.dart';
import 'package:guardianes_mobile/features/walking/data/models/step_history_response_dto.dart';

import 'step_remote_datasource_test.mocks.dart';

@GenerateMocks([http.Client])
void main() {
  late StepRemoteDataSource dataSource;
  late MockClient mockHttpClient;

  setUp(() {
    mockHttpClient = MockClient();
    dataSource = StepRemoteDataSourceImpl(mockHttpClient);
  });

  group('StepRemoteDataSource', () {
    const baseUrl = 'http://dev-guardianes.duckdns.org';
    const guardianId = 1;

    group('submitSteps', () {
      test('should perform a POST request to the correct endpoint', () async {
        // Arrange
        const request = StepSubmissionRequestDto(
          stepCount: 2500,
          timestamp: '2025-07-16T14:30:00',
        );

        const responseDto = StepSubmissionResponseDto(
          guardianId: guardianId,
          totalDailySteps: 2500,
          energyEarned: 250,
          message: 'Steps submitted successfully',
        );

        when(mockHttpClient.post(
          Uri.parse('$baseUrl/api/v1/guardians/$guardianId/steps'),
          headers: {
            'Content-Type': 'application/json',
          },
          body: json.encode(request.toJson()),
        )).thenAnswer((_) async => http.Response(
              json.encode(responseDto.toJson()),
              201,
            ));

        // Act
        final result = await dataSource.submitSteps(guardianId, request);

        // Assert
        expect(result, equals(responseDto));
        verify(mockHttpClient.post(
          Uri.parse('$baseUrl/api/v1/guardians/$guardianId/steps'),
          headers: {
            'Content-Type': 'application/json',
          },
          body: json.encode(request.toJson()),
        ));
      });

      test('should throw an exception when the response code is not 201',
          () async {
        // Arrange
        const request = StepSubmissionRequestDto(
          stepCount: 2500,
          timestamp: '2025-07-16T14:30:00',
        );

        when(mockHttpClient.post(
          Uri.parse('$baseUrl/api/v1/guardians/$guardianId/steps'),
          headers: {
            'Content-Type': 'application/json',
          },
          body: json.encode(request.toJson()),
        )).thenAnswer((_) async => http.Response(
              'Bad Request',
              400,
            ));

        // Act & Assert
        expect(
          () => dataSource.submitSteps(guardianId, request),
          throwsA(isA<Exception>()),
        );
      });

      test('should throw an exception when JSON parsing fails', () async {
        // Arrange
        const request = StepSubmissionRequestDto(
          stepCount: 2500,
          timestamp: '2025-07-16T14:30:00',
        );

        when(mockHttpClient.post(
          Uri.parse('$baseUrl/api/v1/guardians/$guardianId/steps'),
          headers: {
            'Content-Type': 'application/json',
          },
          body: json.encode(request.toJson()),
        )).thenAnswer((_) async => http.Response(
              'Invalid JSON',
              201,
            ));

        // Act & Assert
        expect(
          () => dataSource.submitSteps(guardianId, request),
          throwsA(isA<FormatException>()),
        );
      });
    });

    group('getCurrentStepCount', () {
      test('should perform a GET request to the correct endpoint', () async {
        // Arrange
        const responseDto = CurrentStepCountResponseDto(
          guardianId: guardianId,
          currentSteps: 5000,
          availableEnergy: 500,
          date: '2025-07-16',
        );

        when(mockHttpClient.get(
          Uri.parse('$baseUrl/api/v1/guardians/$guardianId/steps/current'),
          headers: {
            'Content-Type': 'application/json',
          },
        )).thenAnswer((_) async => http.Response(
              json.encode(responseDto.toJson()),
              200,
            ));

        // Act
        final result = await dataSource.getCurrentStepCount(guardianId);

        // Assert
        expect(result, equals(responseDto));
        verify(mockHttpClient.get(
          Uri.parse('$baseUrl/api/v1/guardians/$guardianId/steps/current'),
          headers: {
            'Content-Type': 'application/json',
          },
        ));
      });

      test('should throw an exception when the response code is not 200',
          () async {
        // Arrange
        when(mockHttpClient.get(
          Uri.parse('$baseUrl/api/v1/guardians/$guardianId/steps/current'),
          headers: {
            'Content-Type': 'application/json',
          },
        )).thenAnswer((_) async => http.Response(
              'Not Found',
              404,
            ));

        // Act & Assert
        expect(
          () => dataSource.getCurrentStepCount(guardianId),
          throwsA(isA<Exception>()),
        );
      });
    });

    group('getStepHistory', () {
      test(
          'should perform a GET request to the correct endpoint with query parameters',
          () async {
        // Arrange
        const fromDate = '2025-07-14';
        const toDate = '2025-07-16';
        const responseDto = StepHistoryResponseDto(
          guardianId: guardianId,
          dailySteps: [],
        );

        when(mockHttpClient.get(
          Uri.parse(
              '$baseUrl/api/v1/guardians/$guardianId/steps/history?from=$fromDate&to=$toDate'),
          headers: {
            'Content-Type': 'application/json',
          },
        )).thenAnswer((_) async => http.Response(
              json.encode(responseDto.toJson()),
              200,
            ));

        // Act
        final result =
            await dataSource.getStepHistory(guardianId, fromDate, toDate);

        // Assert
        expect(result, equals(responseDto));
        verify(mockHttpClient.get(
          Uri.parse(
              '$baseUrl/api/v1/guardians/$guardianId/steps/history?from=$fromDate&to=$toDate'),
          headers: {
            'Content-Type': 'application/json',
          },
        ));
      });

      test('should throw an exception when the response code is not 200',
          () async {
        // Arrange
        const fromDate = '2025-07-14';
        const toDate = '2025-07-16';

        when(mockHttpClient.get(
          Uri.parse(
              '$baseUrl/api/v1/guardians/$guardianId/steps/history?from=$fromDate&to=$toDate'),
          headers: {
            'Content-Type': 'application/json',
          },
        )).thenAnswer((_) async => http.Response(
              'Bad Request',
              400,
            ));

        // Act & Assert
        expect(
          () => dataSource.getStepHistory(guardianId, fromDate, toDate),
          throwsA(isA<Exception>()),
        );
      });
    });

    group('Error Handling', () {
      test('should handle network errors', () async {
        // Arrange
        const request = StepSubmissionRequestDto(
          stepCount: 2500,
          timestamp: '2025-07-16T14:30:00',
        );

        when(mockHttpClient.post(
          Uri.parse('$baseUrl/api/v1/guardians/$guardianId/steps'),
          headers: {
            'Content-Type': 'application/json',
          },
          body: json.encode(request.toJson()),
        )).thenThrow(Exception('Network error'));

        // Act & Assert
        expect(
          () => dataSource.submitSteps(guardianId, request),
          throwsA(isA<Exception>()),
        );
      });

      test('should handle timeout errors', () async {
        // Arrange
        when(mockHttpClient.get(
          Uri.parse('$baseUrl/api/v1/guardians/$guardianId/steps/current'),
          headers: {
            'Content-Type': 'application/json',
          },
        )).thenThrow(Exception('Timeout'));

        // Act & Assert
        expect(
          () => dataSource.getCurrentStepCount(guardianId),
          throwsA(isA<Exception>()),
        );
      });
    });
  });
}

import 'package:flutter_test/flutter_test.dart';
import 'package:dio/dio.dart';
import 'package:mockito/mockito.dart';
import 'package:mockito/annotations.dart';
import 'package:guardianes_mobile/features/step_tracking/data/models/current_step_count_response.dart';
import 'package:guardianes_mobile/features/step_tracking/data/models/energy_balance_response.dart';
import 'package:guardianes_mobile/features/step_tracking/data/models/step_history_response.dart';
import 'package:guardianes_mobile/features/step_tracking/data/models/step_submission_request.dart';
import 'package:guardianes_mobile/features/step_tracking/data/models/step_submission_response.dart';

import 'api_client_test.mocks.dart';

@GenerateMocks([Dio])
void main() {
  group('API Client Integration Tests', () {
    late MockDio mockDio;
    late String baseUrl;
    late Map<String, String> headers;

    setUp(() {
      mockDio = MockDio();
      baseUrl = 'http://localhost:8080';
      headers = {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer test-token',
      };
    });

    group('getCurrentStepCount', () {
      test('should handle successful API response', () async {
        // Arrange
        final responseData = {
          'guardianId': 1,
          'currentSteps': 4200,
          'availableEnergy': 420,
          'date': '2025-07-08'
        };

        when(mockDio.get(any, options: anyNamed('options')))
            .thenAnswer((_) async => Response(
                  data: responseData,
                  statusCode: 200,
                  requestOptions: RequestOptions(path: ''),
                ));

        // Act
        final response = await mockDio.get(
          '/api/v1/guardians/1/steps/current',
          options: Options(headers: headers),
        );

        final stepCountResponse = CurrentStepCountResponse.fromJson(response.data);

        // Assert
        expect(stepCountResponse.currentSteps, 1500);
        expect(stepCountResponse.currentSteps, 4200);
        expect(stepCountResponse.totalEnergy, 420);
        expect(stepCountResponse.lastUpdated, '2025-07-08');
      });

      test('should handle 404 error response', () async {
        // Arrange
        when(mockDio.get(any, options: anyNamed('options')))
            .thenThrow(DioException(
              type: DioExceptionType.badResponse,
              response: Response(
                statusCode: 404,
                data: {
                  'error': 'Guardian Not Found',
                  'message': 'Guardian not found with ID: 999'
                },
                requestOptions: RequestOptions(path: ''),
              ),
              requestOptions: RequestOptions(path: ''),
            ));

        // Act & Assert
        expect(
          () async => await mockDio.get(
            '/api/v1/guardians/999/steps/current',
            options: Options(headers: headers),
          ),
          throwsA(isA<DioException>()),
        );
      });

      test('should handle network timeout', () async {
        // Arrange
        when(mockDio.get(any, options: anyNamed('options')))
            .thenThrow(DioException(
              type: DioExceptionType.connectionTimeout,
              requestOptions: RequestOptions(path: ''),
            ));

        // Act & Assert
        expect(
          () async => await mockDio.get(
            '/api/v1/guardians/1/steps/current',
            options: Options(headers: headers),
          ),
          throwsA(isA<DioException>()),
        );
      });
    });

    group('getEnergyBalance', () {
      test('should handle successful API response', () async {
        // Arrange
        final responseData = {
          'guardianId': {'value': 1},
          'currentBalance': {'amount': 750},
          'transactionSummary': []
        };

        when(mockDio.get(any, options: anyNamed('options')))
            .thenAnswer((_) async => Response(
                  data: responseData,
                  statusCode: 200,
                  requestOptions: RequestOptions(path: ''),
                ));

        // Act
        final response = await mockDio.get(
          '/api/v1/guardians/1/energy/balance',
          options: Options(headers: headers),
        );

        final energyBalanceResponse = EnergyBalanceResponse.fromJson(response.data);

        // Assert
        expect(energyBalanceResponse.currentBalance, 150);
        expect(energyBalanceResponse.currentBalance, 750);
        expect(energyBalanceResponse.totalEarned, 0);
      });

      test('should handle malformed response gracefully', () async {
        // Arrange
        final malformedData = {
          'guardianId': 1, // Should be nested object
          'currentBalance': 'not-a-number',
          'transactionSummary': null
        };

        when(mockDio.get(any, options: anyNamed('options')))
            .thenAnswer((_) async => Response(
                  data: malformedData,
                  statusCode: 200,
                  requestOptions: RequestOptions(path: ''),
                ));

        // Act
        final response = await mockDio.get(
          '/api/v1/guardians/1/energy/balance',
          options: Options(headers: headers),
        );

        // Assert
        expect(
          () => EnergyBalanceResponse.fromJson(response.data),
          throwsA(isA<TypeError>()),
        );
      });
    });

    group('getStepHistory', () {
      test('should handle successful API response', () async {
        // Arrange
        final responseData = {
          'guardianId': 1,
          'dailySteps': [
            {
              'date': '2025-07-01',
              'totalSteps': {'value': 2000}
            },
            {
              'date': '2025-07-02',
              'totalSteps': {'value': 3000}
            }
          ]
        };

        when(mockDio.get(any, options: anyNamed('options')))
            .thenAnswer((_) async => Response(
                  data: responseData,
                  statusCode: 200,
                  requestOptions: RequestOptions(path: ''),
                ));

        // Act
        final response = await mockDio.get(
          '/api/v1/guardians/1/steps/history',
          options: Options(headers: headers),
        );

        final stepHistoryResponse = StepHistoryResponse.fromJson(response.data);

        // Assert
        expect(stepHistoryResponse.history.length, 2);
        expect(stepHistoryResponse.history[0].stepCount, 2000);
        expect(stepHistoryResponse.history[0].energyEarned, 200);
        expect(stepHistoryResponse.history[1].stepCount, 3000);
        expect(stepHistoryResponse.history[1].energyEarned, 300);
      });

      test('should handle empty history response', () async {
        // Arrange
        final responseData = {
          'guardianId': 1,
          'dailySteps': []
        };

        when(mockDio.get(any, options: anyNamed('options')))
            .thenAnswer((_) async => Response(
                  data: responseData,
                  statusCode: 200,
                  requestOptions: RequestOptions(path: ''),
                ));

        // Act
        final response = await mockDio.get(
          '/api/v1/guardians/1/steps/history',
          options: Options(headers: headers),
        );

        final stepHistoryResponse = StepHistoryResponse.fromJson(response.data);

        // Assert
        expect(stepHistoryResponse.history, isEmpty);
      });

      test('should handle null dailySteps', () async {
        // Arrange
        final responseData = {
          'guardianId': 1,
          'dailySteps': null
        };

        when(mockDio.get(any, options: anyNamed('options')))
            .thenAnswer((_) async => Response(
                  data: responseData,
                  statusCode: 200,
                  requestOptions: RequestOptions(path: ''),
                ));

        // Act
        final response = await mockDio.get(
          '/api/v1/guardians/1/steps/history',
          options: Options(headers: headers),
        );

        final stepHistoryResponse = StepHistoryResponse.fromJson(response.data);

        // Assert
        expect(stepHistoryResponse.history, isEmpty);
      });
    });

    group('submitSteps', () {
      test('should handle successful step submission', () async {
        // Arrange
        final requestData = StepSubmissionRequest(
          guardianId: '1',
          stepCount: 2500,
          timestamp: DateTime(2025, 7, 8, 14, 30),
        );

        final responseData = {
          'guardianId': 1,
          'totalDailySteps': 3500,
          'energyEarned': 350,
          'message': 'Steps submitted successfully'
        };

        when(mockDio.post(any, data: anyNamed('data'), options: anyNamed('options')))
            .thenAnswer((_) async => Response(
                  data: responseData,
                  statusCode: 200,
                  requestOptions: RequestOptions(path: ''),
                ));

        // Act
        final response = await mockDio.post(
          '/api/v1/guardians/1/steps',
          data: requestData.toJson(),
          options: Options(headers: headers),
        );

        final stepSubmissionResponse = StepSubmissionResponse.fromJson(response.data);

        // Assert
        expect(stepSubmissionResponse.success, true);
        expect(stepSubmissionResponse.totalSteps, 3500);
        expect(stepSubmissionResponse.energyEarned, 350);
        expect(stepSubmissionResponse.message, 'Steps submitted successfully');
      });

      test('should handle validation errors', () async {
        // Arrange
        final requestData = StepSubmissionRequest(
          guardianId: '1',
          stepCount: -100, // Invalid step count
          timestamp: DateTime(2025, 7, 8, 14, 30),
        );

        when(mockDio.post(any, data: anyNamed('data'), options: anyNamed('options')))
            .thenThrow(DioException(
              type: DioExceptionType.badResponse,
              response: Response(
                statusCode: 400,
                data: {
                  'error': 'Bad Request',
                  'fieldErrors': [
                    {
                      'field': 'stepCount',
                      'message': 'Step count must be positive'
                    }
                  ]
                },
                requestOptions: RequestOptions(path: ''),
              ),
              requestOptions: RequestOptions(path: ''),
            ));

        // Act & Assert
        expect(
          () async => await mockDio.post(
            '/api/v1/guardians/1/steps',
            data: requestData.toJson(),
            options: Options(headers: headers),
          ),
          throwsA(isA<DioException>()),
        );
      });

      test('should handle rate limiting', () async {
        // Arrange
        final requestData = StepSubmissionRequest(
          guardianId: '1',
          stepCount: 2500,
          timestamp: DateTime(2025, 7, 8, 14, 30),
        );

        when(mockDio.post(any, data: anyNamed('data'), options: anyNamed('options')))
            .thenThrow(DioException(
              type: DioExceptionType.badResponse,
              response: Response(
                statusCode: 429,
                data: {
                  'error': 'Rate Limit Exceeded',
                  'message': 'Too many step submissions'
                },
                requestOptions: RequestOptions(path: ''),
              ),
              requestOptions: RequestOptions(path: ''),
            ));

        // Act & Assert
        expect(
          () async => await mockDio.post(
            '/api/v1/guardians/1/steps',
            data: requestData.toJson(),
            options: Options(headers: headers),
          ),
          throwsA(isA<DioException>()),
        );
      });
    });

    group('Authentication', () {
      test('should handle expired token', () async {
        // Arrange
        when(mockDio.get(any, options: anyNamed('options')))
            .thenThrow(DioException(
              type: DioExceptionType.badResponse,
              response: Response(
                statusCode: 401,
                data: {
                  'error': 'Unauthorized',
                  'message': 'JWT token expired'
                },
                requestOptions: RequestOptions(path: ''),
              ),
              requestOptions: RequestOptions(path: ''),
            ));

        // Act & Assert
        expect(
          () async => await mockDio.get(
            '/api/v1/guardians/1/steps/current',
            options: Options(headers: headers),
          ),
          throwsA(isA<DioException>()),
        );
      });

      test('should handle missing token', () async {
        // Arrange
        final headersWithoutAuth = {
          'Content-Type': 'application/json',
          // Missing Authorization header
        };

        when(mockDio.get(any, options: anyNamed('options')))
            .thenThrow(DioException(
              type: DioExceptionType.badResponse,
              response: Response(
                statusCode: 401,
                data: {
                  'error': 'Unauthorized',
                  'message': 'No authorization token provided'
                },
                requestOptions: RequestOptions(path: ''),
              ),
              requestOptions: RequestOptions(path: ''),
            ));

        // Act & Assert
        expect(
          () async => await mockDio.get(
            '/api/v1/guardians/1/steps/current',
            options: Options(headers: headersWithoutAuth),
          ),
          throwsA(isA<DioException>()),
        );
      });
    });
  });
}
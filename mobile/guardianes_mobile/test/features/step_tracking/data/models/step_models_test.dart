import 'package:flutter_test/flutter_test.dart';
import 'package:guardianes_mobile/features/step_tracking/data/models/current_step_count_response.dart';
import 'package:guardianes_mobile/features/step_tracking/data/models/energy_balance_response.dart';
import 'package:guardianes_mobile/features/step_tracking/data/models/step_history_response.dart';
import 'package:guardianes_mobile/features/step_tracking/data/models/step_submission_request.dart';
import 'package:guardianes_mobile/features/step_tracking/data/models/step_submission_response.dart';

void main() {
  group('Step Tracking Models - JSON Serialization', () {
    group('CurrentStepCountResponse', () {
      test('should deserialize from API response format', () {
        // This matches the actual backend API response
        final json = {
          'currentSteps': 4200,
          'availableEnergy': 420,
          'date': '2025-07-08'
        };

        final response = CurrentStepCountResponse.fromJson(json);

        expect(response.currentSteps, 4200);
        expect(response.totalEnergy, 420);
        expect(response.lastUpdated, '2025-07-08');
      });

      test('should serialize to JSON correctly', () {
        final response = CurrentStepCountResponse(
          currentSteps: 4200,
          totalEnergy: 420,
          lastUpdated: '2025-07-08',
        );

        final json = response.toJson();

        expect(json['currentSteps'], 4200);
        expect(json['availableEnergy'], 420);
        expect(json['date'], '2025-07-08');
      });

      test('should handle missing fields', () {
        final json = {
          'currentSteps': 4200,
          // Missing availableEnergy and date
        };

        expect(() => CurrentStepCountResponse.fromJson(json), throwsA(isA<TypeError>()));
      });
    });

    group('EnergyBalanceResponse', () {
      test('should deserialize from API response format', () {
        // This matches the actual backend API response
        final json = {
          'currentBalance': {'amount': 750},
        };

        final response = EnergyBalanceResponse.fromJson(json);

        expect(response.currentBalance, 750);
        expect(response.totalEarned, 0);
        expect(response.totalSpent, 0);
      });

      test('should handle nested amount extraction', () {
        final json = {
          'currentBalance': {'amount': 350},
        };

        final response = EnergyBalanceResponse.fromJson(json);

        expect(response.currentBalance, 350);
      });

      test('should handle direct amount value', () {
        final json = {
          'currentBalance': 500, // Direct value instead of nested
        };

        final response = EnergyBalanceResponse.fromJson(json);

        expect(response.currentBalance, 500);
      });

      test('should handle zero balance', () {
        final json = {
          'currentBalance': {'amount': 0},
        };

        final response = EnergyBalanceResponse.fromJson(json);

        expect(response.currentBalance, 0);
        expect(response.totalEarned, 0);
        expect(response.totalSpent, 0);
      });
    });

    group('StepHistoryResponse', () {
      test('should deserialize from API response format', () {
        // This matches the actual backend API response
        final json = {
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

        final response = StepHistoryResponse.fromJson(json);

        expect(response.history.length, 2);
        expect(response.history[0].stepCount, 2000);
        expect(response.history[0].energyEarned, 200); // 2000 / 10
        expect(response.history[1].stepCount, 3000);
        expect(response.history[1].energyEarned, 300); // 3000 / 10
      });

      test('should handle null dailySteps', () {
        final json = {
          'dailySteps': null
        };

        final response = StepHistoryResponse.fromJson(json);

        expect(response.history, isEmpty);
      });

      test('should handle missing dailySteps field', () {
        final json = <String, dynamic>{};

        final response = StepHistoryResponse.fromJson(json);

        expect(response.history, isEmpty);
      });

      test('should handle alternative step count format', () {
        final json = {
          'dailySteps': [
            {
              'date': '2025-07-01',
              'totalStepsValue': 2500 // Alternative format
            }
          ]
        };

        final response = StepHistoryResponse.fromJson(json);

        expect(response.history.length, 1);
        expect(response.history[0].stepCount, 2500);
        expect(response.history[0].energyEarned, 250);
      });
    });

    group('StepSubmissionRequest', () {
      test('should serialize to JSON correctly', () {
        final request = StepSubmissionRequest(
          guardianId: '1',
          stepCount: 2500,
          timestamp: DateTime(2025, 7, 8, 14, 30),
        );

        final json = request.toJson();

        expect(json['guardianId'], '1');
        expect(json['stepCount'], 2500);
        expect(json['timestamp'], '2025-07-08T14:30:00.000');
      });

      test('should deserialize from JSON correctly', () {
        final json = {
          'guardianId': '1',
          'stepCount': 2500,
          'timestamp': '2025-07-08T14:30:00.000'
        };

        final request = StepSubmissionRequest.fromJson(json);

        expect(request.guardianId, '1');
        expect(request.stepCount, 2500);
        expect(request.timestamp, DateTime(2025, 7, 8, 14, 30));
      });

      test('should handle optional source field', () {
        final request = StepSubmissionRequest(
          guardianId: '1',
          stepCount: 2500,
          timestamp: DateTime(2025, 7, 8, 14, 30),
          source: 'MANUAL',
        );

        final json = request.toJson();

        expect(json['source'], 'MANUAL');
      });
    });

    group('StepSubmissionResponse', () {
      test('should deserialize from API response format', () {
        final json = {
          'success': true,
          'message': 'Steps submitted successfully',
          'energyEarned': 350,
          'totalDailySteps': 3500
        };

        final response = StepSubmissionResponse.fromJson(json);

        expect(response.success, true);
        expect(response.message, 'Steps submitted successfully');
        expect(response.energyEarned, 350);
        expect(response.totalSteps, 3500);
      });

      test('should handle missing success field with default', () {
        final json = {
          'message': 'Steps submitted successfully',
          'energyEarned': 350,
          'totalDailySteps': 3500
        };

        final response = StepSubmissionResponse.fromJson(json);

        expect(response.success, true); // Default value
        expect(response.message, 'Steps submitted successfully');
        expect(response.energyEarned, 350);
        expect(response.totalSteps, 3500);
      });

      test('should handle failure response', () {
        final json = {
          'success': false,
          'message': 'Invalid step count',
          'energyEarned': 0,
          'totalDailySteps': 0
        };

        final response = StepSubmissionResponse.fromJson(json);

        expect(response.success, false);
        expect(response.message, 'Invalid step count');
        expect(response.energyEarned, 0);
        expect(response.totalSteps, 0);
      });
    });
  });

  group('Error Handling', () {
    test('should handle malformed JSON gracefully', () {
      final malformedJson = {
        'currentSteps': 'not-a-number',
        'availableEnergy': 'not-a-number',
      };

      expect(() => CurrentStepCountResponse.fromJson(malformedJson), throwsA(isA<TypeError>()));
    });

    test('should handle null values', () {
      final nullJson = {
        'currentSteps': null,
        'availableEnergy': null,
      };

      expect(() => CurrentStepCountResponse.fromJson(nullJson), throwsA(isA<TypeError>()));
    });

    test('should handle empty JSON', () {
      final emptyJson = <String, dynamic>{};

      expect(() => CurrentStepCountResponse.fromJson(emptyJson), throwsA(isA<TypeError>()));
    });
  });

  group('Edge Cases', () {
    test('should handle zero values correctly', () {
      final json = {
        'currentSteps': 0,
        'availableEnergy': 0,
        'date': '2025-07-08'
      };

      final response = CurrentStepCountResponse.fromJson(json);

      expect(response.currentSteps, 0);
      expect(response.totalEnergy, 0);
    });

    test('should handle negative values', () {
      final json = {
        'currentSteps': -100,
        'availableEnergy': -50,
        'date': '2025-07-08'
      };

      final response = CurrentStepCountResponse.fromJson(json);

      expect(response.currentSteps, -100);
      expect(response.totalEnergy, -50);
    });

    test('should handle very large numbers', () {
      final json = {
        'currentSteps': 1000000,
        'availableEnergy': 100000,
        'date': '2025-07-08'
      };

      final response = CurrentStepCountResponse.fromJson(json);

      expect(response.currentSteps, 1000000);
      expect(response.totalEnergy, 100000);
    });
  });
}
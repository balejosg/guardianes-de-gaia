import 'package:flutter_test/flutter_test.dart';

import 'package:guardianes_mobile/features/walking/domain/validators/step_validation.dart';

void main() {
  group('StepValidationResult', () {
    test('valid factory creates valid result', () {
      final result = StepValidationResult.valid();
      expect(result.isValid, true);
      expect(result.errorMessage, isNull);
      expect(result.warnings, isEmpty);
    });

    test('valid factory with warnings', () {
      final result = StepValidationResult.valid(warnings: ['Warning 1']);
      expect(result.isValid, true);
      expect(result.warnings, ['Warning 1']);
    });

    test('invalid factory creates invalid result', () {
      final result = StepValidationResult.invalid('Error message');
      expect(result.isValid, false);
      expect(result.errorMessage, 'Error message');
    });
  });

  group('StepValidator.validateStepSubmission', () {
    test('should reject invalid guardian ID', () {
      final result = StepValidator.validateStepSubmission(
        stepCount: 100,
        timestamp: DateTime.now().toIso8601String(),
        guardianId: 0,
      );
      expect(result.isValid, false);
      expect(result.errorMessage, contains('Invalid guardian ID'));
    });

    test('should reject negative guardian ID', () {
      final result = StepValidator.validateStepSubmission(
        stepCount: 100,
        timestamp: DateTime.now().toIso8601String(),
        guardianId: -1,
      );
      expect(result.isValid, false);
    });

    test('should reject step count below minimum', () {
      final result = StepValidator.validateStepSubmission(
        stepCount: 0,
        timestamp: DateTime.now().toIso8601String(),
        guardianId: 1,
      );
      expect(result.isValid, false);
      expect(result.errorMessage, contains('at least'));
    });

    test('should reject step count above maximum', () {
      final result = StepValidator.validateStepSubmission(
        stepCount: 60000,
        timestamp: DateTime.now().toIso8601String(),
        guardianId: 1,
      );
      expect(result.isValid, false);
      expect(result.errorMessage, contains('cannot exceed'));
    });

    test('should reject empty timestamp', () {
      final result = StepValidator.validateStepSubmission(
        stepCount: 100,
        timestamp: '',
        guardianId: 1,
      );
      expect(result.isValid, false);
      expect(result.errorMessage, contains('required'));
    });

    test('should reject invalid timestamp format', () {
      final result = StepValidator.validateStepSubmission(
        stepCount: 100,
        timestamp: 'invalid-timestamp',
        guardianId: 1,
      );
      expect(result.isValid, false);
      expect(result.errorMessage, contains('Invalid timestamp'));
    });

    test('should reject future timestamp', () {
      final future = DateTime.now().add(const Duration(hours: 1));
      final result = StepValidator.validateStepSubmission(
        stepCount: 100,
        timestamp: future.toIso8601String(),
        guardianId: 1,
      );
      expect(result.isValid, false);
      expect(result.errorMessage, contains('future'));
    });

    test('should warn for old timestamp', () {
      final old = DateTime.now().subtract(const Duration(hours: 30));
      final result = StepValidator.validateStepSubmission(
        stepCount: 100,
        timestamp: old.toIso8601String(),
        guardianId: 1,
      );
      expect(result.isValid, true);
      expect(result.warnings, contains('more than 24 hours old'));
    });

    test('should reject if daily total would exceed max', () {
      final result = StepValidator.validateStepSubmission(
        stepCount: 50000,
        timestamp: DateTime.now().toIso8601String(),
        guardianId: 1,
        currentDailyTotal: 60000,
      );
      expect(result.isValid, false);
      expect(result.errorMessage, contains('exceed'));
    });

    test('should warn for anomaly high step count', () {
      final result = StepValidator.validateStepSubmission(
        stepCount: 20000,
        timestamp: DateTime.now().toIso8601String(),
        guardianId: 1,
      );
      expect(result.isValid, true);
      expect(result.warnings, contains('Unusually high step count'));
    });

    test('should pass valid submission', () {
      final result = StepValidator.validateStepSubmission(
        stepCount: 5000,
        timestamp: DateTime.now().toIso8601String(),
        guardianId: 1,
      );
      expect(result.isValid, true);
      expect(result.warnings, isEmpty);
    });
  });

  group('StepValidator.validateStepRate', () {
    test('should pass for zero time period', () {
      final result = StepValidator.validateStepRate(
        stepCount: 1000,
        timePeriod: Duration.zero,
      );
      expect(result.isValid, true);
    });

    test('should reject extremely high rate', () {
      final result = StepValidator.validateStepRate(
        stepCount: 500,
        timePeriod: const Duration(minutes: 2),
      );
      expect(result.isValid, false);
      expect(result.errorMessage, contains('too high'));
    });

    test('should warn for high rate', () {
      final result = StepValidator.validateStepRate(
        stepCount: 370,
        timePeriod: const Duration(minutes: 2),
      );
      expect(result.isValid, true);
      expect(result.warnings, isNotEmpty);
    });

    test('should pass for normal walking rate', () {
      final result = StepValidator.validateStepRate(
        stepCount: 1000,
        timePeriod: const Duration(minutes: 10),
      );
      expect(result.isValid, true);
      expect(result.warnings, isEmpty);
    });
  });

  group('StepValidator.validateEnergyCalculation', () {
    test('should pass correct energy calculation', () {
      final result = StepValidator.validateEnergyCalculation(
        stepCount: 100,
        expectedEnergy: 10,
      );
      expect(result.isValid, true);
    });

    test('should fail incorrect energy calculation', () {
      final result = StepValidator.validateEnergyCalculation(
        stepCount: 100,
        expectedEnergy: 15,
      );
      expect(result.isValid, false);
      expect(result.errorMessage, contains('mismatch'));
    });
  });

  group('StepValidator.validateSubmissionRate', () {
    test('should pass when under rate limit', () {
      final submissions = [
        DateTime.now().subtract(const Duration(minutes: 5)),
        DateTime.now().subtract(const Duration(minutes: 10)),
      ];
      final result = StepValidator.validateSubmissionRate(
        recentSubmissions: submissions,
        timeWindow: const Duration(hours: 1),
        maxSubmissions: 10,
      );
      expect(result.isValid, true);
    });

    test('should fail when rate limit exceeded', () {
      final submissions = List.generate(
        15,
        (i) => DateTime.now().subtract(Duration(minutes: i)),
      );
      final result = StepValidator.validateSubmissionRate(
        recentSubmissions: submissions,
        timeWindow: const Duration(hours: 1),
        maxSubmissions: 10,
      );
      expect(result.isValid, false);
      expect(result.errorMessage, contains('Too many'));
    });
  });

  group('StepValidator.validateCompleteSubmission', () {
    test('should combine all validations', () {
      final result = StepValidator.validateCompleteSubmission(
        stepCount: 5000,
        timestamp: DateTime.now().toIso8601String(),
        guardianId: 1,
      );
      expect(result.isValid, true);
    });

    test('should fail on basic validation failure', () {
      final result = StepValidator.validateCompleteSubmission(
        stepCount: 0,
        timestamp: DateTime.now().toIso8601String(),
        guardianId: 1,
      );
      expect(result.isValid, false);
    });

    test('should include rate validation when time provided', () {
      final result = StepValidator.validateCompleteSubmission(
        stepCount: 500,
        timestamp: DateTime.now().toIso8601String(),
        guardianId: 1,
        timeSinceLastSubmission: const Duration(minutes: 2),
      );
      expect(result.isValid, false);
    });

    test('should include frequency validation', () {
      final submissions = List.generate(
        105,
        (i) => DateTime.now().subtract(Duration(minutes: i % 60)),
      );
      final result = StepValidator.validateCompleteSubmission(
        stepCount: 100,
        timestamp: DateTime.now().toIso8601String(),
        guardianId: 1,
        recentSubmissions: submissions,
      );
      expect(result.isValid, false);
    });
  });
}

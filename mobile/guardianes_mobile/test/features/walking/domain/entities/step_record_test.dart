import 'package:flutter_test/flutter_test.dart';
import 'package:guardianes_mobile/features/walking/domain/entities/step_record.dart';

void main() {
  group('StepRecord', () {
    test('should create a step record with all required fields', () {
      // Arrange
      const stepRecord = StepRecord(
        guardianId: 1,
        stepCount: 2500,
        timestamp: '2025-07-16T14:30:00',
      );

      // Assert
      expect(stepRecord.guardianId, equals(1));
      expect(stepRecord.stepCount, equals(2500));
      expect(stepRecord.timestamp, equals('2025-07-16T14:30:00'));
    });

    test('should support equality comparison', () {
      // Arrange
      const stepRecord1 = StepRecord(
        guardianId: 1,
        stepCount: 2500,
        timestamp: '2025-07-16T14:30:00',
      );
      const stepRecord2 = StepRecord(
        guardianId: 1,
        stepCount: 2500,
        timestamp: '2025-07-16T14:30:00',
      );
      const stepRecord3 = StepRecord(
        guardianId: 2,
        stepCount: 2500,
        timestamp: '2025-07-16T14:30:00',
      );

      // Assert
      expect(stepRecord1, equals(stepRecord2));
      expect(stepRecord1, isNot(equals(stepRecord3)));
    });

    test('should support props for equatable', () {
      // Arrange
      const stepRecord = StepRecord(
        guardianId: 1,
        stepCount: 2500,
        timestamp: '2025-07-16T14:30:00',
      );

      // Assert
      expect(stepRecord.props, equals([1, 2500, '2025-07-16T14:30:00']));
    });

    test('should calculate energy correctly (1 energy per 10 steps)', () {
      // Arrange
      const stepRecord = StepRecord(
        guardianId: 1,
        stepCount: 2500,
        timestamp: '2025-07-16T14:30:00',
      );

      // Act
      int calculatedEnergy = stepRecord.calculateEnergy();

      // Assert
      expect(calculatedEnergy, equals(250)); // 2500 steps = 250 energy
    });

    test('should calculate energy with rounding down for partial steps', () {
      // Arrange
      const stepRecord = StepRecord(
        guardianId: 1,
        stepCount: 2509,
        timestamp: '2025-07-16T14:30:00',
      );

      // Act
      int calculatedEnergy = stepRecord.calculateEnergy();

      // Assert
      expect(calculatedEnergy, equals(250)); // 2509 steps = 250 energy (rounded down)
    });

    test('should handle zero steps', () {
      // Arrange
      const stepRecord = StepRecord(
        guardianId: 1,
        stepCount: 0,
        timestamp: '2025-07-16T14:30:00',
      );

      // Act
      int calculatedEnergy = stepRecord.calculateEnergy();

      // Assert
      expect(calculatedEnergy, equals(0));
    });

    test('should handle single digit steps', () {
      // Arrange
      const stepRecord = StepRecord(
        guardianId: 1,
        stepCount: 5,
        timestamp: '2025-07-16T14:30:00',
      );

      // Act
      int calculatedEnergy = stepRecord.calculateEnergy();

      // Assert
      expect(calculatedEnergy, equals(0)); // 5 steps = 0 energy (needs at least 10 steps)
    });
  });
}
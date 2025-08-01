import 'package:flutter_test/flutter_test.dart';
import 'package:guardianes_mobile/features/walking/domain/entities/daily_step_aggregate.dart';

void main() {
  group('DailyStepAggregate', () {
    test('should create a daily step aggregate with all required fields', () {
      // Arrange
      const dailyStepAggregate = DailyStepAggregate(
        guardianId: 1,
        date: '2025-07-16',
        totalSteps: 5000,
      );

      // Assert
      expect(dailyStepAggregate.guardianId, equals(1));
      expect(dailyStepAggregate.date, equals('2025-07-16'));
      expect(dailyStepAggregate.totalSteps, equals(5000));
    });

    test('should support equality comparison', () {
      // Arrange
      const aggregate1 = DailyStepAggregate(
        guardianId: 1,
        date: '2025-07-16',
        totalSteps: 5000,
      );
      const aggregate2 = DailyStepAggregate(
        guardianId: 1,
        date: '2025-07-16',
        totalSteps: 5000,
      );
      const aggregate3 = DailyStepAggregate(
        guardianId: 1,
        date: '2025-07-16',
        totalSteps: 6000,
      );

      // Assert
      expect(aggregate1, equals(aggregate2));
      expect(aggregate1, isNot(equals(aggregate3)));
    });

    test('should support props for equatable', () {
      // Arrange
      const dailyStepAggregate = DailyStepAggregate(
        guardianId: 1,
        date: '2025-07-16',
        totalSteps: 5000,
      );

      // Assert
      expect(dailyStepAggregate.props, equals([1, '2025-07-16', 5000]));
    });

    test('should calculate total energy correctly', () {
      // Arrange
      const dailyStepAggregate = DailyStepAggregate(
        guardianId: 1,
        date: '2025-07-16',
        totalSteps: 5000,
      );

      // Act
      int totalEnergy = dailyStepAggregate.calculateTotalEnergy();

      // Assert
      expect(totalEnergy, equals(500)); // 5000 steps = 500 energy
    });

    test('should calculate total energy with rounding down', () {
      // Arrange
      const dailyStepAggregate = DailyStepAggregate(
        guardianId: 1,
        date: '2025-07-16',
        totalSteps: 5009,
      );

      // Act
      int totalEnergy = dailyStepAggregate.calculateTotalEnergy();

      // Assert
      expect(
          totalEnergy, equals(500)); // 5009 steps = 500 energy (rounded down)
    });

    test('should handle zero steps', () {
      // Arrange
      const dailyStepAggregate = DailyStepAggregate(
        guardianId: 1,
        date: '2025-07-16',
        totalSteps: 0,
      );

      // Act
      int totalEnergy = dailyStepAggregate.calculateTotalEnergy();

      // Assert
      expect(totalEnergy, equals(0));
    });

    test('should indicate if goal is reached (8000 steps)', () {
      // Arrange
      const underGoal = DailyStepAggregate(
        guardianId: 1,
        date: '2025-07-16',
        totalSteps: 7999,
      );
      const atGoal = DailyStepAggregate(
        guardianId: 1,
        date: '2025-07-16',
        totalSteps: 8000,
      );
      const overGoal = DailyStepAggregate(
        guardianId: 1,
        date: '2025-07-16',
        totalSteps: 10000,
      );

      // Assert
      expect(underGoal.isGoalReached(), isFalse);
      expect(atGoal.isGoalReached(), isTrue);
      expect(overGoal.isGoalReached(), isTrue);
    });
  });
}

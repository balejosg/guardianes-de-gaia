import 'package:flutter_test/flutter_test.dart';
import 'package:guardianes_mobile/features/walking/domain/entities/step_record.dart';
import 'package:guardianes_mobile/features/walking/domain/entities/daily_step_aggregate.dart';
import 'package:guardianes_mobile/features/walking/domain/repositories/step_repository.dart';

class MockStepRepository implements StepRepository {
  final List<StepRecord> _stepRecords = [];
  final List<DailyStepAggregate> _dailyAggregates = [];

  @override
  Future<void> submitSteps(StepRecord stepRecord) async {
    _stepRecords.add(stepRecord);
  }

  @override
  Future<DailyStepAggregate> getCurrentStepCount(int guardianId) async {
    final today = DateTime.now().toIso8601String().split('T')[0];
    return _dailyAggregates.firstWhere(
      (aggregate) =>
          aggregate.guardianId == guardianId && aggregate.date == today,
      orElse: () => DailyStepAggregate(
        guardianId: guardianId,
        date: today,
        totalSteps: 0,
      ),
    );
  }

  @override
  Future<List<DailyStepAggregate>> getStepHistory(
    int guardianId,
    String fromDate,
    String toDate,
  ) async {
    return _dailyAggregates
        .where((aggregate) =>
            aggregate.guardianId == guardianId &&
            aggregate.date.compareTo(fromDate) >= 0 &&
            aggregate.date.compareTo(toDate) <= 0)
        .toList();
  }

  // Helper method for testing
  void addDailyAggregate(DailyStepAggregate aggregate) {
    _dailyAggregates.add(aggregate);
  }
}

void main() {
  group('StepRepository', () {
    late MockStepRepository repository;

    setUp(() {
      repository = MockStepRepository();
    });

    test('should have correct method signatures', () {
      // This test ensures the interface is correctly defined
      expect(repository, isA<StepRepository>());
      expect(repository.submitSteps, isA<Function>());
      expect(repository.getCurrentStepCount, isA<Function>());
      expect(repository.getStepHistory, isA<Function>());
    });

    test('should submit steps successfully', () async {
      // Arrange
      const stepRecord = StepRecord(
        guardianId: 1,
        stepCount: 2500,
        timestamp: '2025-07-16T14:30:00',
      );

      // Act
      await repository.submitSteps(stepRecord);

      // Assert
      expect(repository._stepRecords, contains(stepRecord));
    });

    test('should get current step count for today', () async {
      // Arrange
      const guardianId = 1;
      final today = DateTime.now().toIso8601String().split('T')[0];
      repository.addDailyAggregate(DailyStepAggregate(
        guardianId: guardianId,
        date: today,
        totalSteps: 3000,
      ));

      // Act
      final result = await repository.getCurrentStepCount(guardianId);

      // Assert
      expect(result.guardianId, equals(guardianId));
      expect(result.date, equals(today));
      expect(result.totalSteps, equals(3000));
    });

    test('should return zero steps for guardian with no data', () async {
      // Arrange
      const guardianId = 999;

      // Act
      final result = await repository.getCurrentStepCount(guardianId);

      // Assert
      expect(result.guardianId, equals(guardianId));
      expect(result.totalSteps, equals(0));
    });

    test('should get step history within date range', () async {
      // Arrange
      const guardianId = 1;
      const aggregate1 = DailyStepAggregate(
        guardianId: guardianId,
        date: '2025-07-14',
        totalSteps: 5000,
      );
      const aggregate2 = DailyStepAggregate(
        guardianId: guardianId,
        date: '2025-07-15',
        totalSteps: 6000,
      );
      const aggregate3 = DailyStepAggregate(
        guardianId: guardianId,
        date: '2025-07-16',
        totalSteps: 7000,
      );

      repository.addDailyAggregate(aggregate1);
      repository.addDailyAggregate(aggregate2);
      repository.addDailyAggregate(aggregate3);

      // Act
      final result = await repository.getStepHistory(
        guardianId,
        '2025-07-15',
        '2025-07-16',
      );

      // Assert
      expect(result.length, equals(2));
      expect(result, contains(aggregate2));
      expect(result, contains(aggregate3));
      expect(result, isNot(contains(aggregate1)));
    });

    test('should filter step history by guardian ID', () async {
      // Arrange
      const guardian1Aggregate = DailyStepAggregate(
        guardianId: 1,
        date: '2025-07-15',
        totalSteps: 5000,
      );
      const guardian2Aggregate = DailyStepAggregate(
        guardianId: 2,
        date: '2025-07-15',
        totalSteps: 6000,
      );

      repository.addDailyAggregate(guardian1Aggregate);
      repository.addDailyAggregate(guardian2Aggregate);

      // Act
      final result = await repository.getStepHistory(
        1,
        '2025-07-15',
        '2025-07-15',
      );

      // Assert
      expect(result.length, equals(1));
      expect(result.first.guardianId, equals(1));
      expect(result.first.totalSteps, equals(5000));
    });

    test('should return empty list when no data in date range', () async {
      // Arrange
      const guardianId = 1;
      const aggregate = DailyStepAggregate(
        guardianId: guardianId,
        date: '2025-07-10',
        totalSteps: 5000,
      );

      repository.addDailyAggregate(aggregate);

      // Act
      final result = await repository.getStepHistory(
        guardianId,
        '2025-07-15',
        '2025-07-16',
      );

      // Assert
      expect(result, isEmpty);
    });
  });
}

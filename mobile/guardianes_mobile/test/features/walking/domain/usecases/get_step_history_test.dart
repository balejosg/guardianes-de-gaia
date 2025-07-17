import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:mockito/annotations.dart';
import 'package:guardianes_mobile/features/walking/domain/entities/daily_step_aggregate.dart';
import 'package:guardianes_mobile/features/walking/domain/repositories/step_repository.dart';
import 'package:guardianes_mobile/features/walking/domain/usecases/get_step_history.dart';

import 'get_step_history_test.mocks.dart';

@GenerateMocks([StepRepository])
void main() {
  late GetStepHistory usecase;
  late MockStepRepository mockStepRepository;

  setUp(() {
    mockStepRepository = MockStepRepository();
    usecase = GetStepHistory(mockStepRepository);
  });

  group('GetStepHistory', () {
    test('should get step history from repository', () async {
      // Arrange
      const guardianId = 1;
      const fromDate = '2025-07-14';
      const toDate = '2025-07-16';
      const expectedHistory = [
        DailyStepAggregate(
          guardianId: guardianId,
          date: '2025-07-14',
          totalSteps: 5000,
        ),
        DailyStepAggregate(
          guardianId: guardianId,
          date: '2025-07-15',
          totalSteps: 6000,
        ),
        DailyStepAggregate(
          guardianId: guardianId,
          date: '2025-07-16',
          totalSteps: 7000,
        ),
      ];

      when(mockStepRepository.getStepHistory(guardianId, fromDate, toDate))
          .thenAnswer((_) async => expectedHistory);

      // Act
      final result = await usecase.call(guardianId, fromDate, toDate);

      // Assert
      expect(result, equals(expectedHistory));
      verify(mockStepRepository.getStepHistory(guardianId, fromDate, toDate));
      verifyNoMoreInteractions(mockStepRepository);
    });

    test('should handle repository errors', () async {
      // Arrange
      const guardianId = 1;
      const fromDate = '2025-07-14';
      const toDate = '2025-07-16';

      when(mockStepRepository.getStepHistory(guardianId, fromDate, toDate))
          .thenThrow(Exception('Network error'));

      // Act & Assert
      expect(
        () => usecase.call(guardianId, fromDate, toDate),
        throwsException,
      );
    });

    test('should validate guardian ID is positive', () async {
      // Arrange
      const guardianId = -1;
      const fromDate = '2025-07-14';
      const toDate = '2025-07-16';

      // Act & Assert
      expect(
        () => usecase.call(guardianId, fromDate, toDate),
        throwsA(isA<ArgumentError>()),
      );
      
      verifyNever(mockStepRepository.getStepHistory(any, any, any));
    });

    test('should validate from date is not empty', () async {
      // Arrange
      const guardianId = 1;
      const fromDate = '';
      const toDate = '2025-07-16';

      // Act & Assert
      expect(
        () => usecase.call(guardianId, fromDate, toDate),
        throwsA(isA<ArgumentError>()),
      );
      
      verifyNever(mockStepRepository.getStepHistory(any, any, any));
    });

    test('should validate to date is not empty', () async {
      // Arrange
      const guardianId = 1;
      const fromDate = '2025-07-14';
      const toDate = '';

      // Act & Assert
      expect(
        () => usecase.call(guardianId, fromDate, toDate),
        throwsA(isA<ArgumentError>()),
      );
      
      verifyNever(mockStepRepository.getStepHistory(any, any, any));
    });

    test('should validate from date is before to date', () async {
      // Arrange
      const guardianId = 1;
      const fromDate = '2025-07-16';
      const toDate = '2025-07-14';

      // Act & Assert
      expect(
        () => usecase.call(guardianId, fromDate, toDate),
        throwsA(isA<ArgumentError>()),
      );
      
      verifyNever(mockStepRepository.getStepHistory(any, any, any));
    });

    test('should return empty list when no data found', () async {
      // Arrange
      const guardianId = 1;
      const fromDate = '2025-07-14';
      const toDate = '2025-07-16';
      const expectedHistory = <DailyStepAggregate>[];

      when(mockStepRepository.getStepHistory(guardianId, fromDate, toDate))
          .thenAnswer((_) async => expectedHistory);

      // Act
      final result = await usecase.call(guardianId, fromDate, toDate);

      // Assert
      expect(result, equals(expectedHistory));
      expect(result, isEmpty);
    });

    test('should validate date format (YYYY-MM-DD)', () async {
      // Arrange
      const guardianId = 1;
      const fromDate = '2025/07/14'; // Invalid format
      const toDate = '2025-07-16';

      // Act & Assert
      expect(
        () => usecase.call(guardianId, fromDate, toDate),
        throwsA(isA<ArgumentError>()),
      );
      
      verifyNever(mockStepRepository.getStepHistory(any, any, any));
    });
  });
}
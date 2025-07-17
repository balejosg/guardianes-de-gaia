import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:mockito/annotations.dart';
import 'package:guardianes_mobile/features/walking/domain/entities/daily_step_aggregate.dart';
import 'package:guardianes_mobile/features/walking/domain/repositories/step_repository.dart';
import 'package:guardianes_mobile/features/walking/domain/usecases/get_current_steps.dart';

import 'get_current_steps_test.mocks.dart';

@GenerateMocks([StepRepository])
void main() {
  late GetCurrentSteps usecase;
  late MockStepRepository mockStepRepository;

  setUp(() {
    mockStepRepository = MockStepRepository();
    usecase = GetCurrentSteps(mockStepRepository);
  });

  group('GetCurrentSteps', () {
    test('should get current steps from repository', () async {
      // Arrange
      const guardianId = 1;
      const expectedAggregate = DailyStepAggregate(
        guardianId: guardianId,
        date: '2025-07-16',
        totalSteps: 5000,
      );

      when(mockStepRepository.getCurrentStepCount(guardianId))
          .thenAnswer((_) async => expectedAggregate);

      // Act
      final result = await usecase.call(guardianId);

      // Assert
      expect(result, equals(expectedAggregate));
      verify(mockStepRepository.getCurrentStepCount(guardianId));
      verifyNoMoreInteractions(mockStepRepository);
    });

    test('should handle repository errors', () async {
      // Arrange
      const guardianId = 1;

      when(mockStepRepository.getCurrentStepCount(guardianId))
          .thenThrow(Exception('Network error'));

      // Act & Assert
      expect(
        () => usecase.call(guardianId),
        throwsException,
      );
    });

    test('should validate guardian ID is positive', () async {
      // Arrange
      const guardianId = -1;

      // Act & Assert
      expect(
        () => usecase.call(guardianId),
        throwsA(isA<ArgumentError>()),
      );
      
      verifyNever(mockStepRepository.getCurrentStepCount(any));
    });

    test('should validate guardian ID is not zero', () async {
      // Arrange
      const guardianId = 0;

      // Act & Assert
      expect(
        () => usecase.call(guardianId),
        throwsA(isA<ArgumentError>()),
      );
      
      verifyNever(mockStepRepository.getCurrentStepCount(any));
    });

    test('should return aggregate with zero steps for new guardian', () async {
      // Arrange
      const guardianId = 1;
      const expectedAggregate = DailyStepAggregate(
        guardianId: guardianId,
        date: '2025-07-16',
        totalSteps: 0,
      );

      when(mockStepRepository.getCurrentStepCount(guardianId))
          .thenAnswer((_) async => expectedAggregate);

      // Act
      final result = await usecase.call(guardianId);

      // Assert
      expect(result, equals(expectedAggregate));
      expect(result.totalSteps, equals(0));
      expect(result.calculateTotalEnergy(), equals(0));
    });
  });
}
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:mockito/annotations.dart';
import 'package:guardianes_mobile/features/walking/domain/entities/step_record.dart';
import 'package:guardianes_mobile/features/walking/domain/repositories/step_repository.dart';
import 'package:guardianes_mobile/features/walking/domain/usecases/submit_steps.dart';

import 'submit_steps_test.mocks.dart';

@GenerateMocks([StepRepository])
void main() {
  late SubmitSteps usecase;
  late MockStepRepository mockStepRepository;

  setUp(() {
    mockStepRepository = MockStepRepository();
    usecase = SubmitSteps(mockStepRepository);
  });

  group('SubmitSteps', () {
    test('should submit steps through repository', () async {
      // Arrange
      const stepRecord = StepRecord(
        guardianId: 1,
        stepCount: 2500,
        timestamp: '2025-07-16T14:30:00',
      );

      when(mockStepRepository.submitSteps(any))
          .thenAnswer((_) async => {});

      // Act
      await usecase.call(stepRecord);

      // Assert
      verify(mockStepRepository.submitSteps(stepRecord));
      verifyNoMoreInteractions(mockStepRepository);
    });

    test('should handle repository errors', () async {
      // Arrange
      const stepRecord = StepRecord(
        guardianId: 1,
        stepCount: 2500,
        timestamp: '2025-07-16T14:30:00',
      );

      when(mockStepRepository.submitSteps(any))
          .thenThrow(Exception('Network error'));

      // Act & Assert
      expect(
        () => usecase.call(stepRecord),
        throwsException,
      );
    });

    test('should validate step count is positive', () async {
      // Arrange
      const stepRecord = StepRecord(
        guardianId: 1,
        stepCount: 0,
        timestamp: '2025-07-16T14:30:00',
      );

      // Act & Assert
      expect(
        () => usecase.call(stepRecord),
        throwsA(isA<ArgumentError>()),
      );
      
      verifyNever(mockStepRepository.submitSteps(any));
    });

    test('should validate guardian ID is positive', () async {
      // Arrange
      const stepRecord = StepRecord(
        guardianId: -1,
        stepCount: 2500,
        timestamp: '2025-07-16T14:30:00',
      );

      // Act & Assert
      expect(
        () => usecase.call(stepRecord),
        throwsA(isA<ArgumentError>()),
      );
      
      verifyNever(mockStepRepository.submitSteps(any));
    });

    test('should validate timestamp is not empty', () async {
      // Arrange
      const stepRecord = StepRecord(
        guardianId: 1,
        stepCount: 2500,
        timestamp: '',
      );

      // Act & Assert
      expect(
        () => usecase.call(stepRecord),
        throwsA(isA<ArgumentError>()),
      );
      
      verifyNever(mockStepRepository.submitSteps(any));
    });
  });
}
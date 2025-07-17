import 'dart:convert';
import 'package:flutter_test/flutter_test.dart';
import 'package:guardianes_mobile/features/walking/data/models/current_step_count_response_dto.dart';

void main() {
  group('CurrentStepCountResponseDto', () {
    const tCurrentStepCountResponseDto = CurrentStepCountResponseDto(
      guardianId: 1,
      currentSteps: 5000,
      availableEnergy: 500,
      date: '2025-07-16',
    );

    test('should be a subclass of CurrentStepCountResponseDto', () {
      expect(tCurrentStepCountResponseDto, isA<CurrentStepCountResponseDto>());
    });

    test('should deserialize from JSON correctly', () {
      // Arrange
      final jsonMap = {
        'guardianId': 1,
        'currentSteps': 5000,
        'availableEnergy': 500,
        'date': '2025-07-16',
      };

      // Act
      final result = CurrentStepCountResponseDto.fromJson(jsonMap);

      // Assert
      expect(result, equals(tCurrentStepCountResponseDto));
    });

    test('should serialize to JSON correctly', () {
      // Act
      final result = tCurrentStepCountResponseDto.toJson();

      // Assert
      final expectedJson = {
        'guardianId': 1,
        'currentSteps': 5000,
        'availableEnergy': 500,
        'date': '2025-07-16',
      };
      expect(result, equals(expectedJson));
    });

    test('should handle toJson and fromJson round trip', () {
      // Act
      final json = tCurrentStepCountResponseDto.toJson();
      final result = CurrentStepCountResponseDto.fromJson(json);

      // Assert
      expect(result, equals(tCurrentStepCountResponseDto));
    });

    test('should support equality comparison', () {
      // Arrange
      const dto1 = CurrentStepCountResponseDto(
        guardianId: 1,
        currentSteps: 5000,
        availableEnergy: 500,
        date: '2025-07-16',
      );
      const dto2 = CurrentStepCountResponseDto(
        guardianId: 1,
        currentSteps: 5000,
        availableEnergy: 500,
        date: '2025-07-16',
      );
      const dto3 = CurrentStepCountResponseDto(
        guardianId: 2,
        currentSteps: 5000,
        availableEnergy: 500,
        date: '2025-07-16',
      );

      // Assert
      expect(dto1, equals(dto2));
      expect(dto1, isNot(equals(dto3)));
    });

    test('should handle JSON string conversion', () {
      // Arrange
      final jsonString = json.encode(tCurrentStepCountResponseDto.toJson());

      // Act
      final jsonMap = json.decode(jsonString) as Map<String, dynamic>;
      final result = CurrentStepCountResponseDto.fromJson(jsonMap);

      // Assert
      expect(result, equals(tCurrentStepCountResponseDto));
    });

    test('should convert to domain entity correctly', () {
      // Act
      final result = tCurrentStepCountResponseDto.toDomainEntity();

      // Assert
      expect(result.guardianId, equals(1));
      expect(result.totalSteps, equals(5000));
      expect(result.date, equals('2025-07-16'));
    });

    test('should handle zero values correctly', () {
      // Arrange
      const dto = CurrentStepCountResponseDto(
        guardianId: 1,
        currentSteps: 0,
        availableEnergy: 0,
        date: '2025-07-16',
      );

      // Act
      final domainEntity = dto.toDomainEntity();

      // Assert
      expect(domainEntity.totalSteps, equals(0));
      expect(domainEntity.calculateTotalEnergy(), equals(0));
    });
  });
}
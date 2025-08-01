import 'dart:convert';
import 'package:flutter_test/flutter_test.dart';
import 'package:guardianes_mobile/features/walking/data/models/step_submission_request_dto.dart';

void main() {
  group('StepSubmissionRequestDto', () {
    const tStepSubmissionRequestDto = StepSubmissionRequestDto(
      stepCount: 2500,
      timestamp: '2025-07-16T14:30:00',
    );

    test('should be a subclass of StepSubmissionRequestDto', () {
      expect(tStepSubmissionRequestDto, isA<StepSubmissionRequestDto>());
    });

    test('should serialize to JSON correctly', () {
      // Act
      final result = tStepSubmissionRequestDto.toJson();

      // Assert
      final expectedJson = {
        'stepCount': 2500,
        'timestamp': '2025-07-16T14:30:00',
      };
      expect(result, equals(expectedJson));
    });

    test('should deserialize from JSON correctly', () {
      // Arrange
      final jsonMap = {
        'stepCount': 2500,
        'timestamp': '2025-07-16T14:30:00',
      };

      // Act
      final result = StepSubmissionRequestDto.fromJson(jsonMap);

      // Assert
      expect(result, equals(tStepSubmissionRequestDto));
    });

    test('should handle toJson and fromJson round trip', () {
      // Act
      final json = tStepSubmissionRequestDto.toJson();
      final result = StepSubmissionRequestDto.fromJson(json);

      // Assert
      expect(result, equals(tStepSubmissionRequestDto));
    });

    test('should support equality comparison', () {
      // Arrange
      const dto1 = StepSubmissionRequestDto(
        stepCount: 2500,
        timestamp: '2025-07-16T14:30:00',
      );
      const dto2 = StepSubmissionRequestDto(
        stepCount: 2500,
        timestamp: '2025-07-16T14:30:00',
      );
      const dto3 = StepSubmissionRequestDto(
        stepCount: 3000,
        timestamp: '2025-07-16T14:30:00',
      );

      // Assert
      expect(dto1, equals(dto2));
      expect(dto1, isNot(equals(dto3)));
    });

    test('should handle JSON string conversion', () {
      // Arrange
      final jsonString = json.encode(tStepSubmissionRequestDto.toJson());

      // Act
      final jsonMap = json.decode(jsonString) as Map<String, dynamic>;
      final result = StepSubmissionRequestDto.fromJson(jsonMap);

      // Assert
      expect(result, equals(tStepSubmissionRequestDto));
    });

    test('should handle null values gracefully', () {
      // Arrange
      final jsonMap = {
        'stepCount': null,
        'timestamp': null,
      };

      // Act & Assert
      expect(
        () => StepSubmissionRequestDto.fromJson(jsonMap),
        throwsA(isA<TypeError>()),
      );
    });

    test('should handle missing fields gracefully', () {
      // Arrange
      final jsonMap = <String, dynamic>{};

      // Act & Assert
      expect(
        () => StepSubmissionRequestDto.fromJson(jsonMap),
        throwsA(isA<TypeError>()),
      );
    });
  });
}

import 'dart:convert';
import 'package:flutter_test/flutter_test.dart';
import 'package:guardianes_mobile/features/auth/data/models/guardian_model.dart';
import 'package:guardianes_mobile/features/auth/domain/entities/guardian.dart';

void main() {
  group('GuardianModel', () {
    final tGuardianModel = GuardianModel(
      id: 1,
      username: 'test_guardian',
      email: 'test@example.com',
      name: 'Test Guardian',
      birthDate: DateTime.parse('2015-01-01T00:00:00Z'),
      age: 9,
      level: 'BEGINNER',
      experiencePoints: 150,
      experienceToNextLevel: 350,
      totalSteps: 5000,
      totalEnergyGenerated: 500,
      createdAt: DateTime.parse('2025-01-01T00:00:00Z'),
      lastActiveAt: DateTime.parse('2025-07-17T09:00:00Z'),
      isChild: true,
    );

    test('should be a subclass of Guardian entity', () {
      expect(tGuardianModel, isA<Guardian>());
    });

    test('should return a valid model when fromJson is called with valid JSON',
        () {
      // arrange
      final Map<String, dynamic> jsonMap = {
        'id': 1,
        'username': 'test_guardian',
        'email': 'test@example.com',
        'name': 'Test Guardian',
        'birthDate': '2015-01-01T00:00:00.000Z',
        'age': 9,
        'level': 'BEGINNER',
        'experiencePoints': 150,
        'experienceToNextLevel': 350,
        'totalSteps': 5000,
        'totalEnergyGenerated': 500,
        'createdAt': '2025-01-01T00:00:00.000Z',
        'lastActiveAt': '2025-07-17T09:00:00.000Z',
        'isChild': true,
      };

      // act
      final result = GuardianModel.fromJson(jsonMap);

      // assert
      expect(result, equals(tGuardianModel));
    });

    test('should return a valid JSON map when toJson is called', () {
      // arrange
      final expectedMap = {
        'id': 1,
        'username': 'test_guardian',
        'email': 'test@example.com',
        'name': 'Test Guardian',
        'birthDate': '2015-01-01T00:00:00.000Z',
        'age': 9,
        'level': 'BEGINNER',
        'experiencePoints': 150,
        'experienceToNextLevel': 350,
        'totalSteps': 5000,
        'totalEnergyGenerated': 500,
        'createdAt': '2025-01-01T00:00:00.000Z',
        'lastActiveAt': '2025-07-17T09:00:00.000Z',
        'isChild': true,
      };

      // act
      final result = tGuardianModel.toJson();

      // assert
      expect(result, equals(expectedMap));
    });

    test('should handle JSON serialization and deserialization correctly', () {
      // arrange
      final jsonString = jsonEncode(tGuardianModel.toJson());

      // act
      final jsonMap = jsonDecode(jsonString) as Map<String, dynamic>;
      final guardianFromJson = GuardianModel.fromJson(jsonMap);

      // assert
      expect(guardianFromJson, equals(tGuardianModel));
    });

    test('should handle child guardian JSON correctly', () {
      // arrange
      final tChildGuardian = GuardianModel(
        id: 2,
        username: 'child_guardian',
        email: 'child@example.com',
        name: 'Child Guardian',
        birthDate: DateTime.parse('2018-01-01T00:00:00Z'),
        age: 6,
        level: 'BEGINNER',
        experiencePoints: 0,
        experienceToNextLevel: 500,
        totalSteps: 0,
        totalEnergyGenerated: 0,
        createdAt: DateTime.parse('2025-01-01T00:00:00Z'),
        lastActiveAt: DateTime.parse('2025-07-17T09:00:00Z'),
        isChild: true,
      );

      final jsonMap = {
        'id': 2,
        'username': 'child_guardian',
        'email': 'child@example.com',
        'name': 'Child Guardian',
        'birthDate': '2018-01-01T00:00:00.000Z',
        'age': 6,
        'level': 'BEGINNER',
        'experiencePoints': 0,
        'experienceToNextLevel': 500,
        'totalSteps': 0,
        'totalEnergyGenerated': 0,
        'createdAt': '2025-01-01T00:00:00.000Z',
        'lastActiveAt': '2025-07-17T09:00:00.000Z',
        'isChild': true,
      };

      // act
      final result = GuardianModel.fromJson(jsonMap);

      // assert
      expect(result, equals(tChildGuardian));
      expect(result.isChild, isTrue);
      expect(result.age, equals(6));
    });

    test('should handle adult guardian JSON correctly', () {
      // arrange
      final tAdultGuardian = GuardianModel(
        id: 3,
        username: 'adult_guardian',
        email: 'adult@example.com',
        name: 'Adult Guardian',
        birthDate: DateTime.parse('1985-01-01T00:00:00Z'),
        age: 40,
        level: 'MENTOR',
        experiencePoints: 1000,
        experienceToNextLevel: 0,
        totalSteps: 50000,
        totalEnergyGenerated: 5000,
        createdAt: DateTime.parse('2025-01-01T00:00:00Z'),
        lastActiveAt: DateTime.parse('2025-07-17T09:00:00Z'),
        isChild: false,
      );

      final jsonMap = {
        'id': 3,
        'username': 'adult_guardian',
        'email': 'adult@example.com',
        'name': 'Adult Guardian',
        'birthDate': '1985-01-01T00:00:00.000Z',
        'age': 40,
        'level': 'MENTOR',
        'experiencePoints': 1000,
        'experienceToNextLevel': 0,
        'totalSteps': 50000,
        'totalEnergyGenerated': 5000,
        'createdAt': '2025-01-01T00:00:00.000Z',
        'lastActiveAt': '2025-07-17T09:00:00.000Z',
        'isChild': false,
      };

      // act
      final result = GuardianModel.fromJson(jsonMap);

      // assert
      expect(result, equals(tAdultGuardian));
      expect(result.isChild, isFalse);
      expect(result.level, equals('MENTOR'));
    });

    test('should throw exception when fromJson is called with invalid JSON',
        () {
      // arrange
      final invalidJsonMap = {
        'id': 'invalid_id', // Should be int
        'username': 'test_guardian',
        // Missing required fields
      };

      // act & assert
      expect(
        () => GuardianModel.fromJson(invalidJsonMap),
        throwsA(isA<TypeError>()),
      );
    });

    test('should handle date parsing correctly', () {
      // arrange
      final jsonWithDifferentDateFormat = {
        'id': 1,
        'username': 'test_guardian',
        'email': 'test@example.com',
        'name': 'Test Guardian',
        'birthDate': '2015-01-01T00:00:00.000Z',
        'age': 9,
        'level': 'BEGINNER',
        'experiencePoints': 150,
        'experienceToNextLevel': 350,
        'totalSteps': 5000,
        'totalEnergyGenerated': 500,
        'createdAt': '2025-01-01T00:00:00.000Z',
        'lastActiveAt': '2025-07-17T09:00:00.000Z',
        'isChild': true,
      };

      // act
      final result = GuardianModel.fromJson(jsonWithDifferentDateFormat);

      // assert
      expect(result.birthDate, isA<DateTime>());
      expect(result.createdAt, isA<DateTime>());
      expect(result.lastActiveAt, isA<DateTime>());
    });
  });
}

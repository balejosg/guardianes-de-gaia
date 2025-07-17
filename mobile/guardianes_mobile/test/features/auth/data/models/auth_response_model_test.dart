import 'dart:convert';
import 'package:flutter_test/flutter_test.dart';
import 'package:guardianes_mobile/features/auth/data/models/auth_response_model.dart';
import 'package:guardianes_mobile/features/auth/data/models/guardian_model.dart';
import 'package:guardianes_mobile/features/auth/domain/entities/auth_result.dart';

void main() {
  group('AuthResponseModel', () {
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

    const tToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token';

    final tAuthResponseModel = AuthResponseModel(
      token: tToken,
      guardianModel: tGuardianModel,
    );

    test('should be a subclass of AuthResult entity', () {
      expect(tAuthResponseModel, isA<AuthResult>());
    });

    test('should return a valid model when fromJson is called with valid JSON', () {
      // arrange
      final Map<String, dynamic> jsonMap = {
        'token': tToken,
        'guardian': {
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
        },
      };

      // act
      final result = AuthResponseModel.fromJson(jsonMap);

      // assert
      expect(result, equals(tAuthResponseModel));
      expect(result.token, equals(tToken));
      expect(result.guardian, equals(tGuardianModel));
      expect(result.guardianModel, equals(tGuardianModel));
    });

    test('should return a valid JSON map when toJson is called', () {
      // arrange
      final expectedMap = {
        'token': tToken,
        'guardian': {
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
        },
      };

      // act
      final result = tAuthResponseModel.toJson();

      // assert
      expect(result, equals(expectedMap));
    });

    test('should handle JSON serialization and deserialization correctly', () {
      // arrange
      final jsonString = jsonEncode(tAuthResponseModel.toJson());

      // act
      final jsonMap = jsonDecode(jsonString) as Map<String, dynamic>;
      final authResponseFromJson = AuthResponseModel.fromJson(jsonMap);

      // assert
      expect(authResponseFromJson, equals(tAuthResponseModel));
      expect(authResponseFromJson.token, equals(tToken));
      expect(authResponseFromJson.guardian, equals(tGuardianModel));
    });

    test('should properly map guardian field to guardianModel', () {
      // arrange
      final jsonMap = {
        'token': tToken,
        'guardian': {
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
        },
      };

      // act
      final result = AuthResponseModel.fromJson(jsonMap);

      // assert
      expect(result.guardianModel, isA<GuardianModel>());
      expect(result.guardian, equals(result.guardianModel));
      expect(result.guardianModel.id, equals(1));
      expect(result.guardianModel.username, equals('test_guardian'));
    });

    test('should handle login response JSON correctly', () {
      // arrange - Simulating actual backend response
      final loginResponseJson = {
        'token': 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.login.response',
        'guardian': {
          'id': 2,
          'username': 'login_test',
          'email': 'login@example.com',
          'name': 'Login Test',
          'birthDate': '2016-05-15T00:00:00.000Z',
          'age': 8,
          'level': 'BEGINNER',
          'experiencePoints': 75,
          'experienceToNextLevel': 425,
          'totalSteps': 2500,
          'totalEnergyGenerated': 250,
          'createdAt': '2025-01-15T00:00:00.000Z',
          'lastActiveAt': '2025-07-17T10:00:00.000Z',
          'isChild': true,
        },
      };

      // act
      final result = AuthResponseModel.fromJson(loginResponseJson);

      // assert
      expect(result.token, contains('login.response'));
      expect(result.guardian.username, equals('login_test'));
      expect(result.guardian.isChild, isTrue);
    });

    test('should handle registration response JSON correctly', () {
      // arrange - Simulating actual backend response
      final registrationResponseJson = {
        'token': 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.register.response',
        'guardian': {
          'id': 3,
          'username': 'new_guardian',
          'email': 'new@example.com',
          'name': 'New Guardian',
          'birthDate': '2017-03-20T00:00:00.000Z',
          'age': 7,
          'level': 'BEGINNER',
          'experiencePoints': 0,
          'experienceToNextLevel': 500,
          'totalSteps': 0,
          'totalEnergyGenerated': 0,
          'createdAt': '2025-07-17T09:00:00.000Z',
          'lastActiveAt': '2025-07-17T09:00:00.000Z',
          'isChild': true,
        },
      };

      // act
      final result = AuthResponseModel.fromJson(registrationResponseJson);

      // assert
      expect(result.token, contains('register.response'));
      expect(result.guardian.username, equals('new_guardian'));
      expect(result.guardian.experiencePoints, equals(0));
      expect(result.guardian.totalSteps, equals(0));
    });

    test('should throw exception when fromJson is called with invalid JSON', () {
      // arrange
      final invalidJsonMap = {
        'token': 123, // Should be string
        'guardian': 'invalid_guardian', // Should be object
      };

      // act & assert
      expect(
        () => AuthResponseModel.fromJson(invalidJsonMap),
        throwsA(isA<TypeError>()),
      );
    });

    test('should throw exception when guardian field is missing', () {
      // arrange
      final jsonMapWithoutGuardian = {
        'token': tToken,
        // Missing guardian field
      };

      // act & assert
      expect(
        () => AuthResponseModel.fromJson(jsonMapWithoutGuardian),
        throwsA(isA<TypeError>()),
      );
    });

    test('should throw exception when token field is missing', () {
      // arrange
      final jsonMapWithoutToken = {
        // Missing token field
        'guardian': {
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
        },
      };

      // act & assert
      expect(
        () => AuthResponseModel.fromJson(jsonMapWithoutToken),
        throwsA(isA<TypeError>()),
      );
    });
  });
}
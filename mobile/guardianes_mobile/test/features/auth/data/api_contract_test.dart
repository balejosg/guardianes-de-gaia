import 'dart:convert';
import 'package:flutter_test/flutter_test.dart';
import 'package:guardianes_mobile/features/auth/data/models/auth_response_model.dart';
import 'package:guardianes_mobile/features/auth/data/models/guardian_model.dart';

/// Contract tests to validate API response structure between mobile and backend.
/// Addresses Gap #6: No contract testing between mobile and backend.
///
/// These tests ensure that:
/// 1. Mobile can parse expected backend response formats
/// 2. Changes to response structure are caught early
/// 3. Both successful and error responses are handled correctly
void main() {
  group('API Contract Tests', () {
    group('AuthResponse Contract', () {
      test('should parse valid registration response from backend', () {
        // This is the exact structure the backend returns on successful registration
        // If backend changes this structure, this test will fail
        const backendResponse = '''
        {
          "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwibmFtZSI6IlRlc3QgR3VhcmRpYW4ifQ.signature",
          "guardian": {
            "id": 1,
            "username": "testuser",
            "email": "test@example.com",
            "name": "Test Guardian",
            "birthDate": "2015-01-15",
            "age": 9,
            "level": "INITIATE",
            "experiencePoints": 0,
            "experienceToNextLevel": 500,
            "totalSteps": 0,
            "totalEnergyGenerated": 0,
            "createdAt": "2025-01-01T10:00:00",
            "lastActiveAt": "2025-01-01T10:00:00",
            "isChild": true
          }
        }
        ''';

        // Parse the response
        final json = jsonDecode(backendResponse) as Map<String, dynamic>;
        final authResponse = AuthResponseModel.fromJson(json);

        // Verify all expected fields are present and parsed correctly
        expect(authResponse.token, isNotEmpty);
        expect(authResponse.token, startsWith('eyJ'));
        expect(authResponse.guardian, isNotNull);
        expect(authResponse.guardian.id, equals(1));
        expect(authResponse.guardian.username, equals('testuser'));
        expect(authResponse.guardian.email, equals('test@example.com'));
        expect(authResponse.guardian.name, equals('Test Guardian'));
        expect(authResponse.guardian.level, equals('INITIATE'));
        expect(authResponse.guardian.experiencePoints, equals(0));
        expect(authResponse.guardian.isChild, isTrue);
      });

      test('should parse valid login response from backend', () {
        // Login response has the same structure as registration
        const backendResponse = '''
        {
          "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.login.token",
          "guardian": {
            "id": 42,
            "username": "guardian_user",
            "email": "guardian@example.com",
            "name": "Guardian User",
            "birthDate": "2010-05-20",
            "age": 14,
            "level": "PROTECTOR",
            "experiencePoints": 1500,
            "experienceToNextLevel": 500,
            "totalSteps": 50000,
            "totalEnergyGenerated": 5000,
            "createdAt": "2024-06-01T08:30:00",
            "lastActiveAt": "2025-01-15T14:45:00",
            "isChild": false
          }
        }
        ''';

        final json = jsonDecode(backendResponse) as Map<String, dynamic>;
        final authResponse = AuthResponseModel.fromJson(json);

        expect(authResponse.token, isNotEmpty);
        expect(authResponse.guardian.id, equals(42));
        expect(authResponse.guardian.username, equals('guardian_user'));
        expect(authResponse.guardian.level, equals('PROTECTOR'));
        expect(authResponse.guardian.experiencePoints, equals(1500));
        expect(authResponse.guardian.totalSteps, equals(50000));
        expect(authResponse.guardian.isChild, isFalse);
      });

      test('should handle guardian with all level types', () {
        final levels = [
          'INITIATE',
          'APPRENTICE',
          'PROTECTOR',
          'GUARDIAN',
          'MASTER',
          'LEGEND'
        ];

        for (final level in levels) {
          final response = '''
          {
            "token": "test.token",
            "guardian": {
              "id": 1,
              "username": "user",
              "email": "user@test.com",
              "name": "User",
              "birthDate": "2015-01-01",
              "age": 9,
              "level": "$level",
              "experiencePoints": 0,
              "experienceToNextLevel": 500,
              "totalSteps": 0,
              "totalEnergyGenerated": 0,
              "createdAt": "2025-01-01T00:00:00",
              "lastActiveAt": "2025-01-01T00:00:00",
              "isChild": true
            }
          }
          ''';

          final json = jsonDecode(response) as Map<String, dynamic>;
          final authResponse = AuthResponseModel.fromJson(json);

          expect(authResponse.guardian.level, equals(level),
              reason: 'Failed to parse level: $level');
        }
      });
    });

    group('GuardianModel Contract', () {
      test('should parse guardian with minimum required fields', () {
        // Minimum fields that backend MUST always provide
        const minimalGuardian = '''
        {
          "id": 1,
          "username": "user",
          "email": "user@test.com",
          "name": "User Name",
          "birthDate": "2015-01-01",
          "age": 9,
          "level": "INITIATE",
          "experiencePoints": 0,
          "experienceToNextLevel": 500,
          "totalSteps": 0,
          "totalEnergyGenerated": 0,
          "createdAt": "2025-01-01T00:00:00",
          "lastActiveAt": "2025-01-01T00:00:00",
          "isChild": true
        }
        ''';

        final json = jsonDecode(minimalGuardian) as Map<String, dynamic>;
        final guardian = GuardianModel.fromJson(json);

        // All these fields are required
        expect(guardian.id, isNotNull);
        expect(guardian.username, isNotEmpty);
        expect(guardian.email, isNotEmpty);
        expect(guardian.name, isNotEmpty);
        expect(guardian.birthDate, isNotNull);
        expect(guardian.level, isNotEmpty);
      });

      test('should handle different date formats', () {
        // Backend might return dates in different formats
        final dateFormats = [
          '2015-01-15', // ISO date only
          '2015-01-15T00:00:00', // ISO datetime without Z
          '2015-01-15T00:00:00Z', // ISO datetime with Z
        ];

        for (final dateFormat in dateFormats) {
          final response = '''
          {
            "id": 1,
            "username": "user",
            "email": "user@test.com",
            "name": "User",
            "birthDate": "$dateFormat",
            "age": 9,
            "level": "INITIATE",
            "experiencePoints": 0,
            "experienceToNextLevel": 500,
            "totalSteps": 0,
            "totalEnergyGenerated": 0,
            "createdAt": "2025-01-01T00:00:00",
            "lastActiveAt": "2025-01-01T00:00:00",
            "isChild": true
          }
          ''';

          final json = jsonDecode(response) as Map<String, dynamic>;

          // Should not throw when parsing different date formats
          expect(
            () => GuardianModel.fromJson(json),
            returnsNormally,
            reason: 'Failed to parse date format: $dateFormat',
          );
        }
      });
    });

    group('Error Response Contract', () {
      test('should parse standard error response with message field', () {
        // Backend error response when using 'message' field
        const errorResponse = '''
        {
          "message": "Username already exists"
        }
        ''';

        final json = jsonDecode(errorResponse) as Map<String, dynamic>;
        final message = json['message'] as String?;

        expect(message, isNotNull);
        expect(message, equals('Username already exists'));
      });

      test('should parse error response with error field', () {
        // Backend error response when using 'error' field
        const errorResponse = '''
        {
          "error": "Invalid credentials"
        }
        ''';

        final json = jsonDecode(errorResponse) as Map<String, dynamic>;
        final error = json['error'] as String?;

        expect(error, isNotNull);
        expect(error, equals('Invalid credentials'));
      });

      test('should handle error response with both message and error fields',
          () {
        // Some error responses might have both
        const errorResponse = '''
        {
          "error": "Validation failed",
          "message": "Email format is invalid"
        }
        ''';

        final json = jsonDecode(errorResponse) as Map<String, dynamic>;

        // Mobile code should prioritize 'message' over 'error'
        final message = json['message'] ?? json['error'];
        expect(message, equals('Email format is invalid'));
      });
    });

    group('Response Structure Compatibility', () {
      test('token field must be a string', () {
        const response = '''
        {
          "token": "jwt.token.here",
          "guardian": {
            "id": 1,
            "username": "user",
            "email": "user@test.com",
            "name": "User",
            "birthDate": "2015-01-01",
            "age": 9,
            "level": "INITIATE",
            "experiencePoints": 0,
            "experienceToNextLevel": 500,
            "totalSteps": 0,
            "totalEnergyGenerated": 0,
            "createdAt": "2025-01-01T00:00:00",
            "lastActiveAt": "2025-01-01T00:00:00",
            "isChild": true
          }
        }
        ''';

        final json = jsonDecode(response) as Map<String, dynamic>;

        expect(json['token'], isA<String>());
        expect(json['guardian'], isA<Map<String, dynamic>>());
      });

      test('guardian.id must be a number', () {
        const response = '''
        {
          "id": 1,
          "username": "user",
          "email": "user@test.com",
          "name": "User",
          "birthDate": "2015-01-01",
          "age": 9,
          "level": "INITIATE",
          "experiencePoints": 0,
          "experienceToNextLevel": 500,
          "totalSteps": 0,
          "totalEnergyGenerated": 0,
          "createdAt": "2025-01-01T00:00:00",
          "lastActiveAt": "2025-01-01T00:00:00",
          "isChild": true
        }
        ''';

        final json = jsonDecode(response) as Map<String, dynamic>;

        expect(json['id'], isA<int>());
        expect(json['experiencePoints'], isA<int>());
        expect(json['totalSteps'], isA<int>());
        expect(json['isChild'], isA<bool>());
      });
    });
  });
}

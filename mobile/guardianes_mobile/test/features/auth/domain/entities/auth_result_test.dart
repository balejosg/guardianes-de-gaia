import 'package:flutter_test/flutter_test.dart';
import 'package:equatable/equatable.dart';
import 'package:guardianes_mobile/features/auth/domain/entities/auth_result.dart';
import 'package:guardianes_mobile/features/auth/domain/entities/guardian.dart';

void main() {
  group('AuthResult Entity', () {
    final tGuardian = Guardian(
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

    final tAuthResult = AuthResult(
      token: tToken,
      guardian: tGuardian,
    );

    test('should be a subclass of Equatable', () {
      expect(tAuthResult, isA<Equatable>());
    });

    test('should have all required properties', () {
      expect(tAuthResult.token, equals(tToken));
      expect(tAuthResult.guardian, equals(tGuardian));
    });

    test('should support value equality', () {
      final tAuthResult2 = AuthResult(
        token: tToken,
        guardian: tGuardian,
      );

      expect(tAuthResult, equals(tAuthResult2));
    });

    test('should not be equal when token differs', () {
      final tDifferentTokenResult = AuthResult(
        token: 'different.jwt.token',
        guardian: tGuardian,
      );

      expect(tAuthResult, isNot(equals(tDifferentTokenResult)));
    });

    test('should not be equal when guardian differs', () {
      final tDifferentGuardian = Guardian(
        id: 2, // Different guardian
        username: 'different_guardian',
        email: 'different@example.com',
        name: 'Different Guardian',
        birthDate: DateTime.parse('2016-01-01T00:00:00Z'),
        age: 8,
        level: 'BEGINNER',
        experiencePoints: 100,
        experienceToNextLevel: 400,
        totalSteps: 3000,
        totalEnergyGenerated: 300,
        createdAt: DateTime.parse('2025-01-01T00:00:00Z'),
        lastActiveAt: DateTime.parse('2025-07-17T09:00:00Z'),
        isChild: true,
      );

      final tDifferentGuardianResult = AuthResult(
        token: tToken,
        guardian: tDifferentGuardian,
      );

      expect(tAuthResult, isNot(equals(tDifferentGuardianResult)));
    });

    test('should have correct hashCode', () {
      final tAuthResult2 = AuthResult(
        token: tToken,
        guardian: tGuardian,
      );

      expect(tAuthResult.hashCode, equals(tAuthResult2.hashCode));
    });

    test('should handle props correctly', () {
      expect(tAuthResult.props, equals([tToken, tGuardian]));
    });
  });
}
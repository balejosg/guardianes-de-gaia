import 'package:flutter_test/flutter_test.dart';
import 'package:equatable/equatable.dart';
import 'package:guardianes_mobile/features/auth/domain/entities/guardian.dart';

void main() {
  group('Guardian Entity', () {
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

    test('should be a subclass of Equatable', () {
      expect(tGuardian, isA<Equatable>());
    });

    test('should have all required properties', () {
      expect(tGuardian.id, equals(1));
      expect(tGuardian.username, equals('test_guardian'));
      expect(tGuardian.email, equals('test@example.com'));
      expect(tGuardian.name, equals('Test Guardian'));
      expect(
          tGuardian.birthDate, equals(DateTime.parse('2015-01-01T00:00:00Z')));
      expect(tGuardian.age, equals(9));
      expect(tGuardian.level, equals('BEGINNER'));
      expect(tGuardian.experiencePoints, equals(150));
      expect(tGuardian.experienceToNextLevel, equals(350));
      expect(tGuardian.totalSteps, equals(5000));
      expect(tGuardian.totalEnergyGenerated, equals(500));
      expect(
          tGuardian.createdAt, equals(DateTime.parse('2025-01-01T00:00:00Z')));
      expect(tGuardian.lastActiveAt,
          equals(DateTime.parse('2025-07-17T09:00:00Z')));
      expect(tGuardian.isChild, equals(true));
    });

    test('should support value equality', () {
      final tGuardian2 = Guardian(
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

      expect(tGuardian, equals(tGuardian2));
    });

    test('should not be equal when properties differ', () {
      final tDifferentGuardian = Guardian(
        id: 2, // Different ID
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

      expect(tGuardian, isNot(equals(tDifferentGuardian)));
    });

    test('should have correct hashCode', () {
      final tGuardian2 = Guardian(
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

      expect(tGuardian.hashCode, equals(tGuardian2.hashCode));
    });

    test('should handle edge cases for age validation', () {
      final tChildGuardian = Guardian(
        id: 1,
        username: 'child_guardian',
        email: 'child@example.com',
        name: 'Child Guardian',
        birthDate: DateTime.parse('2018-01-01T00:00:00Z'),
        age: 6, // Minimum age for target audience
        level: 'BEGINNER',
        experiencePoints: 0,
        experienceToNextLevel: 500,
        totalSteps: 0,
        totalEnergyGenerated: 0,
        createdAt: DateTime.parse('2025-01-01T00:00:00Z'),
        lastActiveAt: DateTime.parse('2025-07-17T09:00:00Z'),
        isChild: true,
      );

      expect(tChildGuardian.age, equals(6));
      expect(tChildGuardian.isChild, isTrue);
    });

    test('should handle adult guardian (Guía del Pacto)', () {
      final tAdultGuardian = Guardian(
        id: 2,
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

      expect(tAdultGuardian.isChild, isFalse);
      expect(tAdultGuardian.level, equals('MENTOR'));
    });
  });
}

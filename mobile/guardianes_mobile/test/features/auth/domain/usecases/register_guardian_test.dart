import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:mockito/annotations.dart';
import 'package:guardianes_mobile/features/auth/domain/entities/auth_result.dart';
import 'package:guardianes_mobile/features/auth/domain/entities/guardian.dart';
import 'package:guardianes_mobile/features/auth/domain/repositories/auth_repository.dart';
import 'package:guardianes_mobile/features/auth/domain/usecases/register_guardian.dart';

import 'register_guardian_test.mocks.dart';

@GenerateMocks([AuthRepository])
void main() {
  late RegisterGuardian usecase;
  late MockAuthRepository mockAuthRepository;

  setUp(() {
    mockAuthRepository = MockAuthRepository();
    usecase = RegisterGuardian(mockAuthRepository);
  });

  group('RegisterGuardian UseCase', () {
    const tUsername = 'new_guardian';
    const tEmail = 'new@example.com';
    const tPassword = 'secure_password';
    const tName = 'New Guardian';
    final tBirthDate = DateTime.parse('2015-01-01T00:00:00Z');

    final tGuardian = Guardian(
      id: 1,
      username: tUsername,
      email: tEmail,
      name: tName,
      birthDate: tBirthDate,
      age: 9,
      level: 'BEGINNER',
      experiencePoints: 0,
      experienceToNextLevel: 500,
      totalSteps: 0,
      totalEnergyGenerated: 0,
      createdAt: DateTime.parse('2025-01-01T00:00:00Z'),
      lastActiveAt: DateTime.parse('2025-07-17T09:00:00Z'),
      isChild: true,
    );

    const tToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.new.token';

    final tAuthResult = AuthResult(
      token: tToken,
      guardian: tGuardian,
    );

    test('should return AuthResult when registration is successful', () async {
      // arrange
      when(mockAuthRepository.register(
        username: anyNamed('username'),
        email: anyNamed('email'),
        password: anyNamed('password'),
        name: anyNamed('name'),
        birthDate: anyNamed('birthDate'),
      )).thenAnswer((_) async => tAuthResult);

      // act
      final result = await usecase(
        username: tUsername,
        email: tEmail,
        password: tPassword,
        name: tName,
        birthDate: tBirthDate,
      );

      // assert
      expect(result, equals(tAuthResult));
      verify(mockAuthRepository.register(
        username: tUsername,
        email: tEmail,
        password: tPassword,
        name: tName,
        birthDate: tBirthDate,
      ));
      verifyNoMoreInteractions(mockAuthRepository);
    });

    test('should forward exception when registration fails', () async {
      // arrange
      const tException = 'Username already exists';
      when(mockAuthRepository.register(
        username: anyNamed('username'),
        email: anyNamed('email'),
        password: anyNamed('password'),
        name: anyNamed('name'),
        birthDate: anyNamed('birthDate'),
      )).thenThrow(Exception(tException));

      // act & assert
      expect(
        () async => await usecase(
          username: tUsername,
          email: tEmail,
          password: tPassword,
          name: tName,
          birthDate: tBirthDate,
        ),
        throwsA(isA<Exception>()),
      );

      verify(mockAuthRepository.register(
        username: tUsername,
        email: tEmail,
        password: tPassword,
        name: tName,
        birthDate: tBirthDate,
      ));
      verifyNoMoreInteractions(mockAuthRepository);
    });

    test('should pass correct parameters to repository', () async {
      // arrange
      when(mockAuthRepository.register(
        username: anyNamed('username'),
        email: anyNamed('email'),
        password: anyNamed('password'),
        name: anyNamed('name'),
        birthDate: anyNamed('birthDate'),
      )).thenAnswer((_) async => tAuthResult);

      // act
      await usecase(
        username: tUsername,
        email: tEmail,
        password: tPassword,
        name: tName,
        birthDate: tBirthDate,
      );

      // assert
      verify(mockAuthRepository.register(
        username: tUsername,
        email: tEmail,
        password: tPassword,
        name: tName,
        birthDate: tBirthDate,
      ));
    });

    test('should handle child registration correctly', () async {
      // arrange
      final tChildBirthDate =
          DateTime.parse('2018-01-01T00:00:00Z'); // 6 years old
      final tChildGuardian = Guardian(
        id: 2,
        username: 'child_guardian',
        email: 'child@example.com',
        name: 'Child Guardian',
        birthDate: tChildBirthDate,
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

      final tChildAuthResult = AuthResult(
        token: 'child.jwt.token',
        guardian: tChildGuardian,
      );

      when(mockAuthRepository.register(
        username: anyNamed('username'),
        email: anyNamed('email'),
        password: anyNamed('password'),
        name: anyNamed('name'),
        birthDate: anyNamed('birthDate'),
      )).thenAnswer((_) async => tChildAuthResult);

      // act
      final result = await usecase(
        username: 'child_guardian',
        email: 'child@example.com',
        password: 'child_password',
        name: 'Child Guardian',
        birthDate: tChildBirthDate,
      );

      // assert
      expect(result, equals(tChildAuthResult));
      expect(result.guardian.isChild, isTrue);
      expect(result.guardian.age, equals(6));
    });

    test('should handle empty or invalid data correctly', () async {
      // arrange
      const tException = 'Validation failed';
      when(mockAuthRepository.register(
        username: anyNamed('username'),
        email: anyNamed('email'),
        password: anyNamed('password'),
        name: anyNamed('name'),
        birthDate: anyNamed('birthDate'),
      )).thenThrow(Exception(tException));

      // act & assert
      expect(
        () async => await usecase(
          username: '',
          email: 'invalid-email',
          password: '123',
          name: '',
          birthDate: DateTime.now(), // Future date
        ),
        throwsA(isA<Exception>()),
      );
    });
  });
}

import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:mockito/annotations.dart';
import 'package:guardianes_mobile/features/auth/domain/entities/auth_result.dart';
import 'package:guardianes_mobile/features/auth/domain/entities/guardian.dart';
import 'package:guardianes_mobile/features/auth/domain/repositories/auth_repository.dart';
import 'package:guardianes_mobile/features/auth/domain/usecases/login_guardian.dart';

import 'login_guardian_test.mocks.dart';

@GenerateMocks([AuthRepository])
void main() {
  late LoginGuardian usecase;
  late MockAuthRepository mockAuthRepository;

  setUp(() {
    mockAuthRepository = MockAuthRepository();
    usecase = LoginGuardian(mockAuthRepository);
  });

  group('LoginGuardian UseCase', () {
    const tUsernameOrEmail = 'test_guardian';
    const tPassword = 'test_password';

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

    test('should return AuthResult when login is successful', () async {
      // arrange
      when(mockAuthRepository.login(
        usernameOrEmail: anyNamed('usernameOrEmail'),
        password: anyNamed('password'),
      )).thenAnswer((_) async => tAuthResult);

      // act
      final result = await usecase(
        usernameOrEmail: tUsernameOrEmail,
        password: tPassword,
      );

      // assert
      expect(result, equals(tAuthResult));
      verify(mockAuthRepository.login(
        usernameOrEmail: tUsernameOrEmail,
        password: tPassword,
      ));
      verifyNoMoreInteractions(mockAuthRepository);
    });

    test('should forward exception when login fails', () async {
      // arrange
      const tException = 'Invalid credentials';
      when(mockAuthRepository.login(
        usernameOrEmail: anyNamed('usernameOrEmail'),
        password: anyNamed('password'),
      )).thenThrow(Exception(tException));

      // act & assert
      expect(
        () async => await usecase(
          usernameOrEmail: tUsernameOrEmail,
          password: tPassword,
        ),
        throwsA(isA<Exception>()),
      );

      verify(mockAuthRepository.login(
        usernameOrEmail: tUsernameOrEmail,
        password: tPassword,
      ));
      verifyNoMoreInteractions(mockAuthRepository);
    });

    test('should pass correct parameters to repository', () async {
      // arrange
      when(mockAuthRepository.login(
        usernameOrEmail: anyNamed('usernameOrEmail'),
        password: anyNamed('password'),
      )).thenAnswer((_) async => tAuthResult);

      // act
      await usecase(
        usernameOrEmail: tUsernameOrEmail,
        password: tPassword,
      );

      // assert
      verify(mockAuthRepository.login(
        usernameOrEmail: tUsernameOrEmail,
        password: tPassword,
      ));
    });

    test('should work with email as usernameOrEmail', () async {
      // arrange
      const tEmail = 'test@example.com';
      when(mockAuthRepository.login(
        usernameOrEmail: anyNamed('usernameOrEmail'),
        password: anyNamed('password'),
      )).thenAnswer((_) async => tAuthResult);

      // act
      final result = await usecase(
        usernameOrEmail: tEmail,
        password: tPassword,
      );

      // assert
      expect(result, equals(tAuthResult));
      verify(mockAuthRepository.login(
        usernameOrEmail: tEmail,
        password: tPassword,
      ));
    });
  });
}

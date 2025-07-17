import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:mockito/annotations.dart';
import 'package:guardianes_mobile/features/auth/data/datasources/auth_local_datasource.dart';
import 'package:guardianes_mobile/features/auth/data/datasources/auth_remote_datasource.dart';
import 'package:guardianes_mobile/features/auth/data/models/auth_response_model.dart';
import 'package:guardianes_mobile/features/auth/data/models/guardian_model.dart';
import 'package:guardianes_mobile/features/auth/data/repositories/auth_repository_impl.dart';
import 'package:guardianes_mobile/features/auth/domain/repositories/auth_repository.dart';

import 'auth_repository_impl_test.mocks.dart';

@GenerateMocks([
  AuthRemoteDataSource,
  AuthLocalDataSource,
])
void main() {
  late AuthRepositoryImpl repository;
  late MockAuthRemoteDataSource mockRemoteDataSource;
  late MockAuthLocalDataSource mockLocalDataSource;

  setUp(() {
    mockRemoteDataSource = MockAuthRemoteDataSource();
    mockLocalDataSource = MockAuthLocalDataSource();
    repository = AuthRepositoryImpl(
      remoteDataSource: mockRemoteDataSource,
      localDataSource: mockLocalDataSource,
    );
  });

  group('AuthRepositoryImpl', () {
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

    test('should be a subclass of AuthRepository', () {
      expect(repository, isA<AuthRepository>());
    });

    group('register', () {
      const tUsername = 'new_guardian';
      const tEmail = 'new@example.com';
      const tPassword = 'secure_password';
      const tName = 'New Guardian';
      final tBirthDate = DateTime.parse('2015-01-01T00:00:00Z');

      test('should return AuthResult when registration is successful',
          () async {
        // arrange
        when(mockRemoteDataSource.register(
          username: anyNamed('username'),
          email: anyNamed('email'),
          password: anyNamed('password'),
          name: anyNamed('name'),
          birthDate: anyNamed('birthDate'),
        )).thenAnswer((_) async => tAuthResponseModel);

        when(mockLocalDataSource.saveToken(any)).thenAnswer((_) async => {});
        when(mockLocalDataSource.saveGuardian(any)).thenAnswer((_) async => {});

        // act
        final result = await repository.register(
          username: tUsername,
          email: tEmail,
          password: tPassword,
          name: tName,
          birthDate: tBirthDate,
        );

        // assert
        expect(result, equals(tAuthResponseModel));
        verify(mockRemoteDataSource.register(
          username: tUsername,
          email: tEmail,
          password: tPassword,
          name: tName,
          birthDate: tBirthDate,
        ));
        verify(mockLocalDataSource.saveToken(tToken));
        verify(mockLocalDataSource.saveGuardian(tGuardianModel));
      });

      test(
          'should save token and guardian locally after successful registration',
          () async {
        // arrange
        when(mockRemoteDataSource.register(
          username: anyNamed('username'),
          email: anyNamed('email'),
          password: anyNamed('password'),
          name: anyNamed('name'),
          birthDate: anyNamed('birthDate'),
        )).thenAnswer((_) async => tAuthResponseModel);

        when(mockLocalDataSource.saveToken(any)).thenAnswer((_) async => {});
        when(mockLocalDataSource.saveGuardian(any)).thenAnswer((_) async => {});

        // act
        await repository.register(
          username: tUsername,
          email: tEmail,
          password: tPassword,
          name: tName,
          birthDate: tBirthDate,
        );

        // assert
        verify(mockLocalDataSource.saveToken(tToken));
        verify(mockLocalDataSource.saveGuardian(tGuardianModel));
      });

      test('should throw exception when remote registration fails', () async {
        // arrange
        when(mockRemoteDataSource.register(
          username: anyNamed('username'),
          email: anyNamed('email'),
          password: anyNamed('password'),
          name: anyNamed('name'),
          birthDate: anyNamed('birthDate'),
        )).thenThrow(Exception('Registration failed'));

        // act & assert
        expect(
          () async => await repository.register(
            username: tUsername,
            email: tEmail,
            password: tPassword,
            name: tName,
            birthDate: tBirthDate,
          ),
          throwsA(isA<Exception>()),
        );

        // Verify that local storage methods are not called when remote fails
        verifyNever(mockLocalDataSource.saveToken(any));
        verifyNever(mockLocalDataSource.saveGuardian(any));
      });
    });

    group('login', () {
      const tUsernameOrEmail = 'test_guardian';
      const tPassword = 'test_password';

      test('should return AuthResult when login is successful', () async {
        // arrange
        when(mockRemoteDataSource.login(
          usernameOrEmail: anyNamed('usernameOrEmail'),
          password: anyNamed('password'),
        )).thenAnswer((_) async => tAuthResponseModel);

        when(mockLocalDataSource.saveToken(any)).thenAnswer((_) async => {});
        when(mockLocalDataSource.saveGuardian(any)).thenAnswer((_) async => {});

        // act
        final result = await repository.login(
          usernameOrEmail: tUsernameOrEmail,
          password: tPassword,
        );

        // assert
        expect(result, equals(tAuthResponseModel));
        verify(mockRemoteDataSource.login(
          usernameOrEmail: tUsernameOrEmail,
          password: tPassword,
        ));
        verify(mockLocalDataSource.saveToken(tToken));
        verify(mockLocalDataSource.saveGuardian(tGuardianModel));
      });

      test('should save token and guardian locally after successful login',
          () async {
        // arrange
        when(mockRemoteDataSource.login(
          usernameOrEmail: anyNamed('usernameOrEmail'),
          password: anyNamed('password'),
        )).thenAnswer((_) async => tAuthResponseModel);

        when(mockLocalDataSource.saveToken(any)).thenAnswer((_) async => {});
        when(mockLocalDataSource.saveGuardian(any)).thenAnswer((_) async => {});

        // act
        await repository.login(
          usernameOrEmail: tUsernameOrEmail,
          password: tPassword,
        );

        // assert
        verify(mockLocalDataSource.saveToken(tToken));
        verify(mockLocalDataSource.saveGuardian(tGuardianModel));
      });

      test('should work with email as usernameOrEmail', () async {
        // arrange
        const tEmail = 'test@example.com';
        when(mockRemoteDataSource.login(
          usernameOrEmail: anyNamed('usernameOrEmail'),
          password: anyNamed('password'),
        )).thenAnswer((_) async => tAuthResponseModel);

        when(mockLocalDataSource.saveToken(any)).thenAnswer((_) async => {});
        when(mockLocalDataSource.saveGuardian(any)).thenAnswer((_) async => {});

        // act
        await repository.login(
          usernameOrEmail: tEmail,
          password: tPassword,
        );

        // assert
        verify(mockRemoteDataSource.login(
          usernameOrEmail: tEmail,
          password: tPassword,
        ));
      });

      test('should throw exception when remote login fails', () async {
        // arrange
        when(mockRemoteDataSource.login(
          usernameOrEmail: anyNamed('usernameOrEmail'),
          password: anyNamed('password'),
        )).thenThrow(Exception('Login failed'));

        // act & assert
        expect(
          () async => await repository.login(
            usernameOrEmail: tUsernameOrEmail,
            password: tPassword,
          ),
          throwsA(isA<Exception>()),
        );

        // Verify that local storage methods are not called when remote fails
        verifyNever(mockLocalDataSource.saveToken(any));
        verifyNever(mockLocalDataSource.saveGuardian(any));
      });
    });

    group('logout', () {
      test('should clear token and guardian from local storage', () async {
        // arrange
        when(mockLocalDataSource.removeToken()).thenAnswer((_) async => {});
        when(mockLocalDataSource.removeGuardian()).thenAnswer((_) async => {});

        // act
        await repository.logout();

        // assert
        verify(mockLocalDataSource.removeToken());
        verify(mockLocalDataSource.removeGuardian());
      });
    });

    group('getToken', () {
      test('should return token from local data source', () async {
        // arrange
        when(mockLocalDataSource.getToken()).thenAnswer((_) async => tToken);

        // act
        final result = await repository.getToken();

        // assert
        expect(result, equals(tToken));
        verify(mockLocalDataSource.getToken());
      });

      test('should return null when no token is stored', () async {
        // arrange
        when(mockLocalDataSource.getToken()).thenAnswer((_) async => null);

        // act
        final result = await repository.getToken();

        // assert
        expect(result, isNull);
        verify(mockLocalDataSource.getToken());
      });
    });

    group('saveToken', () {
      test('should save token to local data source', () async {
        // arrange
        when(mockLocalDataSource.saveToken(any)).thenAnswer((_) async => {});

        // act
        await repository.saveToken(tToken);

        // assert
        verify(mockLocalDataSource.saveToken(tToken));
      });
    });

    group('getCurrentGuardian', () {
      test('should return guardian from local data source', () async {
        // arrange
        when(mockLocalDataSource.getGuardian())
            .thenAnswer((_) async => tGuardianModel);

        // act
        final result = await repository.getCurrentGuardian();

        // assert
        expect(result, equals(tGuardianModel));
        verify(mockLocalDataSource.getGuardian());
      });

      test('should return null when no guardian is stored', () async {
        // arrange
        when(mockLocalDataSource.getGuardian()).thenAnswer((_) async => null);

        // act
        final result = await repository.getCurrentGuardian();

        // assert
        expect(result, isNull);
        verify(mockLocalDataSource.getGuardian());
      });
    });

    group('isLoggedIn', () {
      test('should return true when both token and guardian are present',
          () async {
        // arrange
        when(mockLocalDataSource.getToken()).thenAnswer((_) async => tToken);
        when(mockLocalDataSource.getGuardian())
            .thenAnswer((_) async => tGuardianModel);

        // act
        final result = await repository.isLoggedIn();

        // assert
        expect(result, isTrue);
        verify(mockLocalDataSource.getToken());
        verify(mockLocalDataSource.getGuardian());
      });

      test('should return false when token is null', () async {
        // arrange
        when(mockLocalDataSource.getToken()).thenAnswer((_) async => null);
        when(mockLocalDataSource.getGuardian())
            .thenAnswer((_) async => tGuardianModel);

        // act
        final result = await repository.isLoggedIn();

        // assert
        expect(result, isFalse);
      });

      test('should return false when guardian is null', () async {
        // arrange
        when(mockLocalDataSource.getToken()).thenAnswer((_) async => tToken);
        when(mockLocalDataSource.getGuardian()).thenAnswer((_) async => null);

        // act
        final result = await repository.isLoggedIn();

        // assert
        expect(result, isFalse);
      });

      test('should return false when both token and guardian are null',
          () async {
        // arrange
        when(mockLocalDataSource.getToken()).thenAnswer((_) async => null);
        when(mockLocalDataSource.getGuardian()).thenAnswer((_) async => null);

        // act
        final result = await repository.isLoggedIn();

        // assert
        expect(result, isFalse);
      });
    });

    group('integration scenarios', () {
      test('should handle complete login flow', () async {
        // arrange
        when(mockRemoteDataSource.login(
          usernameOrEmail: anyNamed('usernameOrEmail'),
          password: anyNamed('password'),
        )).thenAnswer((_) async => tAuthResponseModel);

        when(mockLocalDataSource.saveToken(any)).thenAnswer((_) async => {});
        when(mockLocalDataSource.saveGuardian(any)).thenAnswer((_) async => {});
        when(mockLocalDataSource.getToken()).thenAnswer((_) async => tToken);
        when(mockLocalDataSource.getGuardian())
            .thenAnswer((_) async => tGuardianModel);

        // act
        final loginResult = await repository.login(
          usernameOrEmail: 'test_guardian',
          password: 'password',
        );
        final isLoggedIn = await repository.isLoggedIn();
        final currentGuardian = await repository.getCurrentGuardian();

        // assert
        expect(loginResult, equals(tAuthResponseModel));
        expect(isLoggedIn, isTrue);
        expect(currentGuardian, equals(tGuardianModel));
      });

      test('should handle complete logout flow', () async {
        // arrange
        when(mockLocalDataSource.removeToken()).thenAnswer((_) async => {});
        when(mockLocalDataSource.removeGuardian()).thenAnswer((_) async => {});
        when(mockLocalDataSource.getToken()).thenAnswer((_) async => null);
        when(mockLocalDataSource.getGuardian()).thenAnswer((_) async => null);

        // act
        await repository.logout();
        final isLoggedIn = await repository.isLoggedIn();
        final currentGuardian = await repository.getCurrentGuardian();

        // assert
        expect(isLoggedIn, isFalse);
        expect(currentGuardian, isNull);
        verify(mockLocalDataSource.removeToken());
        verify(mockLocalDataSource.removeGuardian());
      });
    });
  });
}

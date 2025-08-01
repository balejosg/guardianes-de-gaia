import 'package:flutter_test/flutter_test.dart';
import 'package:bloc_test/bloc_test.dart';
import 'package:mockito/mockito.dart';
import 'package:mockito/annotations.dart';
import 'package:guardianes_mobile/features/auth/domain/entities/auth_result.dart';
import 'package:guardianes_mobile/features/auth/domain/entities/guardian.dart';
import 'package:guardianes_mobile/features/auth/domain/repositories/auth_repository.dart';
import 'package:guardianes_mobile/features/auth/domain/usecases/login_guardian.dart';
import 'package:guardianes_mobile/features/auth/domain/usecases/register_guardian.dart';
import 'package:guardianes_mobile/features/auth/presentation/bloc/auth_bloc.dart';

import 'auth_bloc_test.mocks.dart';

@GenerateMocks([
  LoginGuardian,
  RegisterGuardian,
  AuthRepository,
])
void main() {
  late AuthBloc authBloc;
  late MockLoginGuardian mockLoginGuardian;
  late MockRegisterGuardian mockRegisterGuardian;
  late MockAuthRepository mockAuthRepository;

  setUp(() {
    mockLoginGuardian = MockLoginGuardian();
    mockRegisterGuardian = MockRegisterGuardian();
    mockAuthRepository = MockAuthRepository();
    authBloc = AuthBloc(
      loginGuardian: mockLoginGuardian,
      registerGuardian: mockRegisterGuardian,
      authRepository: mockAuthRepository,
    );
  });

  tearDown(() {
    authBloc.close();
  });

  group('AuthBloc', () {
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

    test('initial state should be AuthInitial', () {
      expect(authBloc.state, equals(AuthInitial()));
    });

    group('AuthCheckStatus', () {
      blocTest<AuthBloc, AuthState>(
        'should emit [AuthLoading, AuthAuthenticated] when user is logged in',
        build: () {
          when(mockAuthRepository.isLoggedIn()).thenAnswer((_) async => true);
          when(mockAuthRepository.getCurrentGuardian())
              .thenAnswer((_) async => tGuardian);
          return authBloc;
        },
        act: (bloc) => bloc.add(AuthCheckStatus()),
        expect: () => [
          AuthLoading(),
          AuthAuthenticated(guardian: tGuardian),
        ],
        verify: (_) {
          verify(mockAuthRepository.isLoggedIn());
          verify(mockAuthRepository.getCurrentGuardian());
        },
      );

      blocTest<AuthBloc, AuthState>(
        'should emit [AuthLoading, AuthUnauthenticated] when user is not logged in',
        build: () {
          when(mockAuthRepository.isLoggedIn()).thenAnswer((_) async => false);
          return authBloc;
        },
        act: (bloc) => bloc.add(AuthCheckStatus()),
        expect: () => [
          AuthLoading(),
          AuthUnauthenticated(),
        ],
        verify: (_) {
          verify(mockAuthRepository.isLoggedIn());
          verifyNever(mockAuthRepository.getCurrentGuardian());
        },
      );

      blocTest<AuthBloc, AuthState>(
        'should emit [AuthLoading, AuthUnauthenticated] when isLoggedIn is true but getCurrentGuardian returns null',
        build: () {
          when(mockAuthRepository.isLoggedIn()).thenAnswer((_) async => true);
          when(mockAuthRepository.getCurrentGuardian())
              .thenAnswer((_) async => null);
          return authBloc;
        },
        act: (bloc) => bloc.add(AuthCheckStatus()),
        expect: () => [
          AuthLoading(),
          AuthUnauthenticated(),
        ],
        verify: (_) {
          verify(mockAuthRepository.isLoggedIn());
          verify(mockAuthRepository.getCurrentGuardian());
        },
      );

      blocTest<AuthBloc, AuthState>(
        'should emit [AuthLoading, AuthUnauthenticated] when exception occurs',
        build: () {
          when(mockAuthRepository.isLoggedIn())
              .thenThrow(Exception('Storage error'));
          return authBloc;
        },
        act: (bloc) => bloc.add(AuthCheckStatus()),
        expect: () => [
          AuthLoading(),
          AuthUnauthenticated(),
        ],
        verify: (_) {
          verify(mockAuthRepository.isLoggedIn());
        },
      );
    });

    group('AuthLoginRequested', () {
      const tUsernameOrEmail = 'test_guardian';
      const tPassword = 'test_password';

      blocTest<AuthBloc, AuthState>(
        'should emit [AuthLoading, AuthAuthenticated] when login is successful',
        build: () {
          when(mockLoginGuardian(
            usernameOrEmail: anyNamed('usernameOrEmail'),
            password: anyNamed('password'),
          )).thenAnswer((_) async => tAuthResult);
          return authBloc;
        },
        act: (bloc) => bloc.add(const AuthLoginRequested(
          usernameOrEmail: tUsernameOrEmail,
          password: tPassword,
        )),
        expect: () => [
          AuthLoading(),
          AuthAuthenticated(guardian: tGuardian),
        ],
        verify: (_) {
          verify(mockLoginGuardian(
            usernameOrEmail: tUsernameOrEmail,
            password: tPassword,
          ));
        },
      );

      blocTest<AuthBloc, AuthState>(
        'should emit [AuthLoading, AuthError] when login fails',
        build: () {
          when(mockLoginGuardian(
            usernameOrEmail: anyNamed('usernameOrEmail'),
            password: anyNamed('password'),
          )).thenThrow(Exception('Invalid credentials'));
          return authBloc;
        },
        act: (bloc) => bloc.add(const AuthLoginRequested(
          usernameOrEmail: tUsernameOrEmail,
          password: tPassword,
        )),
        expect: () => [
          AuthLoading(),
          const AuthError(message: 'Exception: Invalid credentials'),
        ],
        verify: (_) {
          verify(mockLoginGuardian(
            usernameOrEmail: tUsernameOrEmail,
            password: tPassword,
          ));
        },
      );

      blocTest<AuthBloc, AuthState>(
        'should work with email as usernameOrEmail',
        build: () {
          when(mockLoginGuardian(
            usernameOrEmail: anyNamed('usernameOrEmail'),
            password: anyNamed('password'),
          )).thenAnswer((_) async => tAuthResult);
          return authBloc;
        },
        act: (bloc) => bloc.add(const AuthLoginRequested(
          usernameOrEmail: 'test@example.com',
          password: tPassword,
        )),
        expect: () => [
          AuthLoading(),
          AuthAuthenticated(guardian: tGuardian),
        ],
        verify: (_) {
          verify(mockLoginGuardian(
            usernameOrEmail: 'test@example.com',
            password: tPassword,
          ));
        },
      );
    });

    group('AuthRegisterRequested', () {
      const tUsername = 'new_guardian';
      const tEmail = 'new@example.com';
      const tPassword = 'secure_password';
      const tName = 'New Guardian';
      final tBirthDate = DateTime.parse('2015-01-01T00:00:00Z');

      blocTest<AuthBloc, AuthState>(
        'should emit [AuthLoading, AuthAuthenticated] when registration is successful',
        build: () {
          when(mockRegisterGuardian(
            username: anyNamed('username'),
            email: anyNamed('email'),
            password: anyNamed('password'),
            name: anyNamed('name'),
            birthDate: anyNamed('birthDate'),
          )).thenAnswer((_) async => tAuthResult);
          return authBloc;
        },
        act: (bloc) => bloc.add(AuthRegisterRequested(
          username: tUsername,
          email: tEmail,
          password: tPassword,
          name: tName,
          birthDate: tBirthDate,
        )),
        expect: () => [
          AuthLoading(),
          AuthAuthenticated(guardian: tGuardian),
        ],
        verify: (_) {
          verify(mockRegisterGuardian(
            username: tUsername,
            email: tEmail,
            password: tPassword,
            name: tName,
            birthDate: tBirthDate,
          ));
        },
      );

      blocTest<AuthBloc, AuthState>(
        'should emit [AuthLoading, AuthError] when registration fails',
        build: () {
          when(mockRegisterGuardian(
            username: anyNamed('username'),
            email: anyNamed('email'),
            password: anyNamed('password'),
            name: anyNamed('name'),
            birthDate: anyNamed('birthDate'),
          )).thenThrow(Exception('Username already exists'));
          return authBloc;
        },
        act: (bloc) => bloc.add(AuthRegisterRequested(
          username: tUsername,
          email: tEmail,
          password: tPassword,
          name: tName,
          birthDate: tBirthDate,
        )),
        expect: () => [
          AuthLoading(),
          const AuthError(message: 'Exception: Username already exists'),
        ],
        verify: (_) {
          verify(mockRegisterGuardian(
            username: tUsername,
            email: tEmail,
            password: tPassword,
            name: tName,
            birthDate: tBirthDate,
          ));
        },
      );

      blocTest<AuthBloc, AuthState>(
        'should handle child registration correctly',
        build: () {
          final tChildGuardian = Guardian(
            id: 2,
            username: tUsername,
            email: tEmail,
            name: tName,
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

          final tChildAuthResult = AuthResult(
            token: tToken,
            guardian: tChildGuardian,
          );

          when(mockRegisterGuardian(
            username: anyNamed('username'),
            email: anyNamed('email'),
            password: anyNamed('password'),
            name: anyNamed('name'),
            birthDate: anyNamed('birthDate'),
          )).thenAnswer((_) async => tChildAuthResult);
          return authBloc;
        },
        act: (bloc) => bloc.add(AuthRegisterRequested(
          username: tUsername,
          email: tEmail,
          password: tPassword,
          name: tName,
          birthDate: DateTime.parse('2018-01-01T00:00:00Z'),
        )),
        expect: () => [
          AuthLoading(),
          predicate<AuthAuthenticated>(
              (state) => state.guardian.isChild && state.guardian.age == 6),
        ],
      );
    });

    group('AuthLogoutRequested', () {
      blocTest<AuthBloc, AuthState>(
        'should emit [AuthUnauthenticated] when logout is requested',
        build: () {
          when(mockAuthRepository.logout()).thenAnswer((_) async => {});
          return authBloc;
        },
        act: (bloc) => bloc.add(AuthLogoutRequested()),
        expect: () => [
          AuthUnauthenticated(),
        ],
        verify: (_) {
          verify(mockAuthRepository.logout());
        },
      );

      blocTest<AuthBloc, AuthState>(
        'should emit [AuthUnauthenticated] even when logout throws exception',
        build: () {
          when(mockAuthRepository.logout())
              .thenThrow(Exception('Logout error'));
          return authBloc;
        },
        act: (bloc) => bloc.add(AuthLogoutRequested()),
        expect: () => [
          AuthUnauthenticated(),
        ],
        verify: (_) {
          verify(mockAuthRepository.logout());
        },
      );
    });

    group('State transitions', () {
      blocTest<AuthBloc, AuthState>(
        'should handle multiple events correctly',
        build: () {
          when(mockLoginGuardian(
            usernameOrEmail: anyNamed('usernameOrEmail'),
            password: anyNamed('password'),
          )).thenAnswer((_) async => tAuthResult);
          when(mockAuthRepository.logout()).thenAnswer((_) async => {});
          return authBloc;
        },
        act: (bloc) {
          bloc.add(const AuthLoginRequested(
            usernameOrEmail: 'test',
            password: 'password',
          ));
          bloc.add(AuthLogoutRequested());
        },
        expect: () => [
          AuthLoading(),
          AuthAuthenticated(guardian: tGuardian),
          AuthUnauthenticated(),
        ],
      );

      blocTest<AuthBloc, AuthState>(
        'should maintain loading state during async operations',
        build: () {
          when(mockLoginGuardian(
            usernameOrEmail: anyNamed('usernameOrEmail'),
            password: anyNamed('password'),
          )).thenAnswer((_) async {
            await Future.delayed(const Duration(milliseconds: 50));
            return tAuthResult;
          });
          return authBloc;
        },
        act: (bloc) => bloc.add(const AuthLoginRequested(
          usernameOrEmail: 'test',
          password: 'password',
        )),
        wait: const Duration(milliseconds: 100),
        expect: () => [
          AuthLoading(),
          AuthAuthenticated(guardian: tGuardian),
        ],
      );
    });

    group('Edge cases', () {
      blocTest<AuthBloc, AuthState>(
        'should handle empty credentials gracefully',
        build: () {
          when(mockLoginGuardian(
            usernameOrEmail: anyNamed('usernameOrEmail'),
            password: anyNamed('password'),
          )).thenThrow(Exception('Invalid input'));
          return authBloc;
        },
        act: (bloc) => bloc.add(const AuthLoginRequested(
          usernameOrEmail: '',
          password: '',
        )),
        expect: () => [
          AuthLoading(),
          const AuthError(message: 'Exception: Invalid input'),
        ],
      );

      blocTest<AuthBloc, AuthState>(
        'should handle network errors during registration',
        build: () {
          when(mockRegisterGuardian(
            username: anyNamed('username'),
            email: anyNamed('email'),
            password: anyNamed('password'),
            name: anyNamed('name'),
            birthDate: anyNamed('birthDate'),
          )).thenThrow(Exception('Network error'));
          return authBloc;
        },
        act: (bloc) => bloc.add(AuthRegisterRequested(
          username: 'test',
          email: 'test@example.com',
          password: 'password',
          name: 'Test',
          birthDate: DateTime.now(),
        )),
        expect: () => [
          AuthLoading(),
          const AuthError(message: 'Exception: Network error'),
        ],
      );
    });
  });
}

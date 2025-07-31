import 'dart:convert';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:mockito/annotations.dart';
import 'package:http/http.dart' as http;
import 'package:guardianes_mobile/features/auth/data/datasources/auth_remote_datasource.dart';
import 'package:guardianes_mobile/features/auth/data/models/auth_response_model.dart';
import 'package:guardianes_mobile/features/auth/data/models/guardian_model.dart';

import 'auth_remote_datasource_test.mocks.dart';

@GenerateMocks([http.Client])
void main() {
  late AuthRemoteDataSourceImpl dataSource;
  late MockClient mockHttpClient;

  setUp(() {
    mockHttpClient = MockClient();
    dataSource = AuthRemoteDataSourceImpl(client: mockHttpClient);
  });

  group('AuthRemoteDataSource', () {
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

    group('register', () {
      const tUsername = 'new_guardian';
      const tEmail = 'new@example.com';
      const tPassword = 'secure_password';
      const tName = 'New Guardian';
      final tBirthDate = DateTime.parse('2015-01-01T00:00:00Z');

      final tSuccessResponse = http.Response(
        jsonEncode(tAuthResponseModel.toJson()),
        201,
      );

      test('should perform POST request to /auth/register with correct data', () async {
        // arrange
        when(mockHttpClient.post(
          any,
          headers: anyNamed('headers'),
          body: anyNamed('body'),
        )).thenAnswer((_) async => tSuccessResponse);

        // act
        await dataSource.register(
          username: tUsername,
          email: tEmail,
          password: tPassword,
          name: tName,
          birthDate: tBirthDate,
        );

        // assert
        verify(mockHttpClient.post(
          Uri.parse('https://my-guardianes.duckdns.org/api/auth/register'),
          headers: {
            'Content-Type': 'application/json',
          },
          body: jsonEncode({
            'username': tUsername,
            'email': tEmail,
            'password': tPassword,
            'name': tName,
            'birthDate': '2015-01-01',
          }),
        ));
      });

      test('should return AuthResponseModel when registration is successful', () async {
        // arrange
        when(mockHttpClient.post(
          any,
          headers: anyNamed('headers'),
          body: anyNamed('body'),
        )).thenAnswer((_) async => tSuccessResponse);

        // act
        final result = await dataSource.register(
          username: tUsername,
          email: tEmail,
          password: tPassword,
          name: tName,
          birthDate: tBirthDate,
        );

        // assert
        expect(result, equals(tAuthResponseModel));
      });

      test('should throw exception when registration fails with 400', () async {
        // arrange
        final tErrorResponse = http.Response(
          jsonEncode({'message': 'Username already exists'}),
          400,
        );
        when(mockHttpClient.post(
          any,
          headers: anyNamed('headers'),
          body: anyNamed('body'),
        )).thenAnswer((_) async => tErrorResponse);

        // act & assert
        expect(
          () async => await dataSource.register(
            username: tUsername,
            email: tEmail,
            password: tPassword,
            name: tName,
            birthDate: tBirthDate,
          ),
          throwsA(
            predicate((e) => 
              e is Exception && e.toString().contains('Username already exists')),
          ),
        );
      });

      test('should throw exception when registration fails with 500', () async {
        // arrange
        final tErrorResponse = http.Response(
          jsonEncode({'message': 'Internal server error'}),
          500,
        );
        when(mockHttpClient.post(
          any,
          headers: anyNamed('headers'),
          body: anyNamed('body'),
        )).thenAnswer((_) async => tErrorResponse);

        // act & assert
        expect(
          () async => await dataSource.register(
            username: tUsername,
            email: tEmail,
            password: tPassword,
            name: tName,
            birthDate: tBirthDate,
          ),
          throwsA(
            predicate((e) => 
              e is Exception && e.toString().contains('Internal server error')),
          ),
        );
      });

      test('should throw generic exception when error response has no message', () async {
        // arrange
        final tErrorResponse = http.Response('{}', 400);
        when(mockHttpClient.post(
          any,
          headers: anyNamed('headers'),
          body: anyNamed('body'),
        )).thenAnswer((_) async => tErrorResponse);

        // act & assert
        expect(
          () async => await dataSource.register(
            username: tUsername,
            email: tEmail,
            password: tPassword,
            name: tName,
            birthDate: tBirthDate,
          ),
          throwsA(
            predicate((e) => 
              e is Exception && e.toString().contains('Registration failed')),
          ),
        );
      });

      test('should format birthDate correctly', () async {
        // arrange
        final tDifferentBirthDate = DateTime.parse('2016-12-25T15:30:45Z');
        when(mockHttpClient.post(
          any,
          headers: anyNamed('headers'),
          body: anyNamed('body'),
        )).thenAnswer((_) async => tSuccessResponse);

        // act
        await dataSource.register(
          username: tUsername,
          email: tEmail,
          password: tPassword,
          name: tName,
          birthDate: tDifferentBirthDate,
        );

        // assert
        verify(mockHttpClient.post(
          any,
          headers: anyNamed('headers'),
          body: argThat(
            contains('"birthDate":"2016-12-25"'),
            named: 'body',
          ),
        ));
      });
    });

    group('login', () {
      const tUsernameOrEmail = 'test_guardian';
      const tPassword = 'test_password';

      final tSuccessResponse = http.Response(
        jsonEncode(tAuthResponseModel.toJson()),
        200,
      );

      test('should perform POST request to /auth/login with correct data', () async {
        // arrange
        when(mockHttpClient.post(
          any,
          headers: anyNamed('headers'),
          body: anyNamed('body'),
        )).thenAnswer((_) async => tSuccessResponse);

        // act
        await dataSource.login(
          usernameOrEmail: tUsernameOrEmail,
          password: tPassword,
        );

        // assert
        verify(mockHttpClient.post(
          Uri.parse('https://my-guardianes.duckdns.org/api/auth/login'),
          headers: {
            'Content-Type': 'application/json',
          },
          body: jsonEncode({
            'usernameOrEmail': tUsernameOrEmail,
            'password': tPassword,
          }),
        ));
      });

      test('should return AuthResponseModel when login is successful', () async {
        // arrange
        when(mockHttpClient.post(
          any,
          headers: anyNamed('headers'),
          body: anyNamed('body'),
        )).thenAnswer((_) async => tSuccessResponse);

        // act
        final result = await dataSource.login(
          usernameOrEmail: tUsernameOrEmail,
          password: tPassword,
        );

        // assert
        expect(result, equals(tAuthResponseModel));
      });

      test('should work with email as usernameOrEmail', () async {
        // arrange
        const tEmail = 'test@example.com';
        when(mockHttpClient.post(
          any,
          headers: anyNamed('headers'),
          body: anyNamed('body'),
        )).thenAnswer((_) async => tSuccessResponse);

        // act
        await dataSource.login(
          usernameOrEmail: tEmail,
          password: tPassword,
        );

        // assert
        verify(mockHttpClient.post(
          any,
          headers: anyNamed('headers'),
          body: argThat(
            contains('"usernameOrEmail":"$tEmail"'),
            named: 'body',
          ),
        ));
      });

      test('should throw exception when login fails with 401', () async {
        // arrange
        final tErrorResponse = http.Response(
          jsonEncode({'message': 'Invalid credentials'}),
          401,
        );
        when(mockHttpClient.post(
          any,
          headers: anyNamed('headers'),
          body: anyNamed('body'),
        )).thenAnswer((_) async => tErrorResponse);

        // act & assert
        expect(
          () async => await dataSource.login(
            usernameOrEmail: tUsernameOrEmail,
            password: tPassword,
          ),
          throwsA(
            predicate((e) => 
              e is Exception && e.toString().contains('Invalid credentials')),
          ),
        );
      });

      test('should throw exception when login fails with 404', () async {
        // arrange
        final tErrorResponse = http.Response(
          jsonEncode({'message': 'Guardian not found'}),
          404,
        );
        when(mockHttpClient.post(
          any,
          headers: anyNamed('headers'),
          body: anyNamed('body'),
        )).thenAnswer((_) async => tErrorResponse);

        // act & assert
        expect(
          () async => await dataSource.login(
            usernameOrEmail: tUsernameOrEmail,
            password: tPassword,
          ),
          throwsA(
            predicate((e) => 
              e is Exception && e.toString().contains('Guardian not found')),
          ),
        );
      });

      test('should throw generic exception when error response has no message', () async {
        // arrange
        final tErrorResponse = http.Response('{}', 401);
        when(mockHttpClient.post(
          any,
          headers: anyNamed('headers'),
          body: anyNamed('body'),
        )).thenAnswer((_) async => tErrorResponse);

        // act & assert
        expect(
          () async => await dataSource.login(
            usernameOrEmail: tUsernameOrEmail,
            password: tPassword,
          ),
          throwsA(
            predicate((e) => 
              e is Exception && e.toString().contains('Login failed')),
          ),
        );
      });
    });

    group('baseUrl configuration', () {
      test('should use provided baseUrl', () async {
        // arrange
        const customBaseUrl = 'https://api.guardianes.com/v1';
        final customDataSource = AuthRemoteDataSourceImpl(
          client: mockHttpClient,
          baseUrl: customBaseUrl,
        );

        when(mockHttpClient.post(
          any,
          headers: anyNamed('headers'),
          body: anyNamed('body'),
        )).thenAnswer((_) async => http.Response(
          jsonEncode(tAuthResponseModel.toJson()),
          200,
        ));

        // act
        await customDataSource.login(
          usernameOrEmail: 'test',
          password: 'password',
        );

        // assert
        verify(mockHttpClient.post(
          Uri.parse('$customBaseUrl/auth/login'),
          headers: anyNamed('headers'),
          body: anyNamed('body'),
        ));
      });
    });
  });
}
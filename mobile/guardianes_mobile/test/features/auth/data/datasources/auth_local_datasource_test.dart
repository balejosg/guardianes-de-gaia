import 'dart:convert';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:guardianes_mobile/features/auth/data/datasources/auth_local_datasource.dart';
import 'package:guardianes_mobile/features/auth/data/models/guardian_model.dart';

void main() {
  late AuthLocalDataSourceImpl dataSource;
  late SharedPreferences sharedPreferences;

  setUp(() async {
    SharedPreferences.setMockInitialValues({});
    sharedPreferences = await SharedPreferences.getInstance();
    dataSource = AuthLocalDataSourceImpl(sharedPreferences: sharedPreferences);
  });

  group('AuthLocalDataSource', () {
    group('saveToken', () {
      const tToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token';

      test('should call SharedPreferences to save the token', () async {
        // act
        await dataSource.saveToken(tToken);

        // assert
        final savedToken =
            sharedPreferences.getString(AuthLocalDataSourceImpl.tokenKey);
        expect(savedToken, equals(tToken));
      });
    });

    group('getToken', () {
      const tToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token';

      test('should return token from SharedPreferences when present', () async {
        // arrange
        await sharedPreferences.setString(
            AuthLocalDataSourceImpl.tokenKey, tToken);

        // act
        final result = await dataSource.getToken();

        // assert
        expect(result, equals(tToken));
      });

      test('should return null when token is not present', () async {
        // act
        final result = await dataSource.getToken();

        // assert
        expect(result, isNull);
      });
    });

    group('removeToken', () {
      test('should call SharedPreferences to remove the token', () async {
        // arrange
        const tToken = 'test_token';
        await sharedPreferences.setString(
            AuthLocalDataSourceImpl.tokenKey, tToken);

        // act
        await dataSource.removeToken();

        // assert
        final token =
            sharedPreferences.getString(AuthLocalDataSourceImpl.tokenKey);
        expect(token, isNull);
      });
    });

    group('saveGuardian', () {
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

      test('should call SharedPreferences to save guardian as JSON string',
          () async {
        // act
        await dataSource.saveGuardian(tGuardianModel);

        // assert
        final savedGuardianJson =
            sharedPreferences.getString(AuthLocalDataSourceImpl.guardianKey);
        final expectedJsonString = jsonEncode(tGuardianModel.toJson());
        expect(savedGuardianJson, equals(expectedJsonString));
      });
    });

    group('getGuardian', () {
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

      test('should return GuardianModel from SharedPreferences when present',
          () async {
        // arrange
        final jsonString = jsonEncode(tGuardianModel.toJson());
        await sharedPreferences.setString(
            AuthLocalDataSourceImpl.guardianKey, jsonString);

        // act
        final result = await dataSource.getGuardian();

        // assert
        expect(result, equals(tGuardianModel));
      });

      test('should return null when guardian is not present', () async {
        // act
        final result = await dataSource.getGuardian();

        // assert
        expect(result, isNull);
      });

      test('should handle invalid JSON gracefully', () async {
        // arrange
        await sharedPreferences.setString(
            AuthLocalDataSourceImpl.guardianKey, 'invalid_json');

        // act & assert
        expect(
          () async => await dataSource.getGuardian(),
          throwsA(isA<FormatException>()),
        );
      });
    });

    group('removeGuardian', () {
      test('should call SharedPreferences to remove the guardian', () async {
        // arrange
        const guardianJson = '{"id": 1, "name": "Test"}';
        await sharedPreferences.setString(
            AuthLocalDataSourceImpl.guardianKey, guardianJson);

        // act
        await dataSource.removeGuardian();

        // assert
        final guardian =
            sharedPreferences.getString(AuthLocalDataSourceImpl.guardianKey);
        expect(guardian, isNull);
      });
    });

    group('integration tests', () {
      test('should save and retrieve token correctly', () async {
        // arrange
        const tToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token';

        // act
        await dataSource.saveToken(tToken);
        final result = await dataSource.getToken();

        // assert
        expect(result, equals(tToken));
      });

      test('should save and retrieve guardian correctly', () async {
        // arrange
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

        // act
        await dataSource.saveGuardian(tGuardianModel);
        final result = await dataSource.getGuardian();

        // assert
        expect(result, equals(tGuardianModel));
      });

      test('should clear all authentication data', () async {
        // arrange
        const tToken = 'test_token';
        const guardianJson = '{"id": 1, "name": "Test"}';
        await sharedPreferences.setString(
            AuthLocalDataSourceImpl.tokenKey, tToken);
        await sharedPreferences.setString(
            AuthLocalDataSourceImpl.guardianKey, guardianJson);

        // act
        await dataSource.removeToken();
        await dataSource.removeGuardian();

        // assert
        final token =
            sharedPreferences.getString(AuthLocalDataSourceImpl.tokenKey);
        final guardian =
            sharedPreferences.getString(AuthLocalDataSourceImpl.guardianKey);
        expect(token, isNull);
        expect(guardian, isNull);
      });
    });
  });
}

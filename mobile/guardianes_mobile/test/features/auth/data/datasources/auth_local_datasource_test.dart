import 'dart:convert';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:mockito/annotations.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:guardianes_mobile/features/auth/data/datasources/auth_local_datasource.dart';
import 'package:guardianes_mobile/features/auth/data/models/guardian_model.dart';

import 'auth_local_datasource_test.mocks.dart';

@GenerateMocks([SharedPreferences])
void main() {
  late AuthLocalDataSourceImpl dataSource;
  late MockSharedPreferences mockSharedPreferences;

  setUp(() {
    mockSharedPreferences = MockSharedPreferences();
    dataSource =
        AuthLocalDataSourceImpl(sharedPreferences: mockSharedPreferences);
  });

  group('AuthLocalDataSource', () {
    group('saveToken', () {
      const tToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token';

      test('should call SharedPreferences to save the token', () async {
        // arrange
        when(mockSharedPreferences.setString(any, any))
            .thenAnswer((_) async => true);

        // act
        await dataSource.saveToken(tToken);

        // assert
        verify(mockSharedPreferences.setString(
          AuthLocalDataSourceImpl.tokenKey,
          tToken,
        ));
      });
    });

    group('getToken', () {
      const tToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token';

      test('should return token from SharedPreferences when present', () async {
        // arrange
        when(mockSharedPreferences.getString(any)).thenReturn(tToken);

        // act
        final result = await dataSource.getToken();

        // assert
        expect(result, equals(tToken));
        verify(
            mockSharedPreferences.getString(AuthLocalDataSourceImpl.tokenKey));
      });

      test('should return null when token is not present', () async {
        // arrange
        when(mockSharedPreferences.getString(any)).thenReturn(null);

        // act
        final result = await dataSource.getToken();

        // assert
        expect(result, isNull);
        verify(
            mockSharedPreferences.getString(AuthLocalDataSourceImpl.tokenKey));
      });
    });

    group('removeToken', () {
      test('should call SharedPreferences to remove the token', () async {
        // arrange
        when(mockSharedPreferences.remove(any)).thenAnswer((_) async => true);

        // act
        await dataSource.removeToken();

        // assert
        verify(mockSharedPreferences.remove(AuthLocalDataSourceImpl.tokenKey));
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
        // arrange
        when(mockSharedPreferences.setString(any, any))
            .thenAnswer((_) async => true);

        // act
        await dataSource.saveGuardian(tGuardianModel);

        // assert
        final expectedJsonString = jsonEncode(tGuardianModel.toJson());
        verify(mockSharedPreferences.setString(
          AuthLocalDataSourceImpl.guardianKey,
          expectedJsonString,
        ));
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
        when(mockSharedPreferences.getString(any)).thenReturn(jsonString);

        // act
        final result = await dataSource.getGuardian();

        // assert
        expect(result, equals(tGuardianModel));
        verify(mockSharedPreferences
            .getString(AuthLocalDataSourceImpl.guardianKey));
      });

      test('should return null when guardian is not present', () async {
        // arrange
        when(mockSharedPreferences.getString(any)).thenReturn(null);

        // act
        final result = await dataSource.getGuardian();

        // assert
        expect(result, isNull);
        verify(mockSharedPreferences
            .getString(AuthLocalDataSourceImpl.guardianKey));
      });

      test('should handle invalid JSON gracefully', () async {
        // arrange
        when(mockSharedPreferences.getString(any)).thenReturn('invalid_json');

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
        when(mockSharedPreferences.remove(any)).thenAnswer((_) async => true);

        // act
        await dataSource.removeGuardian();

        // assert
        verify(
            mockSharedPreferences.remove(AuthLocalDataSourceImpl.guardianKey));
      });
    });

    group('integration tests', () {
      test('should save and retrieve token correctly', () async {
        // arrange
        const tToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token';
        when(mockSharedPreferences.setString(any, any))
            .thenAnswer((_) async => true);
        when(mockSharedPreferences.getString(AuthLocalDataSourceImpl.tokenKey))
            .thenReturn(tToken);

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

        final jsonString = jsonEncode(tGuardianModel.toJson());
        when(mockSharedPreferences.setString(any, any))
            .thenAnswer((_) async => true);
        when(mockSharedPreferences
                .getString(AuthLocalDataSourceImpl.guardianKey))
            .thenReturn(jsonString);

        // act
        await dataSource.saveGuardian(tGuardianModel);
        final result = await dataSource.getGuardian();

        // assert
        expect(result, equals(tGuardianModel));
      });

      test('should clear all authentication data', () async {
        // arrange
        when(mockSharedPreferences.remove(any)).thenAnswer((_) async => true);

        // act
        await dataSource.removeToken();
        await dataSource.removeGuardian();

        // assert
        verify(mockSharedPreferences.remove(AuthLocalDataSourceImpl.tokenKey));
        verify(
            mockSharedPreferences.remove(AuthLocalDataSourceImpl.guardianKey));
      });
    });
  });
}

import 'dart:convert';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:mockito/annotations.dart';
import 'package:http/http.dart' as http;
import 'package:guardianes_mobile/features/auth/data/datasources/guardian_profile_remote_datasource.dart';

@GenerateMocks([http.Client])
import 'guardian_profile_remote_datasource_test.mocks.dart';

void main() {
  late GuardianProfileRemoteDataSourceImpl dataSource;
  late MockClient mockClient;
  const baseUrl = 'http://test.local';

  setUp(() {
    mockClient = MockClient();
    dataSource = GuardianProfileRemoteDataSourceImpl(
      client: mockClient,
      baseUrl: baseUrl,
    );
  });

  group('GuardianProfileRemoteDataSource', () {
    group('getGuardianProfile', () {
      test('should return GuardianModel on success', () async {
        // Arrange
        final responseBody = json.encode({
          'id': 1,
          'username': 'testuser',
          'name': 'Test User',
          'email': 'test@example.com',
          'birthDate': '2000-01-01',
          'level': 5,
          'experiencePoints': 1500,
          'totalStepsWalked': 50000,
          'createdAt': '2025-01-01T00:00:00Z',
        });

        when(mockClient.get(
          any,
          headers: anyNamed('headers'),
        )).thenAnswer((_) async => http.Response(responseBody, 200));

        // Act
        final result = await dataSource.getGuardianProfile(1);

        // Assert
        expect(result.id, 1);
        expect(result.username, 'testuser');
        expect(result.name, 'Test User');
      });

      test('should throw exception when guardian not found (404)', () async {
        // Arrange
        when(mockClient.get(
          any,
          headers: anyNamed('headers'),
        )).thenAnswer((_) async => http.Response('Not found', 404));

        // Act & Assert
        expect(
          () => dataSource.getGuardianProfile(999),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('Guardian not found'),
          )),
        );
      });

      test('should throw exception on server error', () async {
        // Arrange
        when(mockClient.get(
          any,
          headers: anyNamed('headers'),
        )).thenAnswer((_) async => http.Response('Server error', 500));

        // Act & Assert
        expect(
          () => dataSource.getGuardianProfile(1),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('Failed to get guardian profile'),
          )),
        );
      });
    });

    group('updateGuardianProfile', () {
      test('should return updated GuardianModel on success', () async {
        // Arrange
        final responseBody = json.encode({
          'id': 1,
          'username': 'testuser',
          'name': 'Updated Name',
          'email': 'test@example.com',
          'birthDate': '2000-01-01',
          'level': 5,
          'experiencePoints': 1500,
          'totalStepsWalked': 50000,
          'createdAt': '2025-01-01T00:00:00Z',
        });

        when(mockClient.put(
          any,
          headers: anyNamed('headers'),
          body: anyNamed('body'),
        )).thenAnswer((_) async => http.Response(responseBody, 200));

        // Act
        final result = await dataSource.updateGuardianProfile(
          1,
          {'name': 'Updated Name'},
        );

        // Assert
        expect(result.name, 'Updated Name');
      });

      test('should throw exception when guardian not found (404)', () async {
        // Arrange
        when(mockClient.put(
          any,
          headers: anyNamed('headers'),
          body: anyNamed('body'),
        )).thenAnswer((_) async => http.Response('Not found', 404));

        // Act & Assert
        expect(
          () => dataSource.updateGuardianProfile(999, {'name': 'New'}),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('Guardian not found'),
          )),
        );
      });

      test('should throw exception on server error', () async {
        // Arrange
        when(mockClient.put(
          any,
          headers: anyNamed('headers'),
          body: anyNamed('body'),
        )).thenAnswer((_) async => http.Response('Server error', 500));

        // Act & Assert
        expect(
          () => dataSource.updateGuardianProfile(1, {'name': 'New'}),
          throwsA(isA<Exception>().having(
            (e) => e.toString(),
            'message',
            contains('Failed to update guardian profile'),
          )),
        );
      });
    });
  });
}

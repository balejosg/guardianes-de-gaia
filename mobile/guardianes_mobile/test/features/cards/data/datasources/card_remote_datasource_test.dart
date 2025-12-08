import 'dart:convert';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:mockito/annotations.dart';
import 'package:http/http.dart' as http;
import 'package:guardianes_mobile/features/cards/data/datasources/card_remote_datasource.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/card.dart';

@GenerateMocks([http.Client])
import 'card_remote_datasource_test.mocks.dart';

void main() {
  late CardRemoteDataSourceImpl dataSource;
  late MockClient mockClient;
  const baseUrl = 'http://test.local';

  setUp(() {
    mockClient = MockClient();
    dataSource = CardRemoteDataSourceImpl(
      client: mockClient,
      baseUrl: baseUrl,
    );
  });

  group('CardRemoteDataSource', () {
    group('scanQRCode', () {
      const guardianId = 1;
      const qrCode = 'valid_qr';

      test('should return CardScanResultModel when response is 200', () async {
        // Arrange
        final responseBody = json.encode({
          'success': true,
          'message': 'Card collected!',
          'card': {
            'id': 1,
            'name': 'Fire Dragon',
            'description': 'A fierce dragon',
            'imageUrl': 'http://example.com/img.png',
            'element': 'FIRE',
            'rarity': 'LEGENDARY',
            'attackPower': 100,
            'defensePower': 80,
            'energyCost': 5,
            'qrCode': 'dragon_qr',
            'createdAt': '2025-01-01T00:00:00Z',
            'active': true,
          },
          'count': 1,
          'isNew': true,
        });

        when(mockClient.post(
          any,
          headers: anyNamed('headers'),
          body: anyNamed('body'),
        )).thenAnswer((_) async => http.Response(responseBody, 200));

        // Act
        final result = await dataSource.scanQRCode(guardianId, qrCode);

        // Assert
        expect(result.success, true);
        expect(result.card?.name, 'Fire Dragon');
        expect(result.isNew, true);
      });

      test('should throw exception when response is 400', () async {
        // Arrange
        final responseBody = json.encode({
          'success': false,
          'message': 'Invalid QR code',
          'isNew': false,
        });

        when(mockClient.post(
          any,
          headers: anyNamed('headers'),
          body: anyNamed('body'),
        )).thenAnswer((_) async => http.Response(responseBody, 400));

        // Act & Assert
        expect(
          () => dataSource.scanQRCode(guardianId, qrCode),
          throwsException,
        );
      });

      test('should throw exception when response is 500', () async {
        // Arrange
        when(mockClient.post(
          any,
          headers: anyNamed('headers'),
          body: anyNamed('body'),
        )).thenAnswer((_) async => http.Response('Server Error', 500));

        // Act & Assert
        expect(
          () => dataSource.scanQRCode(guardianId, qrCode),
          throwsException,
        );
      });
    });

    group('getGuardianCards', () {
      const guardianId = 1;

      test('should return list of CollectedCardModel when response is 200',
          () async {
        // Arrange
        final responseBody = json.encode([
          {
            'card': {
              'id': 1,
              'name': 'Water Spirit',
              'description': 'A calm spirit',
              'imageUrl': 'http://example.com/water.png',
              'element': 'WATER',
              'rarity': 'COMMON',
              'attackPower': 20,
              'defensePower': 30,
              'energyCost': 2,
              'qrCode': 'water_qr',
              'createdAt': '2025-01-01T00:00:00Z',
              'active': true,
            },
            'count': 2,
            'firstCollectedAt': '2025-01-01T00:00:00Z',
            'lastCollectedAt': '2025-01-02T00:00:00Z',
          },
        ]);

        when(mockClient.get(
          any,
          headers: anyNamed('headers'),
        )).thenAnswer((_) async => http.Response(responseBody, 200));

        // Act
        final result = await dataSource.getGuardianCards(guardianId);

        // Assert
        expect(result.length, 1);
        expect(result.first.card.name, 'Water Spirit');
        expect(result.first.count, 2);
      });

      test('should throw exception when response is not 200', () async {
        // Arrange
        when(mockClient.get(
          any,
          headers: anyNamed('headers'),
        )).thenAnswer((_) async => http.Response('Error', 500));

        // Act & Assert
        expect(
          () => dataSource.getGuardianCards(guardianId),
          throwsException,
        );
      });
    });

    group('getCollectionStatistics', () {
      const guardianId = 1;

      test('should return CollectionStatisticsModel when response is 200',
          () async {
        // Arrange
        final responseBody = json.encode({
          'success': true,
          'message': 'OK',
          'timestamp': '2025-01-01T00:00:00Z',
          'data': {
            'totalCardCount': 25,
            'uniqueCardCount': 15,
            'completionPercentage': 31.25,
            'cardCountsByElement': {'FIRE': 5, 'WATER': 4},
            'cardCountsByRarity': {'COMMON': 10},
            'totalTradeValue': 50,
            'hasElementalBalance': true,
          },
        });

        when(mockClient.get(
          any,
          headers: anyNamed('headers'),
        )).thenAnswer((_) async => http.Response(responseBody, 200));

        // Act
        final result = await dataSource.getCollectionStatistics(guardianId);

        // Assert
        expect(result.totalCardCount, 25);
        expect(result.uniqueCardCount, 15);
        expect(result.completionPercentage, 31.25);
      });

      test('should throw exception when response is not 200', () async {
        // Arrange
        when(mockClient.get(
          any,
          headers: anyNamed('headers'),
        )).thenAnswer((_) async => http.Response('Error', 500));

        // Act & Assert
        expect(
          () => dataSource.getCollectionStatistics(guardianId),
          throwsException,
        );
      });
    });

    group('searchCards', () {
      test('should return list of CardModel when search succeeds', () async {
        // Arrange
        final responseBody = json.encode({
          'success': true,
          'message': 'OK',
          'timestamp': '2025-01-01T00:00:00Z',
          'data': [
            {
              'id': 1,
              'name': 'Fire Dragon',
              'description': 'A fierce dragon',
              'imageUrl': 'http://example.com/img.png',
              'element': 'FIRE',
              'rarity': 'LEGENDARY',
              'attackPower': 100,
              'defensePower': 80,
              'energyCost': 5,
              'qrCode': 'dragon_qr',
              'createdAt': '2025-01-01T00:00:00Z',
              'active': true,
            },
          ],
        });

        when(mockClient.get(
          any,
          headers: anyNamed('headers'),
        )).thenAnswer((_) async => http.Response(responseBody, 200));

        // Act
        final result = await dataSource.searchCards(name: 'Dragon');

        // Assert
        expect(result.length, 1);
        expect(result.first.name, 'Fire Dragon');
      });

      test('should include element filter in query params', () async {
        // Arrange
        final responseBody = json.encode({
          'success': true,
          'message': 'OK',
          'timestamp': '2025-01-01T00:00:00Z',
          'data': [],
        });

        when(mockClient.get(
          any,
          headers: anyNamed('headers'),
        )).thenAnswer((_) async => http.Response(responseBody, 200));

        // Act
        await dataSource.searchCards(element: CardElement.fire);

        // Assert
        verify(mockClient.get(
          argThat(predicate<Uri>((uri) => uri.queryParameters['element'] == 'FIRE')),
          headers: anyNamed('headers'),
        )).called(1);
      });
    });

    group('getCard', () {
      test('should return CardModel when card exists', () async {
        // Arrange
        final responseBody = json.encode({
          'success': true,
          'message': 'OK',
          'timestamp': '2025-01-01T00:00:00Z',
          'data': {
            'id': 1,
            'name': 'Earth Golem',
            'description': 'A sturdy golem',
            'imageUrl': 'http://example.com/golem.png',
            'element': 'EARTH',
            'rarity': 'RARE',
            'attackPower': 60,
            'defensePower': 90,
            'energyCost': 4,
            'qrCode': 'golem_qr',
            'createdAt': '2025-01-01T00:00:00Z',
            'active': true,
          },
        });

        when(mockClient.get(
          any,
          headers: anyNamed('headers'),
        )).thenAnswer((_) async => http.Response(responseBody, 200));

        // Act
        final result = await dataSource.getCard(1);

        // Assert
        expect(result, isNotNull);
        expect(result!.name, 'Earth Golem');
      });

      test('should return null when card not found (404)', () async {
        // Arrange
        when(mockClient.get(
          any,
          headers: anyNamed('headers'),
        )).thenAnswer((_) async => http.Response('Not Found', 404));

        // Act
        final result = await dataSource.getCard(999);

        // Assert
        expect(result, isNull);
      });
    });
  });
}

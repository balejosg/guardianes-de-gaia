import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:mockito/annotations.dart';
import 'package:guardianes_mobile/features/cards/data/repositories/card_repository_impl.dart';
import 'package:guardianes_mobile/features/cards/data/datasources/card_remote_datasource.dart';
import 'package:guardianes_mobile/features/cards/data/models/card_scan_result_model.dart';
import 'package:guardianes_mobile/features/cards/data/models/card_model.dart';
import 'package:guardianes_mobile/features/cards/data/models/collected_card_model.dart';
import 'package:guardianes_mobile/features/cards/data/models/collection_statistics_model.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/card.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/card_scan_result.dart';

@GenerateMocks([CardRemoteDataSource])
import 'card_repository_impl_test.mocks.dart';

void main() {
  late CardRepositoryImpl repository;
  late MockCardRemoteDataSource mockRemoteDataSource;

  setUp(() {
    mockRemoteDataSource = MockCardRemoteDataSource();
    repository = CardRepositoryImpl(remoteDataSource: mockRemoteDataSource);
  });

  const tGuardianId = 1;
  const tQrCode = 'valid_qr';

  group('scanQRCode', () {
    final tCardModel = CardModel(
        id: 1,
        name: 'Fire Dragon',
        description: 'A fierce dragon',
        imageUrl: 'url',
        element: 'FIRE', 
        rarity: 'LEGENDARY',
        attackPower: 100,
        defensePower: 80,
        energyCost: 5,
        qrCode: 'qr',
        createdAt: '2025-01-01T00:00:00Z',
        active: true
    );

    final tScanModel = CardScanResultModel(
      success: true,
      message: 'Card collected',
      card: tCardModel,
      count: 1,
      isNew: true,
    );

    test('should return success result when remote data source is successful', () async {
      // arrange
      when(mockRemoteDataSource.scanQRCode(tGuardianId, tQrCode))
          .thenAnswer((_) async => tScanModel);

      // act
      final result = await repository.scanQRCode(tGuardianId, tQrCode);

      // assert
      verify(mockRemoteDataSource.scanQRCode(tGuardianId, tQrCode));
      expect(result.success, true);
      expect(result.card?.name, 'Fire Dragon');
    });

    test('should return error result when remote call fails', () async {
      // arrange
      when(mockRemoteDataSource.scanQRCode(any, any))
          .thenThrow(Exception('Server error'));

      // act
      final result = await repository.scanQRCode(tGuardianId, tQrCode);

      // assert
      expect(result.success, false);
      expect(result.message, contains('Error al escanear código QR'));
    });
  });

  group('getGuardianCollection', () {
     final tCardModel = CardModel(
        id: 1,
        name: 'Water Spirit',
        description: 'Calm spirit',
        imageUrl: 'url',
        element: 'WATER',
        rarity: 'COMMON',
        attackPower: 20,
        defensePower: 30,
        energyCost: 2,
        qrCode: 'qr',
        createdAt: '2025-01-01T00:00:00Z',
        active: true
     );
        
    final tCollectedCardModel = CollectedCardModel(
      card: tCardModel,
      count: 1,
      firstCollectedAt: '2025-01-01T00:00:00Z',
      lastCollectedAt: '2025-01-01T00:00:00Z',
    );

    test('should return collection with cards', () async {
      when(mockRemoteDataSource.getGuardianCards(any))
          .thenAnswer((_) async => [tCollectedCardModel]);

      final result = await repository.getGuardianCollection(tGuardianId);

      expect(result.cards.length, 1);
      expect(result.cards.first.card.name, 'Water Spirit');
      expect(result.guardianId, tGuardianId);
    });
  });
  
  group('getCollectionStatistics', () {
      final tStatsModel = CollectionStatisticsModel(
          totalCardCount: 10,
          uniqueCardCount: 8,
          completionPercentage: 25.0,
          cardCountsByElement: {'FIRE': 5, 'WATER': 3},
          cardCountsByRarity: {'COMMON': 6, 'LEGENDARY': 2},
          totalTradeValue: 100,
          hasElementalBalance: true
      );
      
      test('should return statistics entity', () async {
          when(mockRemoteDataSource.getCollectionStatistics(any))
            .thenAnswer((_) async => tStatsModel);
            
          final result = await repository.getCollectionStatistics(tGuardianId);
          
          expect(result.totalCardCount, 10);
          expect(result.uniqueCardCount, 8);
          expect(result.completionPercentage, 25.0);
      });
  });

  group('getRecentlyCollected', () {
    final tCardModel = CardModel(
      id: 1,
      name: 'Recent Card',
      description: 'Recently collected',
      imageUrl: 'url',
      element: 'FIRE',
      rarity: 'RARE',
      attackPower: 50,
      defensePower: 40,
      energyCost: 3,
      qrCode: 'qr',
      createdAt: '2025-01-01T00:00:00Z',
      active: true,
    );

    final tCollectedCardModel = CollectedCardModel(
      card: tCardModel,
      count: 1,
      firstCollectedAt: '2025-01-01T00:00:00Z',
      lastCollectedAt: '2025-01-01T00:00:00Z',
    );

    test('should return list of recently collected cards', () async {
      when(mockRemoteDataSource.getRecentlyCollected(any, any))
          .thenAnswer((_) async => [tCollectedCardModel]);

      final result = await repository.getRecentlyCollected(tGuardianId, 10);

      expect(result.length, 1);
      expect(result.first.card.name, 'Recent Card');
      verify(mockRemoteDataSource.getRecentlyCollected(tGuardianId, 10));
    });
  });

  group('getRarestCard', () {
    final tCardModel = CardModel(
      id: 1,
      name: 'Legendary Card',
      description: 'Very rare',
      imageUrl: 'url',
      element: 'FIRE',
      rarity: 'LEGENDARY',
      attackPower: 100,
      defensePower: 80,
      energyCost: 5,
      qrCode: 'qr',
      createdAt: '2025-01-01T00:00:00Z',
      active: true,
    );

    final tCollectedCardModel = CollectedCardModel(
      card: tCardModel,
      count: 1,
      firstCollectedAt: '2025-01-01T00:00:00Z',
      lastCollectedAt: '2025-01-01T00:00:00Z',
    );

    test('should return rarest card', () async {
      when(mockRemoteDataSource.getRarestCard(any))
          .thenAnswer((_) async => tCollectedCardModel);

      final result = await repository.getRarestCard(tGuardianId);

      expect(result, isNotNull);
      expect(result!.card.name, 'Legendary Card');
    });

    test('should return null when no cards', () async {
      when(mockRemoteDataSource.getRarestCard(any))
          .thenAnswer((_) async => null);

      final result = await repository.getRarestCard(tGuardianId);

      expect(result, isNull);
    });
  });

  group('searchCards', () {
    final tCardModel = CardModel(
      id: 1,
      name: 'Search Result',
      description: 'Found card',
      imageUrl: 'url',
      element: 'WATER',
      rarity: 'COMMON',
      attackPower: 30,
      defensePower: 20,
      energyCost: 2,
      qrCode: 'qr',
      createdAt: '2025-01-01T00:00:00Z',
      active: true,
    );

    test('should return search results', () async {
      when(mockRemoteDataSource.searchCards(
        name: anyNamed('name'),
        element: anyNamed('element'),
        rarity: anyNamed('rarity'),
      )).thenAnswer((_) async => [tCardModel]);

      final result = await repository.searchCards(name: 'Search');

      expect(result.length, 1);
      expect(result.first.name, 'Search Result');
    });
  });

  group('getCard', () {
    final tCardModel = CardModel(
      id: 5,
      name: 'Single Card',
      description: 'A single card',
      imageUrl: 'url',
      element: 'EARTH',
      rarity: 'UNCOMMON',
      attackPower: 40,
      defensePower: 35,
      energyCost: 3,
      qrCode: 'qr',
      createdAt: '2025-01-01T00:00:00Z',
      active: true,
    );

    test('should return card when found', () async {
      when(mockRemoteDataSource.getCard(any))
          .thenAnswer((_) async => tCardModel);

      final result = await repository.getCard(5);

      expect(result, isNotNull);
      expect(result!.id, 5);
      expect(result.name, 'Single Card');
    });

    test('should return null when not found', () async {
      when(mockRemoteDataSource.getCard(any))
          .thenAnswer((_) async => null);

      final result = await repository.getCard(999);

      expect(result, isNull);
    });
  });

  group('getGuardianCards with filters', () {
    final tCardModel = CardModel(
      id: 1,
      name: 'Filtered Card',
      description: 'Matches filter',
      imageUrl: 'url',
      element: 'FIRE',
      rarity: 'RARE',
      attackPower: 60,
      defensePower: 40,
      energyCost: 4,
      qrCode: 'qr',
      createdAt: '2025-01-01T00:00:00Z',
      active: true,
    );

    final tCollectedCardModel = CollectedCardModel(
      card: tCardModel,
      count: 2,
      firstCollectedAt: '2025-01-01T00:00:00Z',
      lastCollectedAt: '2025-01-01T00:00:00Z',
    );

    test('should return cards filtered by element', () async {
      when(mockRemoteDataSource.getGuardianCards(
        any,
        element: anyNamed('element'),
        rarity: anyNamed('rarity'),
      )).thenAnswer((_) async => [tCollectedCardModel]);

      final result = await repository.getGuardianCards(
        tGuardianId,
        element: CardElement.fire,
      );

      expect(result.length, 1);
      expect(result.first.card.name, 'Filtered Card');
    });
  });
}


import '../../domain/entities/card.dart';
import '../../domain/entities/card_collection.dart';
import '../../domain/entities/card_scan_result.dart';
import '../../domain/entities/collected_card.dart';
import '../../domain/entities/collection_statistics.dart';
import '../../domain/repositories/card_repository.dart';
import '../datasources/card_remote_datasource.dart';

class CardRepositoryImpl implements CardRepository {
  final CardRemoteDataSource remoteDataSource;

  const CardRepositoryImpl({
    required this.remoteDataSource,
  });

  @override
  Future<CardScanResult> scanQRCode(int guardianId, String qrCode) async {
    try {
      final result = await remoteDataSource.scanQRCode(guardianId, qrCode);
      return result.toEntity();
    } catch (e) {
      return CardScanResult.error('Error al escanear código QR: $e');
    }
  }

  @override
  Future<CardCollection> getGuardianCollection(int guardianId) async {
    final cards = await remoteDataSource.getGuardianCards(guardianId);
    final collectedCards = cards.map((model) => model.toEntity()).toList();
    
    return CardCollection(
      guardianId: guardianId,
      cards: collectedCards,
      createdAt: DateTime.now(), // This would come from API in real implementation
    );
  }

  @override
  Future<List<CollectedCard>> getGuardianCards(
    int guardianId, {
    CardElement? element,
    CardRarity? rarity,
  }) async {
    final cards = await remoteDataSource.getGuardianCards(
      guardianId,
      element: element,
      rarity: rarity,
    );
    return cards.map((model) => model.toEntity()).toList();
  }

  @override
  Future<CollectionStatistics> getCollectionStatistics(int guardianId) async {
    final stats = await remoteDataSource.getCollectionStatistics(guardianId);
    return stats.toEntity();
  }

  @override
  Future<List<CollectedCard>> getRecentlyCollected(int guardianId, int limit) async {
    final cards = await remoteDataSource.getRecentlyCollected(guardianId, limit);
    return cards.map((model) => model.toEntity()).toList();
  }

  @override
  Future<CollectedCard?> getRarestCard(int guardianId) async {
    final card = await remoteDataSource.getRarestCard(guardianId);
    return card?.toEntity();
  }

  @override
  Future<List<Card>> searchCards({
    String? name,
    CardElement? element,
    CardRarity? rarity,
  }) async {
    final cards = await remoteDataSource.searchCards(
      name: name,
      element: element,
      rarity: rarity,
    );
    return cards.map((model) => model.toEntity()).toList();
  }

  @override
  Future<Card?> getCard(int cardId) async {
    final card = await remoteDataSource.getCard(cardId);
    return card?.toEntity();
  }
}
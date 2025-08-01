import '../entities/card.dart';
import '../entities/card_collection.dart';
import '../entities/card_scan_result.dart';
import '../entities/collected_card.dart';
import '../entities/collection_statistics.dart';

abstract class CardRepository {
  /// Scan a QR code and attempt to collect the card
  Future<CardScanResult> scanQRCode(int guardianId, String qrCode);

  /// Get a guardian's complete card collection
  Future<CardCollection> getGuardianCollection(int guardianId);

  /// Get cards in a guardian's collection with optional filtering
  Future<List<CollectedCard>> getGuardianCards(
    int guardianId, {
    CardElement? element,
    CardRarity? rarity,
  });

  /// Get collection statistics for a guardian
  Future<CollectionStatistics> getCollectionStatistics(int guardianId);

  /// Get recently collected cards
  Future<List<CollectedCard>> getRecentlyCollected(int guardianId, int limit);

  /// Get the rarest card in a guardian's collection
  Future<CollectedCard?> getRarestCard(int guardianId);

  /// Search for cards by name, element, or rarity
  Future<List<Card>> searchCards({
    String? name,
    CardElement? element,
    CardRarity? rarity,
  });

  /// Get a specific card by ID
  Future<Card?> getCard(int cardId);
}

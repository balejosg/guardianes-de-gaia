import 'package:equatable/equatable.dart';
import 'card.dart';
import 'collected_card.dart';

class CardCollection extends Equatable {
  final int guardianId;
  final List<CollectedCard> cards;
  final DateTime createdAt;

  const CardCollection({
    required this.guardianId,
    required this.cards,
    required this.createdAt,
  });

  int get uniqueCardCount => cards.length;

  int get totalCardCount => cards.fold(0, (sum, card) => sum + card.count);

  List<CollectedCard> getCardsByElement(CardElement element) {
    return cards.where((card) => card.card.element == element).toList();
  }

  List<CollectedCard> getCardsByRarity(CardRarity rarity) {
    return cards.where((card) => card.card.rarity == rarity).toList();
  }

  Map<CardElement, int> get cardCountsByElement {
    final Map<CardElement, int> counts = {};
    for (final element in CardElement.values) {
      counts[element] = 0;
    }

    for (final collectedCard in cards) {
      counts[collectedCard.card.element] =
          (counts[collectedCard.card.element] ?? 0) + collectedCard.count;
    }

    return counts;
  }

  Map<CardRarity, int> get cardCountsByRarity {
    final Map<CardRarity, int> counts = {};
    for (final rarity in CardRarity.values) {
      counts[rarity] = 0;
    }

    for (final collectedCard in cards) {
      counts[collectedCard.card.rarity] =
          (counts[collectedCard.card.rarity] ?? 0) + collectedCard.count;
    }

    return counts;
  }

  bool get hasElementalBalance {
    final elementCounts = cardCountsByElement;
    return CardElement.values.every((element) =>
        elementCounts[element] != null && elementCounts[element]! > 0);
  }

  int get totalTradeValue {
    return cards.fold(
        0,
        (sum, collectedCard) =>
            sum + (collectedCard.card.rarity.tradeValue * collectedCard.count));
  }

  CollectedCard? get rarestCard {
    if (cards.isEmpty) return null;

    return cards.reduce((curr, next) =>
        curr.card.rarity.index > next.card.rarity.index ? curr : next);
  }

  List<CollectedCard> getRecentlyCollected([int limit = 10]) {
    final sortedCards = List<CollectedCard>.from(cards);
    sortedCards.sort((a, b) => b.lastCollectedAt.compareTo(a.lastCollectedAt));
    return sortedCards.take(limit).toList();
  }

  double getCompletionPercentage(int totalAvailableCards) {
    if (totalAvailableCards <= 0) return 0.0;
    return (uniqueCardCount / totalAvailableCards) * 100.0;
  }

  @override
  List<Object?> get props => [guardianId, cards, createdAt];
}

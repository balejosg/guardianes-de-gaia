import 'package:equatable/equatable.dart';
import 'card.dart';

class CollectionStatistics extends Equatable {
  final int uniqueCardCount;
  final int totalCardCount;
  final double completionPercentage;
  final Map<CardElement, int> cardCountsByElement;
  final Map<CardRarity, int> cardCountsByRarity;
  final int totalTradeValue;
  final bool hasElementalBalance;

  const CollectionStatistics({
    required this.uniqueCardCount,
    required this.totalCardCount,
    required this.completionPercentage,
    required this.cardCountsByElement,
    required this.cardCountsByRarity,
    required this.totalTradeValue,
    required this.hasElementalBalance,
  });

  @override
  List<Object?> get props => [
        uniqueCardCount,
        totalCardCount,
        completionPercentage,
        cardCountsByElement,
        cardCountsByRarity,
        totalTradeValue,
        hasElementalBalance,
      ];
}

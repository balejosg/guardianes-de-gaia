import 'package:json_annotation/json_annotation.dart';
import '../../domain/entities/collection_statistics.dart';
import '../../domain/entities/card.dart';

part 'collection_statistics_model.g.dart';

@JsonSerializable()
class CollectionStatisticsModel {
  final int uniqueCardCount;
  final int totalCardCount;
  final double completionPercentage;
  final Map<String, int> cardCountsByElement;
  final Map<String, int> cardCountsByRarity;
  final int totalTradeValue;
  final bool hasElementalBalance;

  const CollectionStatisticsModel({
    required this.uniqueCardCount,
    required this.totalCardCount,
    required this.completionPercentage,
    required this.cardCountsByElement,
    required this.cardCountsByRarity,
    required this.totalTradeValue,
    required this.hasElementalBalance,
  });

  factory CollectionStatisticsModel.fromJson(Map<String, dynamic> json) =>
      _$CollectionStatisticsModelFromJson(json);

  Map<String, dynamic> toJson() => _$CollectionStatisticsModelToJson(this);

  CollectionStatistics toEntity() {
    return CollectionStatistics(
      uniqueCardCount: uniqueCardCount,
      totalCardCount: totalCardCount,
      completionPercentage: completionPercentage,
      cardCountsByElement: _parseElementCounts(cardCountsByElement),
      cardCountsByRarity: _parseRarityCounts(cardCountsByRarity),
      totalTradeValue: totalTradeValue,
      hasElementalBalance: hasElementalBalance,
    );
  }

  Map<CardElement, int> _parseElementCounts(Map<String, int> stringMap) {
    final result = <CardElement, int>{};
    stringMap.forEach((key, value) {
      switch (key.toUpperCase()) {
        case 'FIRE':
          result[CardElement.fire] = value;
          break;
        case 'EARTH':
          result[CardElement.earth] = value;
          break;
        case 'WATER':
          result[CardElement.water] = value;
          break;
        case 'AIR':
          result[CardElement.air] = value;
          break;
      }
    });
    return result;
  }

  Map<CardRarity, int> _parseRarityCounts(Map<String, int> stringMap) {
    final result = <CardRarity, int>{};
    stringMap.forEach((key, value) {
      switch (key.toUpperCase()) {
        case 'COMMON':
          result[CardRarity.common] = value;
          break;
        case 'UNCOMMON':
          result[CardRarity.uncommon] = value;
          break;
        case 'RARE':
          result[CardRarity.rare] = value;
          break;
        case 'EPIC':
          result[CardRarity.epic] = value;
          break;
        case 'LEGENDARY':
          result[CardRarity.legendary] = value;
          break;
      }
    });
    return result;
  }
}
// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'collection_statistics_model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CollectionStatisticsModel _$CollectionStatisticsModelFromJson(
        Map<String, dynamic> json) =>
    CollectionStatisticsModel(
      uniqueCardCount: (json['uniqueCardCount'] as num).toInt(),
      totalCardCount: (json['totalCardCount'] as num).toInt(),
      completionPercentage: (json['completionPercentage'] as num).toDouble(),
      cardCountsByElement:
          Map<String, int>.from(json['cardCountsByElement'] as Map),
      cardCountsByRarity:
          Map<String, int>.from(json['cardCountsByRarity'] as Map),
      totalTradeValue: (json['totalTradeValue'] as num).toInt(),
      hasElementalBalance: json['hasElementalBalance'] as bool,
    );

Map<String, dynamic> _$CollectionStatisticsModelToJson(
        CollectionStatisticsModel instance) =>
    <String, dynamic>{
      'uniqueCardCount': instance.uniqueCardCount,
      'totalCardCount': instance.totalCardCount,
      'completionPercentage': instance.completionPercentage,
      'cardCountsByElement': instance.cardCountsByElement,
      'cardCountsByRarity': instance.cardCountsByRarity,
      'totalTradeValue': instance.totalTradeValue,
      'hasElementalBalance': instance.hasElementalBalance,
    };

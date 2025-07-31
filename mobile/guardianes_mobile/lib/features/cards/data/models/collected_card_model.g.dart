// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'collected_card_model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CollectedCardModel _$CollectedCardModelFromJson(Map<String, dynamic> json) =>
    CollectedCardModel(
      card: CardModel.fromJson(json['card'] as Map<String, dynamic>),
      count: (json['count'] as num).toInt(),
      firstCollectedAt: json['firstCollectedAt'] as String,
      lastCollectedAt: json['lastCollectedAt'] as String,
    );

Map<String, dynamic> _$CollectedCardModelToJson(CollectedCardModel instance) =>
    <String, dynamic>{
      'card': instance.card,
      'count': instance.count,
      'firstCollectedAt': instance.firstCollectedAt,
      'lastCollectedAt': instance.lastCollectedAt,
    };

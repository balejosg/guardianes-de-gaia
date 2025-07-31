// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'card_model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CardModel _$CardModelFromJson(Map<String, dynamic> json) => CardModel(
      id: (json['id'] as num).toInt(),
      name: json['name'] as String,
      description: json['description'] as String,
      element: json['element'] as String,
      rarity: json['rarity'] as String,
      attackPower: (json['attackPower'] as num).toInt(),
      defensePower: (json['defensePower'] as num).toInt(),
      energyCost: (json['energyCost'] as num).toInt(),
      imageUrl: json['imageUrl'] as String?,
      qrCode: json['qrCode'] as String,
      nfcCode: json['nfcCode'] as String?,
      createdAt: json['createdAt'] as String,
      active: json['active'] as bool,
    );

Map<String, dynamic> _$CardModelToJson(CardModel instance) => <String, dynamic>{
      'id': instance.id,
      'name': instance.name,
      'description': instance.description,
      'element': instance.element,
      'rarity': instance.rarity,
      'attackPower': instance.attackPower,
      'defensePower': instance.defensePower,
      'energyCost': instance.energyCost,
      'imageUrl': instance.imageUrl,
      'qrCode': instance.qrCode,
      'nfcCode': instance.nfcCode,
      'createdAt': instance.createdAt,
      'active': instance.active,
    };

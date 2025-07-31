// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'card_scan_result_model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CardScanResultModel _$CardScanResultModelFromJson(Map<String, dynamic> json) =>
    CardScanResultModel(
      success: json['success'] as bool,
      message: json['message'] as String,
      card: json['card'] == null
          ? null
          : CardModel.fromJson(json['card'] as Map<String, dynamic>),
      count: (json['count'] as num?)?.toInt(),
      isNew: json['new'] as bool,
    );

Map<String, dynamic> _$CardScanResultModelToJson(
        CardScanResultModel instance) =>
    <String, dynamic>{
      'success': instance.success,
      'message': instance.message,
      'card': instance.card,
      'count': instance.count,
      'new': instance.isNew,
    };

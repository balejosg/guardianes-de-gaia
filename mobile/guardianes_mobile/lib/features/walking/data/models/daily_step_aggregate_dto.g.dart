// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'daily_step_aggregate_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DailyStepAggregateDto _$DailyStepAggregateDtoFromJson(
        Map<String, dynamic> json) =>
    DailyStepAggregateDto(
      guardianId: (json['guardianId'] as num).toInt(),
      date: json['date'] as String,
      totalSteps: (json['totalSteps'] as num).toInt(),
    );

Map<String, dynamic> _$DailyStepAggregateDtoToJson(
        DailyStepAggregateDto instance) =>
    <String, dynamic>{
      'guardianId': instance.guardianId,
      'date': instance.date,
      'totalSteps': instance.totalSteps,
    };

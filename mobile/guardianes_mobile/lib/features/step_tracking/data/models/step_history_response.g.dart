// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'step_history_response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StepHistoryEntry _$StepHistoryEntryFromJson(Map<String, dynamic> json) =>
    StepHistoryEntry(
      date: DateTime.parse(json['date'] as String),
      stepCount: (json['stepCount'] as num).toInt(),
      energyEarned: (json['energyEarned'] as num).toInt(),
    );

Map<String, dynamic> _$StepHistoryEntryToJson(StepHistoryEntry instance) =>
    <String, dynamic>{
      'date': instance.date.toIso8601String(),
      'stepCount': instance.stepCount,
      'energyEarned': instance.energyEarned,
    };

StepHistoryResponse _$StepHistoryResponseFromJson(Map<String, dynamic> json) =>
    StepHistoryResponse(
      history: StepHistoryResponse._parseHistoryEntries(json['dailySteps']),
    );

Map<String, dynamic> _$StepHistoryResponseToJson(
        StepHistoryResponse instance) =>
    <String, dynamic>{
      'dailySteps': instance.history,
    };

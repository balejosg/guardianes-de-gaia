// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'current_step_count_response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CurrentStepCountResponse _$CurrentStepCountResponseFromJson(
        Map<String, dynamic> json) =>
    CurrentStepCountResponse(
      currentSteps: (json['currentSteps'] as num).toInt(),
      totalEnergy: (json['availableEnergy'] as num).toInt(),
      lastUpdated: json['date'] as String,
    );

Map<String, dynamic> _$CurrentStepCountResponseToJson(
        CurrentStepCountResponse instance) =>
    <String, dynamic>{
      'currentSteps': instance.currentSteps,
      'availableEnergy': instance.totalEnergy,
      'date': instance.lastUpdated,
    };

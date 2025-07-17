// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'current_step_count_response_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CurrentStepCountResponseDto _$CurrentStepCountResponseDtoFromJson(
        Map<String, dynamic> json) =>
    CurrentStepCountResponseDto(
      guardianId: (json['guardianId'] as num).toInt(),
      currentSteps: (json['currentSteps'] as num).toInt(),
      availableEnergy: (json['availableEnergy'] as num).toInt(),
      date: json['date'] as String,
    );

Map<String, dynamic> _$CurrentStepCountResponseDtoToJson(
        CurrentStepCountResponseDto instance) =>
    <String, dynamic>{
      'guardianId': instance.guardianId,
      'currentSteps': instance.currentSteps,
      'availableEnergy': instance.availableEnergy,
      'date': instance.date,
    };

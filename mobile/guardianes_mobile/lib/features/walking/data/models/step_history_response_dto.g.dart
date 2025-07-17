// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'step_history_response_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StepHistoryResponseDto _$StepHistoryResponseDtoFromJson(
        Map<String, dynamic> json) =>
    StepHistoryResponseDto(
      guardianId: (json['guardianId'] as num).toInt(),
      dailySteps: (json['dailySteps'] as List<dynamic>)
          .map((e) => DailyStepAggregateDto.fromJson(e as Map<String, dynamic>))
          .toList(),
    );

Map<String, dynamic> _$StepHistoryResponseDtoToJson(
        StepHistoryResponseDto instance) =>
    <String, dynamic>{
      'guardianId': instance.guardianId,
      'dailySteps': instance.dailySteps,
    };

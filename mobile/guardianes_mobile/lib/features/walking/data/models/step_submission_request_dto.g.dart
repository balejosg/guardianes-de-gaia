// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'step_submission_request_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StepSubmissionRequestDto _$StepSubmissionRequestDtoFromJson(
        Map<String, dynamic> json) =>
    StepSubmissionRequestDto(
      stepCount: (json['stepCount'] as num).toInt(),
      timestamp: json['timestamp'] as String,
    );

Map<String, dynamic> _$StepSubmissionRequestDtoToJson(
        StepSubmissionRequestDto instance) =>
    <String, dynamic>{
      'stepCount': instance.stepCount,
      'timestamp': instance.timestamp,
    };

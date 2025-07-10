// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'step_submission_request.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StepSubmissionRequest _$StepSubmissionRequestFromJson(
        Map<String, dynamic> json) =>
    StepSubmissionRequest(
      guardianId: json['guardianId'] as String,
      stepCount: (json['stepCount'] as num).toInt(),
      timestamp: DateTime.parse(json['timestamp'] as String),
      source: json['source'] as String?,
    );

Map<String, dynamic> _$StepSubmissionRequestToJson(
        StepSubmissionRequest instance) =>
    <String, dynamic>{
      'guardianId': instance.guardianId,
      'stepCount': instance.stepCount,
      'timestamp': instance.timestamp.toIso8601String(),
      'source': instance.source,
    };

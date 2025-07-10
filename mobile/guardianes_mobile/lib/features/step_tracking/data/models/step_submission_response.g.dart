// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'step_submission_response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StepSubmissionResponse _$StepSubmissionResponseFromJson(
        Map<String, dynamic> json) =>
    StepSubmissionResponse(
      success: json['success'] as bool? ?? true,
      message: json['message'] as String,
      energyEarned: (json['energyEarned'] as num).toInt(),
      totalSteps: (json['totalDailySteps'] as num).toInt(),
    );

Map<String, dynamic> _$StepSubmissionResponseToJson(
        StepSubmissionResponse instance) =>
    <String, dynamic>{
      'success': instance.success,
      'message': instance.message,
      'energyEarned': instance.energyEarned,
      'totalDailySteps': instance.totalSteps,
    };

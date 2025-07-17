// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'step_submission_response_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StepSubmissionResponseDto _$StepSubmissionResponseDtoFromJson(
        Map<String, dynamic> json) =>
    StepSubmissionResponseDto(
      guardianId: (json['guardianId'] as num).toInt(),
      totalDailySteps: (json['totalDailySteps'] as num).toInt(),
      energyEarned: (json['energyEarned'] as num).toInt(),
      message: json['message'] as String,
    );

Map<String, dynamic> _$StepSubmissionResponseDtoToJson(
        StepSubmissionResponseDto instance) =>
    <String, dynamic>{
      'guardianId': instance.guardianId,
      'totalDailySteps': instance.totalDailySteps,
      'energyEarned': instance.energyEarned,
      'message': instance.message,
    };

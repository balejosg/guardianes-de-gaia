import 'package:json_annotation/json_annotation.dart';
import 'package:equatable/equatable.dart';

part 'step_submission_response_dto.g.dart';

@JsonSerializable()
class StepSubmissionResponseDto extends Equatable {
  final int guardianId;
  final int totalDailySteps;
  final int energyEarned;
  final String message;

  const StepSubmissionResponseDto({
    required this.guardianId,
    required this.totalDailySteps,
    required this.energyEarned,
    required this.message,
  });

  factory StepSubmissionResponseDto.fromJson(Map<String, dynamic> json) =>
      _$StepSubmissionResponseDtoFromJson(json);

  Map<String, dynamic> toJson() => _$StepSubmissionResponseDtoToJson(this);

  @override
  List<Object?> get props =>
      [guardianId, totalDailySteps, energyEarned, message];
}

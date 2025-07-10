import 'package:json_annotation/json_annotation.dart';

part 'step_submission_response.g.dart';

@JsonSerializable()
class StepSubmissionResponse {
  @JsonKey(defaultValue: true)
  final bool success;
  final String message;
  final int energyEarned;
  @JsonKey(name: 'totalDailySteps')
  final int totalSteps;

  const StepSubmissionResponse({
    required this.success,
    required this.message,
    required this.energyEarned,
    required this.totalSteps,
  });

  factory StepSubmissionResponse.fromJson(Map<String, dynamic> json) =>
      _$StepSubmissionResponseFromJson(json);

  Map<String, dynamic> toJson() => _$StepSubmissionResponseToJson(this);
}
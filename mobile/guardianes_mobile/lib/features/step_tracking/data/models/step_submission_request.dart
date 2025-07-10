import 'package:json_annotation/json_annotation.dart';

part 'step_submission_request.g.dart';

@JsonSerializable()
class StepSubmissionRequest {
  final String guardianId;
  final int stepCount;
  final DateTime timestamp;
  final String? source;

  const StepSubmissionRequest({
    required this.guardianId,
    required this.stepCount,
    required this.timestamp,
    this.source,
  });

  factory StepSubmissionRequest.fromJson(Map<String, dynamic> json) =>
      _$StepSubmissionRequestFromJson(json);

  Map<String, dynamic> toJson() => _$StepSubmissionRequestToJson(this);
}
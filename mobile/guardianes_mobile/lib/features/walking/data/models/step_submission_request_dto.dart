import 'package:json_annotation/json_annotation.dart';
import 'package:equatable/equatable.dart';

part 'step_submission_request_dto.g.dart';

@JsonSerializable()
class StepSubmissionRequestDto extends Equatable {
  final int stepCount;
  final String timestamp;

  const StepSubmissionRequestDto({
    required this.stepCount,
    required this.timestamp,
  });

  factory StepSubmissionRequestDto.fromJson(Map<String, dynamic> json) =>
      _$StepSubmissionRequestDtoFromJson(json);

  Map<String, dynamic> toJson() => _$StepSubmissionRequestDtoToJson(this);

  @override
  List<Object?> get props => [stepCount, timestamp];
}
import 'package:json_annotation/json_annotation.dart';

part 'current_step_count_response.g.dart';

@JsonSerializable()
class CurrentStepCountResponse {
  final int currentSteps;
  @JsonKey(name: 'availableEnergy')
  final int totalEnergy;
  @JsonKey(name: 'date')
  final String lastUpdated;

  const CurrentStepCountResponse({
    required this.currentSteps,
    required this.totalEnergy,
    required this.lastUpdated,
  });

  factory CurrentStepCountResponse.fromJson(Map<String, dynamic> json) =>
      _$CurrentStepCountResponseFromJson(json);

  Map<String, dynamic> toJson() => _$CurrentStepCountResponseToJson(this);
}
import 'package:json_annotation/json_annotation.dart';
import 'package:equatable/equatable.dart';
import 'package:guardianes_mobile/features/walking/domain/entities/daily_step_aggregate.dart';

part 'current_step_count_response_dto.g.dart';

@JsonSerializable()
class CurrentStepCountResponseDto extends Equatable {
  final int guardianId;
  final int currentSteps;
  final int availableEnergy;
  final String date;

  const CurrentStepCountResponseDto({
    required this.guardianId,
    required this.currentSteps,
    required this.availableEnergy,
    required this.date,
  });

  factory CurrentStepCountResponseDto.fromJson(Map<String, dynamic> json) =>
      _$CurrentStepCountResponseDtoFromJson(json);

  Map<String, dynamic> toJson() => _$CurrentStepCountResponseDtoToJson(this);

  /// Convert to domain entity
  DailyStepAggregate toDomainEntity() {
    return DailyStepAggregate(
      guardianId: guardianId,
      date: date,
      totalSteps: currentSteps,
    );
  }

  @override
  List<Object?> get props => [guardianId, currentSteps, availableEnergy, date];
}
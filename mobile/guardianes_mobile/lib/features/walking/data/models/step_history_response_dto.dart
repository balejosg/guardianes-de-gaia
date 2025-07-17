import 'package:json_annotation/json_annotation.dart';
import 'package:equatable/equatable.dart';
import 'package:guardianes_mobile/features/walking/data/models/daily_step_aggregate_dto.dart';
import 'package:guardianes_mobile/features/walking/domain/entities/daily_step_aggregate.dart';

part 'step_history_response_dto.g.dart';

@JsonSerializable()
class StepHistoryResponseDto extends Equatable {
  final int guardianId;
  final List<DailyStepAggregateDto> dailySteps;

  const StepHistoryResponseDto({
    required this.guardianId,
    required this.dailySteps,
  });

  factory StepHistoryResponseDto.fromJson(Map<String, dynamic> json) =>
      _$StepHistoryResponseDtoFromJson(json);

  Map<String, dynamic> toJson() => _$StepHistoryResponseDtoToJson(this);

  /// Convert to domain entities
  List<DailyStepAggregate> toDomainEntities() {
    return dailySteps.map((dto) => dto.toDomainEntity()).toList();
  }

  @override
  List<Object?> get props => [guardianId, dailySteps];
}
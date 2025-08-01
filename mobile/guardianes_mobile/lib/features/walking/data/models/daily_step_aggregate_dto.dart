import 'package:json_annotation/json_annotation.dart';
import 'package:equatable/equatable.dart';
import 'package:guardianes_mobile/features/walking/domain/entities/daily_step_aggregate.dart';

part 'daily_step_aggregate_dto.g.dart';

@JsonSerializable()
class DailyStepAggregateDto extends Equatable {
  final int guardianId;
  final String date;
  final int totalSteps;

  const DailyStepAggregateDto({
    required this.guardianId,
    required this.date,
    required this.totalSteps,
  });

  factory DailyStepAggregateDto.fromJson(Map<String, dynamic> json) =>
      _$DailyStepAggregateDtoFromJson(json);

  Map<String, dynamic> toJson() => _$DailyStepAggregateDtoToJson(this);

  /// Convert to domain entity
  DailyStepAggregate toDomainEntity() {
    return DailyStepAggregate(
      guardianId: guardianId,
      date: date,
      totalSteps: totalSteps,
    );
  }

  @override
  List<Object?> get props => [guardianId, date, totalSteps];
}

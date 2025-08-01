import 'package:json_annotation/json_annotation.dart';
import 'package:guardianes_mobile/features/auth/domain/entities/guardian.dart';

part 'guardian_model.g.dart';

@JsonSerializable()
class GuardianModel extends Guardian {
  const GuardianModel({
    required super.id,
    required super.username,
    required super.email,
    required super.name,
    required super.birthDate,
    required super.age,
    required super.level,
    required super.experiencePoints,
    required super.experienceToNextLevel,
    required super.totalSteps,
    required super.totalEnergyGenerated,
    required super.createdAt,
    required super.lastActiveAt,
    required super.isChild,
  });

  factory GuardianModel.fromJson(Map<String, dynamic> json) =>
      _$GuardianModelFromJson(json);

  Map<String, dynamic> toJson() => _$GuardianModelToJson(this);
}

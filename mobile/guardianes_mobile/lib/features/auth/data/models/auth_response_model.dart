import 'package:json_annotation/json_annotation.dart';
import 'package:guardianes_mobile/features/auth/data/models/guardian_model.dart';
import 'package:guardianes_mobile/features/auth/domain/entities/auth_result.dart';

part 'auth_response_model.g.dart';

@JsonSerializable(explicitToJson: true)
class AuthResponseModel extends AuthResult {
  @JsonKey(name: 'guardian')
  final GuardianModel guardianModel;

  const AuthResponseModel({
    required super.token,
    required this.guardianModel,
  }) : super(guardian: guardianModel);

  factory AuthResponseModel.fromJson(Map<String, dynamic> json) =>
      _$AuthResponseModelFromJson(json);

  Map<String, dynamic> toJson() => _$AuthResponseModelToJson(this);
}
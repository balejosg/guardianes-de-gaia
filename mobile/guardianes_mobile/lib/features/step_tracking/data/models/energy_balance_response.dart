import 'package:json_annotation/json_annotation.dart';

part 'energy_balance_response.g.dart';

@JsonSerializable()
class EnergyBalanceResponse {
  @JsonKey(name: 'currentBalance', fromJson: _extractAmount)
  final int currentBalance;
  @JsonKey(includeFromJson: false, includeToJson: false)
  final int totalEarned;
  @JsonKey(includeFromJson: false, includeToJson: false)
  final int totalSpent;
  @JsonKey(includeFromJson: false, includeToJson: false)
  final DateTime lastUpdated;

  EnergyBalanceResponse({
    required this.currentBalance,
    this.totalEarned = 0,
    this.totalSpent = 0,
    DateTime? lastUpdated,
  }) : lastUpdated = lastUpdated ?? DateTime.now();

  
  static int _extractAmount(dynamic value) {
    if (value is Map<String, dynamic> && value.containsKey('amount')) {
      return value['amount'] as int;
    }
    return value as int;
  }

  factory EnergyBalanceResponse.fromJson(Map<String, dynamic> json) =>
      _$EnergyBalanceResponseFromJson(json);

  Map<String, dynamic> toJson() => _$EnergyBalanceResponseToJson(this);
}
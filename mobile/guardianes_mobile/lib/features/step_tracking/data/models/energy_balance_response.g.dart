// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'energy_balance_response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EnergyBalanceResponse _$EnergyBalanceResponseFromJson(
        Map<String, dynamic> json) =>
    EnergyBalanceResponse(
      currentBalance:
          EnergyBalanceResponse._extractAmount(json['currentBalance']),
    );

Map<String, dynamic> _$EnergyBalanceResponseToJson(
        EnergyBalanceResponse instance) =>
    <String, dynamic>{
      'currentBalance': instance.currentBalance,
    };

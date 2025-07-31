// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'qr_scan_request_model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

QRScanRequestModel _$QRScanRequestModelFromJson(Map<String, dynamic> json) =>
    QRScanRequestModel(
      guardianId: (json['guardianId'] as num).toInt(),
      qrCode: json['qrCode'] as String,
    );

Map<String, dynamic> _$QRScanRequestModelToJson(QRScanRequestModel instance) =>
    <String, dynamic>{
      'guardianId': instance.guardianId,
      'qrCode': instance.qrCode,
    };

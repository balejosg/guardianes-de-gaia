import 'package:json_annotation/json_annotation.dart';

part 'qr_scan_request_model.g.dart';

@JsonSerializable()
class QRScanRequestModel {
  final int guardianId;
  final String qrCode;

  const QRScanRequestModel({
    required this.guardianId,
    required this.qrCode,
  });

  factory QRScanRequestModel.fromJson(Map<String, dynamic> json) =>
      _$QRScanRequestModelFromJson(json);

  Map<String, dynamic> toJson() => _$QRScanRequestModelToJson(this);
}
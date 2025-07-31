import 'package:json_annotation/json_annotation.dart';
import '../../domain/entities/card_scan_result.dart';
import 'card_model.dart';

part 'card_scan_result_model.g.dart';

@JsonSerializable()
class CardScanResultModel {
  final bool success;
  final String message;
  final CardModel? card;
  final int? count;
  @JsonKey(name: 'new')
  final bool isNew;

  const CardScanResultModel({
    required this.success,
    required this.message,
    this.card,
    this.count,
    required this.isNew,
  });

  factory CardScanResultModel.fromJson(Map<String, dynamic> json) =>
      _$CardScanResultModelFromJson(json);

  Map<String, dynamic> toJson() => _$CardScanResultModelToJson(this);

  CardScanResult toEntity() {
    return CardScanResult(
      success: success,
      message: message,
      card: card?.toEntity(),
      count: count,
      isNew: isNew,
    );
  }
}
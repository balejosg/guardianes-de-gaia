import 'package:json_annotation/json_annotation.dart';
import '../../domain/entities/collected_card.dart';
import 'card_model.dart';

part 'collected_card_model.g.dart';

@JsonSerializable()
class CollectedCardModel {
  final CardModel card;
  final int count;
  final String firstCollectedAt;
  final String lastCollectedAt;

  const CollectedCardModel({
    required this.card,
    required this.count,
    required this.firstCollectedAt,
    required this.lastCollectedAt,
  });

  factory CollectedCardModel.fromJson(Map<String, dynamic> json) =>
      _$CollectedCardModelFromJson(json);

  Map<String, dynamic> toJson() => _$CollectedCardModelToJson(this);

  CollectedCard toEntity() {
    return CollectedCard(
      card: card.toEntity(),
      count: count,
      firstCollectedAt: DateTime.parse(firstCollectedAt),
      lastCollectedAt: DateTime.parse(lastCollectedAt),
    );
  }
}

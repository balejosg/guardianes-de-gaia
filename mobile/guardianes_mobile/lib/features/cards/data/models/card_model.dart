import 'package:json_annotation/json_annotation.dart';
import '../../domain/entities/card.dart';

part 'card_model.g.dart';

@JsonSerializable()
class CardModel {
  final int id;
  final String name;
  final String description;
  final String element;
  final String rarity;
  final int attackPower;
  final int defensePower;
  final int energyCost;
  final String? imageUrl;
  final String qrCode;
  final String? nfcCode;
  final String createdAt;
  final bool active;

  const CardModel({
    required this.id,
    required this.name,
    required this.description,
    required this.element,
    required this.rarity,
    required this.attackPower,
    required this.defensePower,
    required this.energyCost,
    this.imageUrl,
    required this.qrCode,
    this.nfcCode,
    required this.createdAt,
    required this.active,
  });

  factory CardModel.fromJson(Map<String, dynamic> json) =>
      _$CardModelFromJson(json);

  Map<String, dynamic> toJson() => _$CardModelToJson(this);

  Card toEntity() {
    return Card(
      id: id,
      name: name,
      description: description,
      element: _parseElement(element),
      rarity: _parseRarity(rarity),
      attackPower: attackPower,
      defensePower: defensePower,
      energyCost: energyCost,
      imageUrl: imageUrl,
      qrCode: qrCode,
      nfcCode: nfcCode,
      createdAt: DateTime.parse(createdAt),
      isActive: active,
    );
  }

  static CardElement _parseElement(String element) {
    switch (element.toUpperCase()) {
      case 'FIRE':
        return CardElement.fire;
      case 'EARTH':
        return CardElement.earth;
      case 'WATER':
        return CardElement.water;
      case 'AIR':
        return CardElement.air;
      default:
        throw ArgumentError('Unknown element: $element');
    }
  }

  static CardRarity _parseRarity(String rarity) {
    switch (rarity.toUpperCase()) {
      case 'COMMON':
        return CardRarity.common;
      case 'UNCOMMON':
        return CardRarity.uncommon;
      case 'RARE':
        return CardRarity.rare;
      case 'EPIC':
        return CardRarity.epic;
      case 'LEGENDARY':
        return CardRarity.legendary;
      default:
        throw ArgumentError('Unknown rarity: $rarity');
    }
  }
}

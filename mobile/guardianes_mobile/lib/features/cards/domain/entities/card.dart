import 'package:equatable/equatable.dart';

enum CardElement { fire, earth, water, air }

enum CardRarity { common, uncommon, rare, epic, legendary }

class Card extends Equatable {
  final int id;
  final String name;
  final String description;
  final CardElement element;
  final CardRarity rarity;
  final int attackPower;
  final int defensePower;
  final int energyCost;
  final String? imageUrl;
  final String qrCode;
  final String? nfcCode;
  final DateTime createdAt;
  final bool isActive;

  const Card({
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
    required this.isActive,
  });

  bool get isPremium => nfcCode != null && nfcCode!.isNotEmpty;

  int get totalPower => attackPower + defensePower;

  bool canBePlayedWith(int availableEnergy) {
    return availableEnergy >= energyCost;
  }

  bool hasElementalAdvantageOver(Card opponent) {
    const advantageMap = {
      CardElement.fire: CardElement.earth,
      CardElement.earth: CardElement.water,
      CardElement.water: CardElement.air,
      CardElement.air: CardElement.fire,
    };
    return advantageMap[element] == opponent.element;
  }

  int calculateDamageAgainst(Card opponent) {
    int baseDamage = attackPower - opponent.defensePower;
    baseDamage = baseDamage < 1 ? 1 : baseDamage; // Minimum 1 damage

    if (hasElementalAdvantageOver(opponent)) {
      baseDamage = (baseDamage * 1.5).round(); // 50% bonus damage
    }

    return baseDamage;
  }

  @override
  List<Object?> get props => [
        id,
        name,
        element,
        rarity,
        attackPower,
        defensePower,
        energyCost,
        qrCode,
        isActive,
      ];
}

extension CardElementExtension on CardElement {
  String get displayName {
    switch (this) {
      case CardElement.fire:
        return 'Fuego';
      case CardElement.earth:
        return 'Tierra';
      case CardElement.water:
        return 'Agua';
      case CardElement.air:
        return 'Aire';
    }
  }

  String get emoji {
    switch (this) {
      case CardElement.fire:
        return '🔥';
      case CardElement.earth:
        return '🌍';
      case CardElement.water:
        return '💧';
      case CardElement.air:
        return '🌪️';
    }
  }
}

extension CardRarityExtension on CardRarity {
  String get displayName {
    switch (this) {
      case CardRarity.common:
        return 'Común';
      case CardRarity.uncommon:
        return 'Poco Común';
      case CardRarity.rare:
        return 'Raro';
      case CardRarity.epic:
        return 'Épico';
      case CardRarity.legendary:
        return 'Legendario';
    }
  }

  String get colorEmoji {
    switch (this) {
      case CardRarity.common:
        return '⚪';
      case CardRarity.uncommon:
        return '🟢';
      case CardRarity.rare:
        return '🔵';
      case CardRarity.epic:
        return '🟣';
      case CardRarity.legendary:
        return '🟡';
    }
  }

  int get experiencePoints {
    switch (this) {
      case CardRarity.common:
        return 10;
      case CardRarity.uncommon:
        return 25;
      case CardRarity.rare:
        return 50;
      case CardRarity.epic:
        return 100;
      case CardRarity.legendary:
        return 200;
    }
  }

  int get tradeValue {
    switch (this) {
      case CardRarity.common:
        return 1;
      case CardRarity.uncommon:
        return 3;
      case CardRarity.rare:
        return 10;
      case CardRarity.epic:
        return 25;
      case CardRarity.legendary:
        return 100;
    }
  }
}

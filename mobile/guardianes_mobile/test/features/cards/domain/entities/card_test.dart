import 'package:flutter_test/flutter_test.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/card.dart';

void main() {
  Card createCard({
    int id = 1,
    String name = 'Test Card',
    String description = 'A test card',
    CardElement element = CardElement.fire,
    CardRarity rarity = CardRarity.common,
    int attackPower = 50,
    int defensePower = 30,
    int energyCost = 3,
    String? imageUrl,
    String qrCode = 'test_qr',
    String? nfcCode,
    DateTime? createdAt,
    bool isActive = true,
  }) {
    return Card(
      id: id,
      name: name,
      description: description,
      element: element,
      rarity: rarity,
      attackPower: attackPower,
      defensePower: defensePower,
      energyCost: energyCost,
      imageUrl: imageUrl,
      qrCode: qrCode,
      nfcCode: nfcCode,
      createdAt: createdAt ?? DateTime(2025, 1, 1),
      isActive: isActive,
    );
  }

  group('Card Entity', () {
    test('should create a card with all required fields', () {
      final card = createCard();

      expect(card.id, 1);
      expect(card.name, 'Test Card');
      expect(card.element, CardElement.fire);
      expect(card.rarity, CardRarity.common);
    });

    group('isPremium', () {
      test('should return true when nfcCode is present', () {
        final card = createCard(nfcCode: 'nfc_123');
        expect(card.isPremium, true);
      });

      test('should return false when nfcCode is null', () {
        final card = createCard(nfcCode: null);
        expect(card.isPremium, false);
      });

      test('should return false when nfcCode is empty', () {
        final card = createCard(nfcCode: '');
        expect(card.isPremium, false);
      });
    });

    group('totalPower', () {
      test('should return sum of attack and defense', () {
        final card = createCard(attackPower: 50, defensePower: 30);
        expect(card.totalPower, 80);
      });

      test('should handle zero values', () {
        final card = createCard(attackPower: 0, defensePower: 0);
        expect(card.totalPower, 0);
      });
    });

    group('canBePlayedWith', () {
      test('should return true when energy is sufficient', () {
        final card = createCard(energyCost: 3);
        expect(card.canBePlayedWith(5), true);
      });

      test('should return true when energy is exactly equal', () {
        final card = createCard(energyCost: 3);
        expect(card.canBePlayedWith(3), true);
      });

      test('should return false when energy is insufficient', () {
        final card = createCard(energyCost: 5);
        expect(card.canBePlayedWith(3), false);
      });
    });

    group('hasElementalAdvantageOver', () {
      test('fire has advantage over earth', () {
        final fireCard = createCard(element: CardElement.fire);
        final earthCard = createCard(element: CardElement.earth);
        expect(fireCard.hasElementalAdvantageOver(earthCard), true);
      });

      test('earth has advantage over water', () {
        final earthCard = createCard(element: CardElement.earth);
        final waterCard = createCard(element: CardElement.water);
        expect(earthCard.hasElementalAdvantageOver(waterCard), true);
      });

      test('water has advantage over air', () {
        final waterCard = createCard(element: CardElement.water);
        final airCard = createCard(element: CardElement.air);
        expect(waterCard.hasElementalAdvantageOver(airCard), true);
      });

      test('air has advantage over fire', () {
        final airCard = createCard(element: CardElement.air);
        final fireCard = createCard(element: CardElement.fire);
        expect(airCard.hasElementalAdvantageOver(fireCard), true);
      });

      test('fire has no advantage over water', () {
        final fireCard = createCard(element: CardElement.fire);
        final waterCard = createCard(element: CardElement.water);
        expect(fireCard.hasElementalAdvantageOver(waterCard), false);
      });

      test('same element has no advantage', () {
        final fireCard1 = createCard(element: CardElement.fire);
        final fireCard2 = createCard(element: CardElement.fire);
        expect(fireCard1.hasElementalAdvantageOver(fireCard2), false);
      });
    });

    group('calculateDamageAgainst', () {
      test('should calculate base damage as attack minus defense', () {
        final attacker = createCard(attackPower: 50, element: CardElement.water);
        final defender = createCard(defensePower: 20, element: CardElement.earth);
        expect(attacker.calculateDamageAgainst(defender), 30);
      });

      test('should return minimum 1 damage even if defense is higher', () {
        final attacker = createCard(attackPower: 10, element: CardElement.water);
        final defender = createCard(defensePower: 50, element: CardElement.earth);
        expect(attacker.calculateDamageAgainst(defender), 1);
      });

      test('should apply 50% bonus for elemental advantage', () {
        final attacker = createCard(attackPower: 50, element: CardElement.fire);
        final defender = createCard(defensePower: 20, element: CardElement.earth);
        // Base: 50 - 20 = 30, with 50% bonus = 45
        expect(attacker.calculateDamageAgainst(defender), 45);
      });

      test('should apply 50% bonus correctly for minimum damage', () {
        final attacker = createCard(attackPower: 10, element: CardElement.fire);
        final defender = createCard(defensePower: 50, element: CardElement.earth);
        // Base: 1 (minimum), with 50% bonus = 1.5 rounded = 2
        expect(attacker.calculateDamageAgainst(defender), 2);
      });
    });

    group('Equatable', () {
      test('should be equal when all props match', () {
        final card1 = createCard();
        final card2 = createCard();
        expect(card1, equals(card2));
      });

      test('should not be equal when id differs', () {
        final card1 = createCard(id: 1);
        final card2 = createCard(id: 2);
        expect(card1, isNot(equals(card2)));
      });
    });
  });

  group('CardElementExtension', () {
    test('displayName returns correct Spanish names', () {
      expect(CardElement.fire.displayName, 'Fuego');
      expect(CardElement.earth.displayName, 'Tierra');
      expect(CardElement.water.displayName, 'Agua');
      expect(CardElement.air.displayName, 'Aire');
    });

    test('emoji returns correct emojis', () {
      expect(CardElement.fire.emoji, '🔥');
      expect(CardElement.earth.emoji, '🌍');
      expect(CardElement.water.emoji, '💧');
      expect(CardElement.air.emoji, '🌪️');
    });
  });

  group('CardRarityExtension', () {
    test('displayName returns correct Spanish names', () {
      expect(CardRarity.common.displayName, 'Común');
      expect(CardRarity.uncommon.displayName, 'Poco Común');
      expect(CardRarity.rare.displayName, 'Raro');
      expect(CardRarity.epic.displayName, 'Épico');
      expect(CardRarity.legendary.displayName, 'Legendario');
    });

    test('colorEmoji returns correct emojis', () {
      expect(CardRarity.common.colorEmoji, '⚪');
      expect(CardRarity.uncommon.colorEmoji, '🟢');
      expect(CardRarity.rare.colorEmoji, '🔵');
      expect(CardRarity.epic.colorEmoji, '🟣');
      expect(CardRarity.legendary.colorEmoji, '🟡');
    });

    test('experiencePoints returns correct values', () {
      expect(CardRarity.common.experiencePoints, 10);
      expect(CardRarity.uncommon.experiencePoints, 25);
      expect(CardRarity.rare.experiencePoints, 50);
      expect(CardRarity.epic.experiencePoints, 100);
      expect(CardRarity.legendary.experiencePoints, 200);
    });

    test('tradeValue returns correct values', () {
      expect(CardRarity.common.tradeValue, 1);
      expect(CardRarity.uncommon.tradeValue, 3);
      expect(CardRarity.rare.tradeValue, 10);
      expect(CardRarity.epic.tradeValue, 25);
      expect(CardRarity.legendary.tradeValue, 100);
    });
  });
}

import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:mockito/annotations.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/card.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/card_collection.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/collected_card.dart';
import 'package:guardianes_mobile/features/cards/domain/repositories/card_repository.dart';
import 'package:guardianes_mobile/features/cards/domain/usecases/get_card_collection.dart';

@GenerateMocks([CardRepository])
import 'get_card_collection_test.mocks.dart';

void main() {
  late GetCardCollection usecase;
  late MockCardRepository mockRepository;

  setUp(() {
    mockRepository = MockCardRepository();
    usecase = GetCardCollection(mockRepository);
  });

  group('GetCardCollection', () {
    const tGuardianId = 1;

    test('should return collection from repository', () async {
      final testCard = Card(
        id: 1,
        name: 'Test Card',
        description: 'A test card',
        element: CardElement.fire,
        rarity: CardRarity.common,
        attackPower: 50,
        defensePower: 30,
        energyCost: 3,
        qrCode: 'test_qr',
        createdAt: DateTime(2025, 1, 1),
        isActive: true,
      );

      final collectedCard = CollectedCard(
        card: testCard,
        count: 2,
        firstCollectedAt: DateTime(2025, 1, 1),
        lastCollectedAt: DateTime(2025, 1, 2),
      );

      final collection = CardCollection(
        guardianId: tGuardianId,
        cards: [collectedCard],
        createdAt: DateTime(2025, 1, 1),
      );

      when(mockRepository.getGuardianCollection(tGuardianId))
          .thenAnswer((_) async => collection);

      final result = await usecase(tGuardianId);

      expect(result.guardianId, tGuardianId);
      expect(result.cards.length, 1);
      expect(result.cards.first.card.name, 'Test Card');
      verify(mockRepository.getGuardianCollection(tGuardianId));
    });

    test('should return empty collection when guardian has no cards', () async {
      final emptyCollection = CardCollection(
        guardianId: tGuardianId,
        cards: [],
        createdAt: DateTime(2025, 1, 1),
      );

      when(mockRepository.getGuardianCollection(tGuardianId))
          .thenAnswer((_) async => emptyCollection);

      final result = await usecase(tGuardianId);

      expect(result.cards, isEmpty);
    });

    test('should propagate exception from repository', () async {
      when(mockRepository.getGuardianCollection(any))
          .thenThrow(Exception('Network error'));

      expect(
        () => usecase(tGuardianId),
        throwsException,
      );
    });
  });
}

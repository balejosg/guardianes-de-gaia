import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:mockito/annotations.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/card.dart';
import 'package:guardianes_mobile/features/cards/domain/repositories/card_repository.dart';
import 'package:guardianes_mobile/features/cards/domain/usecases/search_cards.dart';

@GenerateMocks([CardRepository])
import 'search_cards_test.mocks.dart';

void main() {
  late SearchCards usecase;
  late MockCardRepository mockRepository;

  setUp(() {
    mockRepository = MockCardRepository();
    usecase = SearchCards(mockRepository);
  });

  Card createTestCard({
    int id = 1,
    String name = 'Test Card',
    CardElement element = CardElement.fire,
    CardRarity rarity = CardRarity.common,
  }) {
    return Card(
      id: id,
      name: name,
      description: 'A test card',
      element: element,
      rarity: rarity,
      attackPower: 50,
      defensePower: 30,
      energyCost: 3,
      qrCode: 'test_qr_$id',
      createdAt: DateTime(2025, 1, 1),
      isActive: true,
    );
  }

  group('SearchCards', () {
    test('should search by name', () async {
      final cards = [createTestCard(name: 'Fire Dragon')];
      when(mockRepository.searchCards(
        name: 'Dragon',
        element: null,
        rarity: null,
      )).thenAnswer((_) async => cards);

      final result = await usecase(name: 'Dragon');

      expect(result.length, 1);
      expect(result.first.name, 'Fire Dragon');
      verify(mockRepository.searchCards(
        name: 'Dragon',
        element: null,
        rarity: null,
      ));
    });

    test('should search by element', () async {
      final cards = [
        createTestCard(id: 1, element: CardElement.water),
        createTestCard(id: 2, element: CardElement.water),
      ];
      when(mockRepository.searchCards(
        name: null,
        element: CardElement.water,
        rarity: null,
      )).thenAnswer((_) async => cards);

      final result = await usecase(element: CardElement.water);

      expect(result.length, 2);
      verify(mockRepository.searchCards(
        name: null,
        element: CardElement.water,
        rarity: null,
      ));
    });

    test('should search by rarity', () async {
      final cards = [createTestCard(rarity: CardRarity.legendary)];
      when(mockRepository.searchCards(
        name: null,
        element: null,
        rarity: CardRarity.legendary,
      )).thenAnswer((_) async => cards);

      final result = await usecase(rarity: CardRarity.legendary);

      expect(result.length, 1);
    });

    test('should search with multiple filters', () async {
      final cards = [
        createTestCard(
          name: 'Fire Dragon',
          element: CardElement.fire,
          rarity: CardRarity.legendary,
        ),
      ];
      when(mockRepository.searchCards(
        name: 'Dragon',
        element: CardElement.fire,
        rarity: CardRarity.legendary,
      )).thenAnswer((_) async => cards);

      final result = await usecase(
        name: 'Dragon',
        element: CardElement.fire,
        rarity: CardRarity.legendary,
      );

      expect(result.length, 1);
      expect(result.first.name, 'Fire Dragon');
    });

    test('should return empty list when no cards match', () async {
      when(mockRepository.searchCards(
        name: 'NonExistent',
        element: null,
        rarity: null,
      )).thenAnswer((_) async => []);

      final result = await usecase(name: 'NonExistent');

      expect(result, isEmpty);
    });

    test('should propagate exception from repository', () async {
      when(mockRepository.searchCards(
        name: anyNamed('name'),
        element: anyNamed('element'),
        rarity: anyNamed('rarity'),
      )).thenThrow(Exception('Network error'));

      expect(
        () => usecase(name: 'Test'),
        throwsException,
      );
    });
  });
}

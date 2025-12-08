import 'package:bloc_test/bloc_test.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:mockito/annotations.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/card.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/card_collection.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/card_scan_result.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/collected_card.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/collection_statistics.dart';
import 'package:guardianes_mobile/features/cards/domain/repositories/card_repository.dart';
import 'package:guardianes_mobile/features/cards/domain/usecases/scan_qr_code.dart';
import 'package:guardianes_mobile/features/cards/domain/usecases/get_card_collection.dart';
import 'package:guardianes_mobile/features/cards/domain/usecases/get_collection_statistics.dart';
import 'package:guardianes_mobile/features/cards/domain/usecases/search_cards.dart';
import 'package:guardianes_mobile/features/cards/presentation/bloc/card_bloc.dart';
import 'package:guardianes_mobile/features/cards/presentation/bloc/card_event.dart';
import 'package:guardianes_mobile/features/cards/presentation/bloc/card_state.dart';

@GenerateMocks([
  ScanQRCode,
  GetCardCollection,
  GetCollectionStatistics,
  SearchCards,
  CardRepository,
])
import 'card_bloc_test.mocks.dart';

void main() {
  late CardBloc bloc;
  late MockScanQRCode mockScanQRCode;
  late MockGetCardCollection mockGetCardCollection;
  late MockGetCollectionStatistics mockGetCollectionStatistics;
  late MockSearchCards mockSearchCards;
  late MockCardRepository mockCardRepository;

  setUp(() {
    mockScanQRCode = MockScanQRCode();
    mockGetCardCollection = MockGetCardCollection();
    mockGetCollectionStatistics = MockGetCollectionStatistics();
    mockSearchCards = MockSearchCards();
    mockCardRepository = MockCardRepository();

    bloc = CardBloc(
      scanQRCode: mockScanQRCode,
      getCardCollection: mockGetCardCollection,
      getCollectionStatistics: mockGetCollectionStatistics,
      searchCards: mockSearchCards,
      cardRepository: mockCardRepository,
    );
  });

  tearDown(() {
    bloc.close();
  });

  Card createTestCard({int id = 1, String name = 'Test Card'}) {
    return Card(
      id: id,
      name: name,
      description: 'A test card',
      element: CardElement.fire,
      rarity: CardRarity.common,
      attackPower: 50,
      defensePower: 30,
      energyCost: 3,
      qrCode: 'test_qr_$id',
      createdAt: DateTime(2025, 1, 1),
      isActive: true,
    );
  }

  group('CardBloc', () {
    test('initial state is CardInitial', () {
      expect(bloc.state, CardInitial());
    });

    group('ScanQRCodeEvent', () {
      const tGuardianId = 1;
      const tQrCode = 'valid_qr';

      blocTest<CardBloc, CardState>(
        'emits [CardLoading, CardScanSuccess] when scan is successful',
        build: () {
          final card = createTestCard();
          final result = CardScanResult.success(card: card, count: 1, isNew: true);
          when(mockScanQRCode(tGuardianId, tQrCode))
              .thenAnswer((_) async => result);
          return bloc;
        },
        act: (bloc) => bloc.add(const ScanQRCodeEvent(
          guardianId: tGuardianId,
          qrCode: tQrCode,
        )),
        expect: () => [
          CardLoading(),
          isA<CardScanSuccess>(),
        ],
      );

      blocTest<CardBloc, CardState>(
        'emits [CardLoading, CardScanFailure] when scan returns error',
        build: () {
          final result = CardScanResult.error('Invalid QR code');
          when(mockScanQRCode(tGuardianId, tQrCode))
              .thenAnswer((_) async => result);
          return bloc;
        },
        act: (bloc) => bloc.add(const ScanQRCodeEvent(
          guardianId: tGuardianId,
          qrCode: tQrCode,
        )),
        expect: () => [
          CardLoading(),
          const CardScanFailure(error: 'Invalid QR code'),
        ],
      );

      blocTest<CardBloc, CardState>(
        'emits [CardLoading, CardScanFailure] when exception thrown',
        build: () {
          when(mockScanQRCode(any, any))
              .thenThrow(Exception('Network error'));
          return bloc;
        },
        act: (bloc) => bloc.add(const ScanQRCodeEvent(
          guardianId: tGuardianId,
          qrCode: tQrCode,
        )),
        expect: () => [
          CardLoading(),
          isA<CardScanFailure>(),
        ],
      );
    });

    group('LoadCardCollectionEvent', () {
      const tGuardianId = 1;

      blocTest<CardBloc, CardState>(
        'emits [CardLoading, CollectionLoaded] when collection loaded',
        build: () {
          final collection = CardCollection(
            guardianId: tGuardianId,
            cards: [],
            createdAt: DateTime(2025, 1, 1),
          );
          when(mockGetCardCollection(tGuardianId))
              .thenAnswer((_) async => collection);
          return bloc;
        },
        act: (bloc) => bloc.add(const LoadCardCollectionEvent(guardianId: tGuardianId)),
        expect: () => [
          CardLoading(),
          isA<CollectionLoaded>(),
        ],
      );

      blocTest<CardBloc, CardState>(
        'emits [CardLoading, CardError] when exception thrown',
        build: () {
          when(mockGetCardCollection(any))
              .thenThrow(Exception('Network error'));
          return bloc;
        },
        act: (bloc) => bloc.add(const LoadCardCollectionEvent(guardianId: tGuardianId)),
        expect: () => [
          CardLoading(),
          isA<CardError>(),
        ],
      );
    });

    group('LoadCollectionStatisticsEvent', () {
      const tGuardianId = 1;

      blocTest<CardBloc, CardState>(
        'emits [CardLoading, CollectionStatisticsLoaded] when stats loaded',
        build: () {
          final stats = CollectionStatistics(
            uniqueCardCount: 10,
            totalCardCount: 15,
            completionPercentage: 20.0,
            cardCountsByElement: {},
            cardCountsByRarity: {},
            totalTradeValue: 30,
            hasElementalBalance: false,
          );
          when(mockGetCollectionStatistics(tGuardianId))
              .thenAnswer((_) async => stats);
          return bloc;
        },
        act: (bloc) => bloc.add(const LoadCollectionStatisticsEvent(guardianId: tGuardianId)),
        expect: () => [
          CardLoading(),
          isA<CollectionStatisticsLoaded>(),
        ],
      );

      blocTest<CardBloc, CardState>(
        'emits [CardLoading, CardError] when exception thrown',
        build: () {
          when(mockGetCollectionStatistics(any))
              .thenThrow(Exception('Network error'));
          return bloc;
        },
        act: (bloc) => bloc.add(const LoadCollectionStatisticsEvent(guardianId: tGuardianId)),
        expect: () => [
          CardLoading(),
          isA<CardError>(),
        ],
      );
    });

    group('SearchCardsEvent', () {
      blocTest<CardBloc, CardState>(
        'emits [CardLoading, CardsSearchResults] when search succeeds',
        build: () {
          final cards = [createTestCard(), createTestCard(id: 2)];
          when(mockSearchCards(
            name: 'Dragon',
            element: null,
            rarity: null,
          )).thenAnswer((_) async => cards);
          return bloc;
        },
        act: (bloc) => bloc.add(const SearchCardsEvent(name: 'Dragon')),
        expect: () => [
          CardLoading(),
          isA<CardsSearchResults>(),
        ],
      );

      blocTest<CardBloc, CardState>(
        'emits [CardLoading, CardError] when exception thrown',
        build: () {
          when(mockSearchCards(
            name: anyNamed('name'),
            element: anyNamed('element'),
            rarity: anyNamed('rarity'),
          )).thenThrow(Exception('Network error'));
          return bloc;
        },
        act: (bloc) => bloc.add(const SearchCardsEvent(name: 'Dragon')),
        expect: () => [
          CardLoading(),
          isA<CardError>(),
        ],
      );
    });

    group('LoadRecentCardsEvent', () {
      const tGuardianId = 1;
      const tLimit = 5;

      blocTest<CardBloc, CardState>(
        'emits [CardLoading, RecentCardsLoaded] when recent cards loaded',
        build: () {
          final card = createTestCard();
          final collectedCards = [
            CollectedCard(
              card: card,
              count: 1,
              firstCollectedAt: DateTime(2025, 1, 1),
              lastCollectedAt: DateTime(2025, 1, 2),
            ),
          ];
          when(mockCardRepository.getRecentlyCollected(tGuardianId, tLimit))
              .thenAnswer((_) async => collectedCards);
          return bloc;
        },
        act: (bloc) => bloc.add(const LoadRecentCardsEvent(
          guardianId: tGuardianId,
          limit: tLimit,
        )),
        expect: () => [
          CardLoading(),
          isA<RecentCardsLoaded>(),
        ],
      );

      blocTest<CardBloc, CardState>(
        'emits [CardLoading, CardError] when exception thrown',
        build: () {
          when(mockCardRepository.getRecentlyCollected(any, any))
              .thenThrow(Exception('Network error'));
          return bloc;
        },
        act: (bloc) => bloc.add(const LoadRecentCardsEvent(
          guardianId: tGuardianId,
          limit: tLimit,
        )),
        expect: () => [
          CardLoading(),
          isA<CardError>(),
        ],
      );
    });
  });
}

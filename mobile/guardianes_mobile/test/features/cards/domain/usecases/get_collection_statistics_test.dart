import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:mockito/annotations.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/card.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/collection_statistics.dart';
import 'package:guardianes_mobile/features/cards/domain/repositories/card_repository.dart';
import 'package:guardianes_mobile/features/cards/domain/usecases/get_collection_statistics.dart';

@GenerateMocks([CardRepository])
import 'get_collection_statistics_test.mocks.dart';

void main() {
  late GetCollectionStatistics usecase;
  late MockCardRepository mockRepository;

  setUp(() {
    mockRepository = MockCardRepository();
    usecase = GetCollectionStatistics(mockRepository);
  });

  group('GetCollectionStatistics', () {
    const tGuardianId = 1;

    test('should return statistics from repository', () async {
      final stats = CollectionStatistics(
        uniqueCardCount: 15,
        totalCardCount: 25,
        completionPercentage: 31.25,
        cardCountsByElement: {
          CardElement.fire: 5,
          CardElement.water: 4,
          CardElement.earth: 3,
          CardElement.air: 3,
        },
        cardCountsByRarity: {
          CardRarity.common: 10,
          CardRarity.legendary: 1,
        },
        totalTradeValue: 50,
        hasElementalBalance: true,
      );

      when(mockRepository.getCollectionStatistics(tGuardianId))
          .thenAnswer((_) async => stats);

      final result = await usecase(tGuardianId);

      expect(result.uniqueCardCount, 15);
      expect(result.totalCardCount, 25);
      expect(result.completionPercentage, 31.25);
      expect(result.hasElementalBalance, true);
      verify(mockRepository.getCollectionStatistics(tGuardianId));
    });

    test('should return zero statistics for new guardian', () async {
      final emptyStats = CollectionStatistics(
        uniqueCardCount: 0,
        totalCardCount: 0,
        completionPercentage: 0.0,
        cardCountsByElement: {},
        cardCountsByRarity: {},
        totalTradeValue: 0,
        hasElementalBalance: false,
      );

      when(mockRepository.getCollectionStatistics(tGuardianId))
          .thenAnswer((_) async => emptyStats);

      final result = await usecase(tGuardianId);

      expect(result.uniqueCardCount, 0);
      expect(result.completionPercentage, 0.0);
    });

    test('should propagate exception from repository', () async {
      when(mockRepository.getCollectionStatistics(any))
          .thenThrow(Exception('Network error'));

      expect(
        () => usecase(tGuardianId),
        throwsException,
      );
    });
  });
}

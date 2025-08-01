import '../entities/collection_statistics.dart';
import '../repositories/card_repository.dart';

class GetCollectionStatistics {
  final CardRepository repository;

  const GetCollectionStatistics(this.repository);

  Future<CollectionStatistics> call(int guardianId) async {
    return await repository.getCollectionStatistics(guardianId);
  }
}

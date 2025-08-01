import '../entities/card_collection.dart';
import '../repositories/card_repository.dart';

class GetCardCollection {
  final CardRepository repository;

  const GetCardCollection(this.repository);

  Future<CardCollection> call(int guardianId) async {
    return await repository.getGuardianCollection(guardianId);
  }
}

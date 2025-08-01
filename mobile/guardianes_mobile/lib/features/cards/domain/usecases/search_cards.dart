import '../entities/card.dart';
import '../repositories/card_repository.dart';

class SearchCards {
  final CardRepository repository;

  const SearchCards(this.repository);

  Future<List<Card>> call({
    String? name,
    CardElement? element,
    CardRarity? rarity,
  }) async {
    return await repository.searchCards(
      name: name,
      element: element,
      rarity: rarity,
    );
  }
}

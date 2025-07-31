import 'package:equatable/equatable.dart';
import 'card.dart';

class CollectedCard extends Equatable {
  final Card card;
  final int count;
  final DateTime firstCollectedAt;
  final DateTime lastCollectedAt;

  const CollectedCard({
    required this.card,
    required this.count,
    required this.firstCollectedAt,
    required this.lastCollectedAt,
  });

  bool get isMultiple => count > 1;
  bool get isPremium => card.isPremium;

  @override
  List<Object?> get props => [
        card.id,
        count,
        firstCollectedAt,
        lastCollectedAt,
      ];
}
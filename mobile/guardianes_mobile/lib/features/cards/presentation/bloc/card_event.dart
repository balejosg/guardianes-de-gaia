import 'package:equatable/equatable.dart';
import '../../domain/entities/card.dart';

abstract class CardEvent extends Equatable {
  const CardEvent();

  @override
  List<Object?> get props => [];
}

class ScanQRCodeEvent extends CardEvent {
  final int guardianId;
  final String qrCode;

  const ScanQRCodeEvent({
    required this.guardianId,
    required this.qrCode,
  });

  @override
  List<Object?> get props => [guardianId, qrCode];
}

class LoadCardCollectionEvent extends CardEvent {
  final int guardianId;

  const LoadCardCollectionEvent({required this.guardianId});

  @override
  List<Object?> get props => [guardianId];
}

class LoadCollectionStatisticsEvent extends CardEvent {
  final int guardianId;

  const LoadCollectionStatisticsEvent({required this.guardianId});

  @override
  List<Object?> get props => [guardianId];
}

class SearchCardsEvent extends CardEvent {
  final String? name;
  final CardElement? element;
  final CardRarity? rarity;

  const SearchCardsEvent({
    this.name,
    this.element,
    this.rarity,
  });

  @override
  List<Object?> get props => [name, element, rarity];
}

class LoadRecentCardsEvent extends CardEvent {
  final int guardianId;
  final int limit;

  const LoadRecentCardsEvent({
    required this.guardianId,
    this.limit = 10,
  });

  @override
  List<Object?> get props => [guardianId, limit];
}
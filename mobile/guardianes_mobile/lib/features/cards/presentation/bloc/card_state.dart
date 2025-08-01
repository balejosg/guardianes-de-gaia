import 'package:equatable/equatable.dart';
import '../../domain/entities/card.dart';
import '../../domain/entities/card_collection.dart';
import '../../domain/entities/card_scan_result.dart';
import '../../domain/entities/collected_card.dart';
import '../../domain/entities/collection_statistics.dart';

abstract class CardState extends Equatable {
  const CardState();

  @override
  List<Object?> get props => [];
}

class CardInitial extends CardState {}

class CardLoading extends CardState {}

class CardScanSuccess extends CardState {
  final CardScanResult result;

  const CardScanSuccess({required this.result});

  @override
  List<Object?> get props => [result];
}

class CardScanFailure extends CardState {
  final String error;

  const CardScanFailure({required this.error});

  @override
  List<Object?> get props => [error];
}

class CollectionLoaded extends CardState {
  final CardCollection collection;

  const CollectionLoaded({required this.collection});

  @override
  List<Object?> get props => [collection];
}

class CollectionStatisticsLoaded extends CardState {
  final CollectionStatistics statistics;

  const CollectionStatisticsLoaded({required this.statistics});

  @override
  List<Object?> get props => [statistics];
}

class CardsSearchResults extends CardState {
  final List<Card> cards;

  const CardsSearchResults({required this.cards});

  @override
  List<Object?> get props => [cards];
}

class RecentCardsLoaded extends CardState {
  final List<CollectedCard> recentCards;

  const RecentCardsLoaded({required this.recentCards});

  @override
  List<Object?> get props => [recentCards];
}

class CardError extends CardState {
  final String error;

  const CardError({required this.error});

  @override
  List<Object?> get props => [error];
}

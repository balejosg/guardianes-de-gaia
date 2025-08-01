import 'package:flutter_bloc/flutter_bloc.dart';
import '../../domain/usecases/scan_qr_code.dart';
import '../../domain/usecases/get_card_collection.dart';
import '../../domain/usecases/get_collection_statistics.dart';
import '../../domain/usecases/search_cards.dart';
import '../../domain/repositories/card_repository.dart';
import 'card_event.dart';
import 'card_state.dart';

class CardBloc extends Bloc<CardEvent, CardState> {
  final ScanQRCode scanQRCode;
  final GetCardCollection getCardCollection;
  final GetCollectionStatistics getCollectionStatistics;
  final SearchCards searchCards;
  final CardRepository cardRepository;

  CardBloc({
    required this.scanQRCode,
    required this.getCardCollection,
    required this.getCollectionStatistics,
    required this.searchCards,
    required this.cardRepository,
  }) : super(CardInitial()) {
    on<ScanQRCodeEvent>(_onScanQRCode);
    on<LoadCardCollectionEvent>(_onLoadCardCollection);
    on<LoadCollectionStatisticsEvent>(_onLoadCollectionStatistics);
    on<SearchCardsEvent>(_onSearchCards);
    on<LoadRecentCardsEvent>(_onLoadRecentCards);
  }

  Future<void> _onScanQRCode(
    ScanQRCodeEvent event,
    Emitter<CardState> emit,
  ) async {
    emit(CardLoading());

    try {
      final result = await scanQRCode(event.guardianId, event.qrCode);

      if (result.success) {
        emit(CardScanSuccess(result: result));
      } else {
        emit(CardScanFailure(error: result.message));
      }
    } catch (e) {
      emit(CardScanFailure(error: 'Error inesperado: $e'));
    }
  }

  Future<void> _onLoadCardCollection(
    LoadCardCollectionEvent event,
    Emitter<CardState> emit,
  ) async {
    emit(CardLoading());

    try {
      final collection = await getCardCollection(event.guardianId);
      emit(CollectionLoaded(collection: collection));
    } catch (e) {
      emit(CardError(error: 'Error al cargar la colección: $e'));
    }
  }

  Future<void> _onLoadCollectionStatistics(
    LoadCollectionStatisticsEvent event,
    Emitter<CardState> emit,
  ) async {
    emit(CardLoading());

    try {
      final statistics = await getCollectionStatistics(event.guardianId);
      emit(CollectionStatisticsLoaded(statistics: statistics));
    } catch (e) {
      emit(CardError(error: 'Error al cargar estadísticas: $e'));
    }
  }

  Future<void> _onSearchCards(
    SearchCardsEvent event,
    Emitter<CardState> emit,
  ) async {
    emit(CardLoading());

    try {
      final cards = await searchCards(
        name: event.name,
        element: event.element,
        rarity: event.rarity,
      );
      emit(CardsSearchResults(cards: cards));
    } catch (e) {
      emit(CardError(error: 'Error al buscar cartas: $e'));
    }
  }

  Future<void> _onLoadRecentCards(
    LoadRecentCardsEvent event,
    Emitter<CardState> emit,
  ) async {
    emit(CardLoading());

    try {
      final recentCards = await cardRepository.getRecentlyCollected(
        event.guardianId,
        event.limit,
      );
      emit(RecentCardsLoaded(recentCards: recentCards));
    } catch (e) {
      emit(CardError(error: 'Error al cargar cartas recientes: $e'));
    }
  }
}

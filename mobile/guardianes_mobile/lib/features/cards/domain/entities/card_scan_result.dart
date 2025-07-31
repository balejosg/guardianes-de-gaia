import 'package:equatable/equatable.dart';
import 'card.dart';

class CardScanResult extends Equatable {
  final bool success;
  final String message;
  final Card? card;
  final int? count;
  final bool isNew;

  const CardScanResult({
    required this.success,
    required this.message,
    this.card,
    this.count,
    required this.isNew,
  });

  factory CardScanResult.success({
    required Card card,
    required int count,
    required bool isNew,
  }) {
    final message = isNew 
        ? '¡Nueva carta coleccionada!'
        : '¡Carta ya poseída - cantidad aumentada!';
    
    return CardScanResult(
      success: true,
      message: message,
      card: card,
      count: count,
      isNew: isNew,
    );
  }

  factory CardScanResult.error(String message) {
    return CardScanResult(
      success: false,
      message: message,
      isNew: false,
    );
  }

  @override
  List<Object?> get props => [success, message, card?.id, count, isNew];
}
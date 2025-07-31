import '../entities/card_scan_result.dart';
import '../repositories/card_repository.dart';

class ScanQRCode {
  final CardRepository repository;

  const ScanQRCode(this.repository);

  Future<CardScanResult> call(int guardianId, String qrCode) async {
    if (qrCode.trim().isEmpty) {
      return CardScanResult.error('El código QR no puede estar vacío');
    }

    try {
      return await repository.scanQRCode(guardianId, qrCode);
    } catch (e) {
      return CardScanResult.error('Error al escanear código QR: $e');
    }
  }
}
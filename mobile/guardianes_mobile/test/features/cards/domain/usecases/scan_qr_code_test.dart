import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:mockito/annotations.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/card.dart';
import 'package:guardianes_mobile/features/cards/domain/entities/card_scan_result.dart';
import 'package:guardianes_mobile/features/cards/domain/repositories/card_repository.dart';
import 'package:guardianes_mobile/features/cards/domain/usecases/scan_qr_code.dart';

@GenerateMocks([CardRepository])
import 'scan_qr_code_test.mocks.dart';

void main() {
  late ScanQRCode usecase;
  late MockCardRepository mockRepository;

  setUp(() {
    mockRepository = MockCardRepository();
    usecase = ScanQRCode(mockRepository);
  });

  Card createTestCard() {
    return Card(
      id: 1,
      name: 'Fire Dragon',
      description: 'A fierce dragon',
      element: CardElement.fire,
      rarity: CardRarity.legendary,
      attackPower: 100,
      defensePower: 80,
      energyCost: 5,
      qrCode: 'dragon_qr',
      createdAt: DateTime(2025, 1, 1),
      isActive: true,
    );
  }

  group('ScanQRCode', () {
    const tGuardianId = 1;
    const tQrCode = 'valid_qr_code';

    test('should return error when QR code is empty', () async {
      final result = await usecase(tGuardianId, '');

      expect(result.success, false);
      expect(result.message, 'El código QR no puede estar vacío');
      verifyNever(mockRepository.scanQRCode(any, any));
    });

    test('should return error when QR code is whitespace only', () async {
      final result = await usecase(tGuardianId, '   ');

      expect(result.success, false);
      expect(result.message, 'El código QR no puede estar vacío');
      verifyNever(mockRepository.scanQRCode(any, any));
    });

    test('should call repository and return success result', () async {
      final testCard = createTestCard();
      final scanResult = CardScanResult.success(
        card: testCard,
        count: 1,
        isNew: true,
      );
      when(mockRepository.scanQRCode(tGuardianId, tQrCode))
          .thenAnswer((_) async => scanResult);

      final result = await usecase(tGuardianId, tQrCode);

      expect(result.success, true);
      expect(result.card, testCard);
      verify(mockRepository.scanQRCode(tGuardianId, tQrCode));
    });

    test('should return error result when repository throws', () async {
      when(mockRepository.scanQRCode(any, any))
          .thenThrow(Exception('Network error'));

      final result = await usecase(tGuardianId, tQrCode);

      expect(result.success, false);
      expect(result.message, contains('Error al escanear código QR'));
    });
  });
}

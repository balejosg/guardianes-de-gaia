import 'dart:async';
import 'package:flutter_test/flutter_test.dart';
import 'package:guardianes_mobile/features/cards/data/services/qr_scanner_service.dart';

void main() {
  group('QRScannerService', () {
    late QRScannerService service;

    setUp(() {
      service = QRScannerService();
    });

    tearDown(() {
      service.dispose();
    });

    test('isInitialized returns false initially', () {
      expect(service.isInitialized, false);
    });

    test('controller throws StateError when not initialized', () {
      expect(() => service.controller, throwsStateError);
    });

    test('startScanning throws StateError when not initialized', () {
      expect(() => service.startScanning(), throwsStateError);
    });

    test('stopScanning does not throw when not initialized', () {
      expect(() => service.stopScanning(), returnsNormally);
    });

    test('toggleTorch does not throw when not initialized', () async {
      await expectLater(service.toggleTorch(), completes);
    });

    test('switchCamera does not throw when not initialized', () async {
      await expectLater(service.switchCamera(), completes);
    });

    test('hasTorch returns false when not initialized', () async {
      expect(await service.hasTorch, false);
    });

    test('dispose does not throw when not initialized', () {
      expect(() => service.dispose(), returnsNormally);
    });

    test('checkCameraPermission is a static method that completes', () async {
      // This will check the current permission status
      await expectLater(
        QRScannerService.checkCameraPermission(),
        completes,
      );
    });
  });
}

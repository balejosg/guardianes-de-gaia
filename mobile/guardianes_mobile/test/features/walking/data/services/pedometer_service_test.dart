import 'dart:async';
import 'package:flutter_test/flutter_test.dart';
import 'package:guardianes_mobile/features/walking/data/services/pedometer_service.dart';

/// Mock implementation of PedometerService for testing
class MockPedometerService implements PedometerService {
  final StreamController<int> _stepController = StreamController<int>.broadcast();
  bool _initialized = false;
  bool _permissionsGranted = true;
  int _currentSteps = 0;
  
  set permissionsGranted(bool value) => _permissionsGranted = value;
  set currentSteps(int value) => _currentSteps = value;

  void emitSteps(int steps) {
    _stepController.add(steps);
  }

  @override
  Future<bool> initialize() async {
    if (!_permissionsGranted) return false;
    _initialized = true;
    return true;
  }

  @override
  Future<int> getCurrentStepCount() async {
    return _currentSteps;
  }

  @override
  Stream<int> get stepCountStream => _stepController.stream;

  @override
  Future<bool> requestPermissions() async {
    return _permissionsGranted;
  }

  @override
  Future<bool> hasPermissions() async {
    return _permissionsGranted;
  }

  @override
  void dispose() {
    _stepController.close();
  }
}

void main() {
  group('PedometerService Interface', () {
    late MockPedometerService service;

    setUp(() {
      service = MockPedometerService();
    });

    tearDown(() {
      service.dispose();
    });

    test('initialize returns true when permissions granted', () async {
      service.permissionsGranted = true;
      final result = await service.initialize();
      expect(result, true);
    });

    test('initialize returns false when permissions denied', () async {
      service.permissionsGranted = false;
      final result = await service.initialize();
      expect(result, false);
    });

    test('getCurrentStepCount returns current steps', () async {
      service.currentSteps = 5000;
      final result = await service.getCurrentStepCount();
      expect(result, 5000);
    });

    test('stepCountStream emits step updates', () async {
      final steps = <int>[];
      final subscription = service.stepCountStream.listen(steps.add);

      service.emitSteps(100);
      service.emitSteps(200);
      service.emitSteps(300);

      await Future.delayed(const Duration(milliseconds: 50));
      await subscription.cancel();

      expect(steps, [100, 200, 300]);
    });

    test('requestPermissions returns grant status', () async {
      service.permissionsGranted = true;
      expect(await service.requestPermissions(), true);

      service.permissionsGranted = false;
      expect(await service.requestPermissions(), false);
    });

    test('hasPermissions returns current status', () async {
      service.permissionsGranted = true;
      expect(await service.hasPermissions(), true);

      service.permissionsGranted = false;
      expect(await service.hasPermissions(), false);
    });
  });
}

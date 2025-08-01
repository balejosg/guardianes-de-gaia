import 'dart:async';
import 'package:pedometer/pedometer.dart';
import 'package:permission_handler/permission_handler.dart';

abstract class PedometerService {
  /// Initialize pedometer tracking
  Future<bool> initialize();

  /// Get current step count for today
  Future<int> getCurrentStepCount();

  /// Stream of step count updates
  Stream<int> get stepCountStream;

  /// Request necessary permissions
  Future<bool> requestPermissions();

  /// Check if permissions are granted
  Future<bool> hasPermissions();

  /// Dispose of resources
  void dispose();
}

class PedometerServiceImpl implements PedometerService {
  StreamSubscription<StepCount>? _stepCountSubscription;
  StreamSubscription<PedestrianStatus>? _pedestrianStatusSubscription;

  final StreamController<int> _stepCountController =
      StreamController<int>.broadcast();

  int _currentStepCount = 0;
  int _initialStepCount = 0;
  bool _isInitialized = false;

  @override
  Future<bool> initialize() async {
    if (_isInitialized) return true;

    // Request permissions first
    final hasPermission = await requestPermissions();
    if (!hasPermission) return false;

    try {
      // Listen to step count stream
      _stepCountSubscription = Pedometer.stepCountStream.listen(
        _onStepCount,
        onError: _onStepCountError,
      );

      // Listen to pedestrian status stream (optional)
      _pedestrianStatusSubscription = Pedometer.pedestrianStatusStream.listen(
        _onPedestrianStatusChanged,
        onError: _onPedestrianStatusError,
      );

      _isInitialized = true;
      return true;
    } catch (e) {
      print('Error initializing pedometer: $e');
      return false;
    }
  }

  @override
  Future<int> getCurrentStepCount() async {
    if (!_isInitialized) {
      final initialized = await initialize();
      if (!initialized) return 0;
    }

    // Return steps taken today (total minus initial count)
    return _currentStepCount - _initialStepCount;
  }

  @override
  Stream<int> get stepCountStream => _stepCountController.stream;

  @override
  Future<bool> requestPermissions() async {
    try {
      // Request activity recognition permission (required for step counting)
      var status = await Permission.activityRecognition.request();

      if (status != PermissionStatus.granted) {
        // Try requesting sensors permission as fallback
        status = await Permission.sensors.request();
      }

      return status == PermissionStatus.granted;
    } catch (e) {
      print('Error requesting permissions: $e');
      return false;
    }
  }

  @override
  Future<bool> hasPermissions() async {
    try {
      final activityStatus = await Permission.activityRecognition.status;
      final sensorStatus = await Permission.sensors.status;

      return activityStatus == PermissionStatus.granted ||
          sensorStatus == PermissionStatus.granted;
    } catch (e) {
      print('Error checking permissions: $e');
      return false;
    }
  }

  void _onStepCount(StepCount event) {
    final now = DateTime.now();
    final eventTime = event.timeStamp;

    // Only count steps from today
    if (_isSameDay(now, eventTime)) {
      if (_initialStepCount == 0) {
        // Set initial count on first reading of the day
        _initialStepCount = event.steps;
      }

      _currentStepCount = event.steps;
      final todaySteps = _currentStepCount - _initialStepCount;

      // Emit the daily step count
      _stepCountController.add(todaySteps.clamp(0, double.infinity).toInt());
    }
  }

  void _onStepCountError(dynamic error) {
    print('Step count error: $error');
    // Emit 0 on error
    _stepCountController.add(0);
  }

  void _onPedestrianStatusChanged(PedestrianStatus event) {
    // Handle pedestrian status changes if needed
    print('Pedestrian status: ${event.status} at ${event.timeStamp}');
  }

  void _onPedestrianStatusError(dynamic error) {
    print('Pedestrian status error: $error');
  }

  bool _isSameDay(DateTime date1, DateTime date2) {
    return date1.year == date2.year &&
        date1.month == date2.month &&
        date1.day == date2.day;
  }

  @override
  void dispose() {
    _stepCountSubscription?.cancel();
    _pedestrianStatusSubscription?.cancel();
    _stepCountController.close();
    _isInitialized = false;
  }
}

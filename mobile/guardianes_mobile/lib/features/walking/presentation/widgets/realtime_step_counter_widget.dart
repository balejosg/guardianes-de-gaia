import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:guardianes_mobile/core/utils/injection.dart';
import 'package:guardianes_mobile/features/walking/data/services/pedometer_service.dart';
import 'package:guardianes_mobile/features/walking/domain/entities/step_record.dart';
import 'package:guardianes_mobile/features/walking/domain/validators/step_validation.dart';
import 'package:guardianes_mobile/features/walking/presentation/bloc/step_bloc.dart';
import 'package:guardianes_mobile/features/walking/presentation/bloc/step_event.dart';
import 'package:intl/intl.dart';

class RealtimeStepCounterWidget extends StatefulWidget {
  final int guardianId;
  final PedometerService? pedometerService; // For testing

  const RealtimeStepCounterWidget({
    Key? key,
    this.guardianId = 1, // TODO: Get from auth context
    this.pedometerService,
  }) : super(key: key);

  @override
  State<RealtimeStepCounterWidget> createState() =>
      _RealtimeStepCounterWidgetState();
}

class _RealtimeStepCounterWidgetState extends State<RealtimeStepCounterWidget> {
  PedometerService? _pedometerService;
  StreamSubscription<int>? _stepSubscription;

  int _realtimeSteps = 0;
  int _lastSyncedSteps = 0;
  bool _isInitialized = false;
  bool _hasPermissions = false;
  String _errorMessage = '';
  Timer? _syncTimer;

  @override
  void initState() {
    super.initState();
    try {
      _pedometerService = widget.pedometerService ?? getIt<PedometerService>();
      _initializePedometer();
      _startPeriodicSync();
    } catch (e) {
      // Handle case where service is not available (e.g., in tests)
      setState(() {
        _isInitialized = false;
        _errorMessage = 'Pedometer service not available';
      });
    }
  }

  @override
  void dispose() {
    _stepSubscription?.cancel();
    _syncTimer?.cancel();
    try {
      _pedometerService?.dispose();
    } catch (e) {
      // Service might not be initialized
    }
    super.dispose();
  }

  Future<void> _initializePedometer() async {
    if (_pedometerService == null) return;

    try {
      final hasPermissions = await _pedometerService!.requestPermissions();

      if (!hasPermissions) {
        setState(() {
          _hasPermissions = false;
          _errorMessage = 'Permission required to count steps';
        });
        return;
      }

      final initialized = await _pedometerService!.initialize();

      if (initialized) {
        // Get initial step count
        final currentSteps = await _pedometerService!.getCurrentStepCount();

        setState(() {
          _isInitialized = true;
          _hasPermissions = true;
          _realtimeSteps = currentSteps;
        });

        // Listen to step updates
        _stepSubscription = _pedometerService!.stepCountStream.listen(
          (stepCount) {
            if (mounted) {
              setState(() {
                _realtimeSteps = stepCount;
              });
            }
          },
          onError: (error) {
            if (mounted) {
              setState(() {
                _errorMessage = 'Error counting steps: $error';
              });
            }
          },
        );
      } else {
        setState(() {
          _errorMessage = 'Failed to initialize step counter';
        });
      }
    } catch (e) {
      setState(() {
        _errorMessage = 'Error: $e';
      });
    }
  }

  void _startPeriodicSync() {
    // Sync with backend every 5 minutes
    _syncTimer = Timer.periodic(const Duration(minutes: 5), (timer) {
      _syncStepsWithBackend();
    });
  }

  void _syncStepsWithBackend() {
    if (_realtimeSteps > _lastSyncedSteps) {
      final newSteps = _realtimeSteps - _lastSyncedSteps;
      final timestamp = DateTime.now().toIso8601String();

      // Validate step submission before sending to backend
      final validationResult = StepValidator.validateCompleteSubmission(
        stepCount: newSteps,
        timestamp: timestamp,
        guardianId: widget.guardianId,
        currentDailyTotal: _realtimeSteps,
      );

      if (validationResult.isValid) {
        final stepRecord = StepRecord(
          guardianId: widget.guardianId,
          stepCount: newSteps,
          timestamp: timestamp,
        );

        context.read<StepBloc>().add(SubmitStepsEvent(stepRecord: stepRecord));
        _lastSyncedSteps = _realtimeSteps;

        // Show warnings if any
        if (validationResult.warnings.isNotEmpty && mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('Warning: ${validationResult.warnings.first}'),
              backgroundColor: Colors.orange,
              duration: const Duration(seconds: 2),
            ),
          );
        }
      } else {
        // Show validation error
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content:
                  Text('Validation Error: ${validationResult.errorMessage}'),
              backgroundColor: Colors.red,
              duration: const Duration(seconds: 3),
            ),
          );
        }
      }
    }
  }

  void _manualSync() {
    _syncStepsWithBackend();
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('Steps synced successfully!'),
        backgroundColor: Colors.green,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    if (!_hasPermissions) {
      return _buildPermissionCard();
    }

    if (!_isInitialized) {
      return _buildLoadingCard();
    }

    if (_errorMessage.isNotEmpty) {
      return _buildErrorCard();
    }

    return _buildStepCounterCard();
  }

  Widget _buildPermissionCard() {
    return Card(
      elevation: 4,
      margin: const EdgeInsets.all(16),
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              Icons.security,
              size: 48,
              color: Colors.orange[400],
            ),
            const SizedBox(height: 16),
            const Text(
              'Permission Required',
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 8),
            const Text(
              'This app needs permission to count your steps automatically.',
              textAlign: TextAlign.center,
              style: TextStyle(color: Colors.grey),
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: () => _initializePedometer(),
              child: const Text('Grant Permission'),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildLoadingCard() {
    return Card(
      elevation: 4,
      margin: const EdgeInsets.all(16),
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const CircularProgressIndicator(),
            const SizedBox(height: 16),
            Text(
              'Initializing Step Counter...',
              style: TextStyle(
                fontSize: 16,
                color: Colors.grey[600],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildErrorCard() {
    return Card(
      elevation: 4,
      margin: const EdgeInsets.all(16),
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              Icons.error,
              size: 48,
              color: Colors.red[400],
            ),
            const SizedBox(height: 16),
            Text(
              'Step Counter Error',
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.bold,
                color: Colors.red[600],
              ),
            ),
            const SizedBox(height: 8),
            Text(
              _errorMessage,
              textAlign: TextAlign.center,
              style: const TextStyle(color: Colors.grey),
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: () {
                setState(() {
                  _errorMessage = '';
                });
                _initializePedometer();
              },
              child: const Text('Retry'),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildStepCounterCard() {
    final energy = (_realtimeSteps / 10).floor();
    final progress = (_realtimeSteps / 8000).clamp(0.0, 1.0);
    final isGoalReached = _realtimeSteps >= 8000;

    return Card(
      elevation: 4,
      margin: const EdgeInsets.all(16),
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            _buildHeader(),
            const SizedBox(height: 20),
            _buildStepDisplay(),
            const SizedBox(height: 16),
            _buildEnergyDisplay(energy),
            const SizedBox(height: 20),
            _buildProgressBar(progress),
            const SizedBox(height: 10),
            if (isGoalReached) _buildGoalReachedBadge(),
            const SizedBox(height: 16),
            _buildSyncButton(),
          ],
        ),
      ),
    );
  }

  Widget _buildHeader() {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Icon(
          Icons.directions_walk,
          size: 28,
          color: Colors.blue[600],
        ),
        const SizedBox(width: 8),
        Text(
          'Daily Steps (Live)',
          style: TextStyle(
            fontSize: 22,
            fontWeight: FontWeight.bold,
            color: Colors.blue[600],
          ),
        ),
        const SizedBox(width: 8),
        Container(
          width: 8,
          height: 8,
          decoration: const BoxDecoration(
            color: Colors.green,
            shape: BoxShape.circle,
          ),
        ),
      ],
    );
  }

  Widget _buildStepDisplay() {
    return Column(
      children: [
        Text(
          NumberFormat('#,###').format(_realtimeSteps),
          style: const TextStyle(
            fontSize: 36,
            fontWeight: FontWeight.bold,
            color: Colors.black87,
          ),
        ),
        const Text(
          'Steps',
          style: TextStyle(
            fontSize: 16,
            color: Colors.grey,
          ),
        ),
      ],
    );
  }

  Widget _buildEnergyDisplay(int energy) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      decoration: BoxDecoration(
        color: Colors.orange[50],
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.orange[200]!),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(
            Icons.bolt,
            color: Colors.orange[600],
            size: 20,
          ),
          const SizedBox(width: 4),
          Text(
            '${NumberFormat('#,###').format(energy)} Energy',
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w600,
              color: Colors.orange[700],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildProgressBar(double progress) {
    return Column(
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              'Daily Goal Progress',
              style: TextStyle(
                fontSize: 14,
                color: Colors.grey[600],
              ),
            ),
            Text(
              '${(progress * 100).toInt()}%',
              style: TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.w600,
                color: Colors.grey[600],
              ),
            ),
          ],
        ),
        const SizedBox(height: 8),
        LinearProgressIndicator(
          value: progress,
          backgroundColor: Colors.grey[200],
          valueColor: AlwaysStoppedAnimation<Color>(
            progress >= 1.0 ? Colors.green[600]! : Colors.blue[600]!,
          ),
          minHeight: 8,
        ),
      ],
    );
  }

  Widget _buildGoalReachedBadge() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: Colors.green[50],
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: Colors.green[200]!),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(
            Icons.check_circle,
            color: Colors.green[600],
            size: 16,
          ),
          const SizedBox(width: 4),
          Text(
            'Goal Reached! 🎉',
            style: TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w600,
              color: Colors.green[700],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSyncButton() {
    final hasNewSteps = _realtimeSteps > _lastSyncedSteps;

    return ElevatedButton.icon(
      onPressed: hasNewSteps ? _manualSync : null,
      icon: Icon(
        hasNewSteps ? Icons.sync : Icons.sync_disabled,
        size: 16,
      ),
      label: Text(
        hasNewSteps
            ? 'Sync ${_realtimeSteps - _lastSyncedSteps} New Steps'
            : 'Steps Synced',
      ),
      style: ElevatedButton.styleFrom(
        backgroundColor: hasNewSteps ? Colors.blue[600] : Colors.grey[400],
        foregroundColor: Colors.white,
      ),
    );
  }
}

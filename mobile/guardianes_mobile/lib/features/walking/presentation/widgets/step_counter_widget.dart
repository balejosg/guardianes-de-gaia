import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:guardianes_mobile/features/walking/presentation/bloc/step_bloc.dart';
import 'package:guardianes_mobile/features/walking/presentation/bloc/step_state.dart' as step_state;
import 'package:intl/intl.dart';

class StepCounterWidget extends StatelessWidget {
  const StepCounterWidget({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<StepBloc, step_state.StepState>(
      builder: (context, state) {
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
                _buildStepDisplay(state),
                const SizedBox(height: 16),
                _buildEnergyDisplay(state),
                const SizedBox(height: 20),
                _buildProgressBar(state),
                const SizedBox(height: 10),
                _buildGoalStatus(state),
              ],
            ),
          ),
        );
      },
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
          'Daily Steps',
          style: TextStyle(
            fontSize: 22,
            fontWeight: FontWeight.bold,
            color: Colors.blue[600],
          ),
        ),
      ],
    );
  }

  Widget _buildStepDisplay(step_state.StepState state) {
    return Column(
      children: [
        Text(
          _getStepCountText(state),
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

  Widget _buildEnergyDisplay(step_state.StepState state) {
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
            _getEnergyText(state),
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

  Widget _buildProgressBar(step_state.StepState state) {
    final progress = _getProgress(state);
    return Column(
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              'Progress',
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

  Widget _buildGoalStatus(step_state.StepState state) {
    if (state is step_state.StepLoaded && state.currentSteps.isGoalReached()) {
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
              'Goal Reached!',
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
    return const SizedBox.shrink();
  }

  String _getStepCountText(step_state.StepState state) {
    if (state is step_state.StepLoading) {
      return '...';
    } else if (state is step_state.StepLoaded) {
      return NumberFormat('#,###').format(state.currentSteps.totalSteps);
    } else if (state is step_state.StepError) {
      return '--';
    }
    return '--';
  }

  String _getEnergyText(step_state.StepState state) {
    if (state is step_state.StepLoading) {
      return '-- Energy';
    } else if (state is step_state.StepLoaded) {
      final energy = state.currentSteps.calculateTotalEnergy();
      return '${NumberFormat('#,###').format(energy)} Energy';
    } else if (state is step_state.StepError) {
      return '-- Energy';
    }
    return '-- Energy';
  }

  double _getProgress(step_state.StepState state) {
    if (state is step_state.StepLoaded) {
      return (state.currentSteps.totalSteps / 8000).clamp(0.0, 1.0);
    }
    return 0.0;
  }
}

// Error display widget
class StepCounterError extends StatelessWidget {
  final String message;

  const StepCounterError({Key? key, required this.message}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 4,
      margin: const EdgeInsets.all(16),
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.error,
              size: 48,
              color: Colors.red[400],
            ),
            const SizedBox(height: 12),
            Text(
              'Error: $message',
              style: TextStyle(
                fontSize: 16,
                color: Colors.red[600],
              ),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 12),
            ElevatedButton(
              onPressed: () {
                // Retry logic would go here
              },
              child: const Text('Retry'),
            ),
          ],
        ),
      ),
    );
  }
}
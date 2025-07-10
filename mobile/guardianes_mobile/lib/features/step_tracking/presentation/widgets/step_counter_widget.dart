import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../bloc/step_tracking_bloc.dart';
import '../bloc/step_tracking_state.dart';

class StepCounterWidget extends StatelessWidget {
  const StepCounterWidget({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<StepTrackingBloc, StepTrackingState>(
      builder: (context, state) {
        if (state is StepTrackingLoaded && state.currentStepCount != null) {
          final stepCount = state.currentStepCount!;
          return Card(
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                children: [
                  Text(
                    'Today\'s Steps',
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
                  const SizedBox(height: 16),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                    children: [
                      _buildStatItem(
                        context,
                        'Steps',
                        stepCount.currentSteps.toString(),
                        Icons.directions_walk,
                      ),
                      _buildStatItem(
                        context,
                        'Energy',
                        stepCount.totalEnergy.toString(),
                        Icons.flash_on,
                      ),
                    ],
                  ),
                ],
              ),
            ),
          );
        }
        return const Card(
          child: Padding(
            padding: EdgeInsets.all(16.0),
            child: Center(child: CircularProgressIndicator()),
          ),
        );
      },
    );
  }

  Widget _buildStatItem(BuildContext context, String label, String value, IconData icon) {
    return Column(
      children: [
        Icon(icon, size: 32, color: Theme.of(context).colorScheme.primary),
        const SizedBox(height: 8),
        Text(
          value,
          style: Theme.of(context).textTheme.headlineMedium?.copyWith(
            fontWeight: FontWeight.bold,
          ),
        ),
        Text(
          label,
          style: Theme.of(context).textTheme.bodyMedium,
        ),
      ],
    );
  }
}
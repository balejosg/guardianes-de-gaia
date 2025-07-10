import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../bloc/step_tracking_bloc.dart';
import '../bloc/step_tracking_state.dart';

class EnergyBalanceWidget extends StatelessWidget {
  const EnergyBalanceWidget({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<StepTrackingBloc, StepTrackingState>(
      builder: (context, state) {
        if (state is StepTrackingLoaded && state.energyBalance != null) {
          final balance = state.energyBalance!;
          return Card(
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Energy Balance',
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
                  const SizedBox(height: 16),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      _buildBalanceItem(context, 'Current', balance.currentBalance, Colors.green),
                      _buildBalanceItem(context, 'Earned', balance.totalEarned, Colors.blue),
                      _buildBalanceItem(context, 'Spent', balance.totalSpent, Colors.orange),
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

  Widget _buildBalanceItem(BuildContext context, String label, int value, Color color) {
    return Column(
      children: [
        Text(
          value.toString(),
          style: Theme.of(context).textTheme.headlineSmall?.copyWith(
            fontWeight: FontWeight.bold,
            color: color,
          ),
        ),
        Text(
          label,
          style: Theme.of(context).textTheme.bodySmall,
        ),
      ],
    );
  }
}
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:guardianes_mobile/features/walking/presentation/bloc/step_bloc.dart';
import 'package:guardianes_mobile/features/walking/presentation/bloc/step_state.dart' as step_state;
import 'package:guardianes_mobile/features/walking/domain/entities/daily_step_aggregate.dart';
import 'package:intl/intl.dart';

class StepHistoryWidget extends StatelessWidget {
  const StepHistoryWidget({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<StepBloc, step_state.StepState>(
      builder: (context, state) {
        if (state is step_state.StepLoading) {
          return const Center(child: CircularProgressIndicator());
        } else if (state is step_state.StepHistoryLoaded) {
          return _buildHistoryList(state.stepHistory);
        } else if (state is step_state.StepError) {
          return _buildErrorWidget(state.message);
        }
        return _buildEmptyState();
      },
    );
  }

  Widget _buildHistoryList(List<DailyStepAggregate> stepHistory) {
    if (stepHistory.isEmpty) {
      return _buildEmptyState();
    }

    return Column(
      children: [
        _buildWeeklySummary(stepHistory),
        const SizedBox(height: 16),
        Expanded(
          child: ListView.builder(
            itemCount: stepHistory.length,
            itemBuilder: (context, index) {
              final dayData = stepHistory[index];
              return _buildHistoryItem(dayData);
            },
          ),
        ),
      ],
    );
  }

  Widget _buildHistoryItem(DailyStepAggregate dayData) {
    final isGoalReached = dayData.isGoalReached();
    final energy = dayData.calculateTotalEnergy();
    
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            // Date
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  dayData.date,
                  style: const TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                Text(
                  _formatDayOfWeek(dayData.date),
                  style: TextStyle(
                    fontSize: 12,
                    color: Colors.grey[600],
                  ),
                ),
              ],
            ),
            const Spacer(),
            // Step count and energy
            Column(
              crossAxisAlignment: CrossAxisAlignment.end,
              children: [
                Text(
                  '${NumberFormat('#,###').format(dayData.totalSteps)} steps',
                  style: const TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                Text(
                  '${NumberFormat('#,###').format(energy)} energy',
                  style: TextStyle(
                    fontSize: 12,
                    color: Colors.orange[600],
                  ),
                ),
              ],
            ),
            const SizedBox(width: 12),
            // Goal status
            if (isGoalReached)
              Icon(
                Icons.check_circle,
                color: Colors.green[600],
                size: 24,
              )
            else
              Icon(
                Icons.radio_button_unchecked,
                color: Colors.grey[400],
                size: 24,
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildWeeklySummary(List<DailyStepAggregate> stepHistory) {
    if (stepHistory.length < 7) {
      return const SizedBox.shrink();
    }

    final totalSteps = stepHistory.fold<int>(
      0,
      (sum, day) => sum + day.totalSteps,
    );
    final averageSteps = totalSteps / stepHistory.length;

    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 16),
      color: Colors.blue[50],
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            Row(
              children: [
                Icon(
                  Icons.bar_chart,
                  color: Colors.blue[600],
                  size: 20,
                ),
                const SizedBox(width: 8),
                Text(
                  'Weekly Summary',
                  style: TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.w600,
                    color: Colors.blue[700],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceAround,
              children: [
                Expanded(
                  child: Text(
                    'Weekly Total: ${NumberFormat('#,###').format(totalSteps)} steps',
                    style: TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.w600,
                      color: Colors.blue[600],
                    ),
                  ),
                ),
                Expanded(
                  child: Text(
                    'Average: ${NumberFormat('#,###').format(averageSteps.round())} steps/day',
                    style: TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.w600,
                      color: Colors.green[600],
                    ),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }


  Widget _buildEmptyState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(
            Icons.history,
            size: 64,
            color: Colors.grey[400],
          ),
          const SizedBox(height: 16),
          Text(
            'No step history available',
            style: TextStyle(
              fontSize: 18,
              color: Colors.grey[600],
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'Start walking to see your progress!',
            style: TextStyle(
              fontSize: 14,
              color: Colors.grey[500],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildErrorWidget(String message) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(
            Icons.error,
            size: 64,
            color: Colors.red[400],
          ),
          const SizedBox(height: 16),
          Text(
            'Error: $message',
            style: TextStyle(
              fontSize: 16,
              color: Colors.red[600],
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 16),
          ElevatedButton(
            onPressed: () {
              // Retry logic would go here
            },
            child: const Text('Retry'),
          ),
        ],
      ),
    );
  }


  String _formatDayOfWeek(String dateString) {
    try {
      final date = DateTime.parse(dateString);
      return DateFormat('EEEE').format(date);
    } catch (e) {
      return '';
    }
  }
}
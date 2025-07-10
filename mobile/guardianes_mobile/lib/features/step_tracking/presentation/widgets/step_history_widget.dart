import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../bloc/step_tracking_bloc.dart';
import '../bloc/step_tracking_state.dart';
import '../bloc/step_tracking_event.dart';
import '../../../../core/auth/auth_service.dart';
import 'package:intl/intl.dart';

class StepHistoryWidget extends StatefulWidget {
  const StepHistoryWidget({super.key});

  @override
  State<StepHistoryWidget> createState() => _StepHistoryWidgetState();
}

class _StepHistoryWidgetState extends State<StepHistoryWidget> {
  final _authService = AuthService();

  @override
  void initState() {
    super.initState();
    _loadHistory();
  }

  Future<void> _loadHistory() async {
    final guardianId = await _authService.getCurrentGuardianId();
    if (guardianId != null && mounted) {
      context.read<StepTrackingBloc>().add(LoadStepHistory(guardianId, days: 7));
    }
  }

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<StepTrackingBloc, StepTrackingState>(
      builder: (context, state) {
        if (state is StepTrackingLoaded && state.stepHistory != null) {
          final history = state.stepHistory!;
          return Card(
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Step History (Last 7 days)',
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
                  const SizedBox(height: 16),
                  Expanded(
                    child: history.entries.isEmpty
                        ? const Center(
                            child: Text('No step history available'),
                          )
                        : ListView.builder(
                            itemCount: history.entries.length,
                            itemBuilder: (context, index) {
                              final entry = history.entries[index];
                              return ListTile(
                                leading: Icon(
                                  Icons.directions_walk,
                                  color: Theme.of(context).colorScheme.primary,
                                ),
                                title: Text(
                                  DateFormat('MMM dd, yyyy').format(entry.date),
                                ),
                                subtitle: Text('${entry.stepCount} steps'),
                                trailing: Row(
                                  mainAxisSize: MainAxisSize.min,
                                  children: [
                                    Icon(
                                      Icons.flash_on,
                                      size: 16,
                                      color: Colors.orange,
                                    ),
                                    const SizedBox(width: 4),
                                    Text(
                                      '${entry.energyEarned}',
                                      style: const TextStyle(
                                        fontWeight: FontWeight.bold,
                                        color: Colors.orange,
                                      ),
                                    ),
                                  ],
                                ),
                              );
                            },
                          ),
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
}
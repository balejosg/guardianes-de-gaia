import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:guardianes_mobile/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:guardianes_mobile/features/walking/domain/entities/step_record.dart';
import 'package:guardianes_mobile/features/walking/domain/validators/step_validation.dart';
import 'package:guardianes_mobile/features/walking/presentation/bloc/step_bloc.dart';
import 'package:guardianes_mobile/features/walking/presentation/bloc/step_event.dart';
import 'package:guardianes_mobile/features/walking/presentation/bloc/step_state.dart'
    as step_state;
import 'package:guardianes_mobile/features/walking/presentation/widgets/realtime_step_counter_widget.dart';
import 'package:guardianes_mobile/features/walking/presentation/widgets/step_history_widget.dart';

class StepTrackingPage extends StatefulWidget {
  const StepTrackingPage({Key? key}) : super(key: key);

  @override
  State<StepTrackingPage> createState() => _StepTrackingPageState();
}

class _StepTrackingPageState extends State<StepTrackingPage>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;
  final _stepFormKey = GlobalKey<FormState>();
  final _stepController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);

    // Load initial data with guardian ID from auth state
    final authState = context.read<AuthBloc>().state;
    if (authState is AuthAuthenticated) {
      context
          .read<StepBloc>()
          .add(GetCurrentStepsEvent(guardianId: authState.guardian.id));
    }

    // Listen for tab changes
    _tabController.addListener(() {
      if (_tabController.index == 1) {
        // History tab selected
        final authState = context.read<AuthBloc>().state;
        if (authState is AuthAuthenticated) {
          final now = DateTime.now();
          final weekAgo = now.subtract(const Duration(days: 7));
          context.read<StepBloc>().add(GetStepHistoryEvent(
                guardianId: authState.guardian.id,
                fromDate: weekAgo.toIso8601String().split('T')[0],
                toDate: now.toIso8601String().split('T')[0],
              ));
        }
      }
    });
  }

  @override
  void dispose() {
    _tabController.dispose();
    _stepController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Step Tracking'),
        backgroundColor: Colors.blue[600],
        foregroundColor: Colors.white,
        bottom: TabBar(
          controller: _tabController,
          indicatorColor: Colors.white,
          labelColor: Colors.white,
          unselectedLabelColor: Colors.white70,
          tabs: const [
            Tab(key: Key('step_today_tab'), text: 'Today', icon: Icon(Icons.today)),
            Tab(key: Key('step_history_tab'), text: 'History', icon: Icon(Icons.history)),
          ],
        ),
      ),
      body: BlocListener<StepBloc, step_state.StepState>(
        listener: (context, state) {
          if (state is step_state.StepSubmitted) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(
                content: Text('Steps submitted successfully!'),
                backgroundColor: Colors.green,
              ),
            );
            // Refresh current data
            final authState = context.read<AuthBloc>().state;
            if (authState is AuthAuthenticated) {
              context
                  .read<StepBloc>()
                  .add(GetCurrentStepsEvent(guardianId: authState.guardian.id));
            }
          } else if (state is step_state.StepSubmissionError) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                content: Text('Error: ${state.message}'),
                backgroundColor: Colors.red,
              ),
            );
          }
        },
        child: RefreshIndicator(
          onRefresh: _handleRefresh,
          child: TabBarView(
            controller: _tabController,
            children: [
              _buildTodayTab(),
              _buildHistoryTab(),
            ],
          ),
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _showManualStepEntryDialog,
        backgroundColor: Colors.blue[600],
        child: const Icon(Icons.add, color: Colors.white),
      ),
    );
  }

  Widget _buildTodayTab() {
    return SingleChildScrollView(
      physics: const AlwaysScrollableScrollPhysics(),
      child: Column(
        children: [
          const SizedBox(height: 20),
          BlocBuilder<AuthBloc, AuthState>(
            builder: (context, authState) {
              if (authState is AuthAuthenticated) {
                return RealtimeStepCounterWidget(
                    guardianId: authState.guardian.id);
              }
              return const SizedBox.shrink();
            },
          ),
          const SizedBox(height: 20),
          _buildTodayStats(),
          const SizedBox(height: 20),
          _buildQuickActions(),
        ],
      ),
    );
  }

  Widget _buildHistoryTab() {
    return const StepHistoryWidget();
  }

  Widget _buildTodayStats() {
    return BlocBuilder<StepBloc, step_state.StepState>(
      builder: (context, state) {
        if (state is step_state.StepLoaded) {
          final steps = state.currentSteps;
          final energy = steps.calculateTotalEnergy();
          final goalProgress = (steps.totalSteps / 8000).clamp(0.0, 1.0);

          return Card(
            margin: const EdgeInsets.symmetric(horizontal: 16),
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                children: [
                  Text(
                    'Today\'s Progress',
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
                  const SizedBox(height: 16),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceAround,
                    children: [
                      _buildStatItem(
                        'Goal Progress',
                        '${(goalProgress * 100).toInt()}%',
                        Icons.flag,
                        Colors.green,
                      ),
                      _buildStatItem(
                        'Energy',
                        '$energy',
                        Icons.bolt,
                        Colors.orange,
                      ),
                      _buildStatItem(
                        'Remaining',
                        '${(8000 - steps.totalSteps).clamp(0, 8000)}',
                        Icons.directions_walk,
                        Colors.blue,
                      ),
                    ],
                  ),
                ],
              ),
            ),
          );
        }
        return const SizedBox.shrink();
      },
    );
  }

  Widget _buildStatItem(
      String label, String value, IconData icon, Color color) {
    return Column(
      children: [
        Icon(icon, color: color, size: 32),
        const SizedBox(height: 4),
        Text(
          value,
          style: TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.bold,
            color: color,
          ),
        ),
        Text(
          label,
          style: TextStyle(
            fontSize: 12,
            color: Colors.grey[600],
          ),
        ),
      ],
    );
  }

  Widget _buildQuickActions() {
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 16),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Quick Actions',
              style: Theme.of(context).textTheme.titleMedium,
            ),
            const SizedBox(height: 12),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceAround,
              children: [
                _buildActionButton(
                  'Add Steps',
                  Icons.add,
                  Colors.blue,
                  _showManualStepEntryDialog,
                ),
                _buildActionButton(
                  'View History',
                  Icons.history,
                  Colors.green,
                  () => _tabController.animateTo(1),
                ),
                _buildActionButton(
                  'Refresh',
                  Icons.refresh,
                  Colors.orange,
                  () => _handleRefresh(),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildActionButton(
    String label,
    IconData icon,
    Color color,
    VoidCallback onPressed,
  ) {
    return Column(
      children: [
        ElevatedButton(
          onPressed: onPressed,
          style: ElevatedButton.styleFrom(
            backgroundColor: color,
            foregroundColor: Colors.white,
            shape: const CircleBorder(),
            padding: const EdgeInsets.all(16),
          ),
          child: Icon(icon, size: 24),
        ),
        const SizedBox(height: 4),
        Text(
          label,
          style: const TextStyle(fontSize: 12),
        ),
      ],
    );
  }

  Future<void> _handleRefresh() async {
    final authState = context.read<AuthBloc>().state;
    if (authState is AuthAuthenticated) {
      context
          .read<StepBloc>()
          .add(GetCurrentStepsEvent(guardianId: authState.guardian.id));

      if (_tabController.index == 1) {
        final now = DateTime.now();
        final weekAgo = now.subtract(const Duration(days: 7));
        context.read<StepBloc>().add(GetStepHistoryEvent(
              guardianId: authState.guardian.id,
              fromDate: weekAgo.toIso8601String().split('T')[0],
              toDate: now.toIso8601String().split('T')[0],
            ));
      }
    }
  }

  void _showManualStepEntryDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Add Steps'),
        content: Form(
          key: _stepFormKey,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextFormField(
                controller: _stepController,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(
                  labelText: 'Number of steps',
                  hintText: 'Enter step count',
                  border: OutlineInputBorder(),
                ),
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Please enter a number';
                  }
                  final steps = int.tryParse(value);
                  if (steps == null || steps <= 0) {
                    return 'Please enter a positive number';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 16),
              Text(
                'Steps will be added to today\'s total',
                style: TextStyle(
                  fontSize: 12,
                  color: Colors.grey[600],
                ),
              ),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () {
              Navigator.of(context).pop();
              _stepController.clear();
            },
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: _submitManualSteps,
            child: const Text('Add Steps'),
          ),
        ],
      ),
    );
  }

  void _submitManualSteps() {
    if (_stepFormKey.currentState?.validate() ?? false) {
      final stepCount = int.parse(_stepController.text);
      final timestamp = DateTime.now().toIso8601String();

      final authState = context.read<AuthBloc>().state;
      if (authState is AuthAuthenticated) {
        // Get current daily total from StepBloc state
        final stepState = context.read<StepBloc>().state;
        int currentDailyTotal = 0;
        if (stepState is step_state.StepLoaded) {
          currentDailyTotal = stepState.currentSteps.totalSteps;
        }

        // Validate step submission before sending
        final validationResult = StepValidator.validateStepSubmission(
          stepCount: stepCount,
          timestamp: timestamp,
          guardianId: authState.guardian.id,
          currentDailyTotal: currentDailyTotal,
        );

        if (validationResult.isValid) {
          final stepRecord = StepRecord(
            guardianId: authState.guardian.id,
            stepCount: stepCount,
            timestamp: timestamp,
          );

          context
              .read<StepBloc>()
              .add(SubmitStepsEvent(stepRecord: stepRecord));
          Navigator.of(context).pop();
          _stepController.clear();

          // Show warnings if any
          if (validationResult.warnings.isNotEmpty) {
            Future.delayed(const Duration(milliseconds: 500), () {
              if (mounted) {
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(
                    content:
                        Text('Warning: ${validationResult.warnings.first}'),
                    backgroundColor: Colors.orange,
                    duration: const Duration(seconds: 3),
                  ),
                );
              }
            });
          }
        } else {
          // Show validation error
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('${validationResult.errorMessage}'),
              backgroundColor: Colors.red,
              duration: const Duration(seconds: 3),
            ),
          );
        }
      }
    }
  }
}

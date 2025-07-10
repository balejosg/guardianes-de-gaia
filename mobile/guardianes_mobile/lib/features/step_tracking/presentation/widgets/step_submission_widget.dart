import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../bloc/step_tracking_bloc.dart';
import '../bloc/step_tracking_state.dart';
import '../bloc/step_tracking_event.dart';
import '../../../../core/auth/auth_service.dart';

class StepSubmissionWidget extends StatefulWidget {
  const StepSubmissionWidget({super.key});

  @override
  State<StepSubmissionWidget> createState() => _StepSubmissionWidgetState();
}

class _StepSubmissionWidgetState extends State<StepSubmissionWidget> {
  final _formKey = GlobalKey<FormState>();
  final _stepController = TextEditingController();
  final _authService = AuthService();

  @override
  void dispose() {
    _stepController.dispose();
    super.dispose();
  }

  Future<void> _submitSteps() async {
    if (!_formKey.currentState!.validate()) return;

    final guardianId = await _authService.getCurrentGuardianId();
    if (guardianId != null && mounted) {
      final stepCount = int.parse(_stepController.text);
      context.read<StepTrackingBloc>().add(SubmitSteps(guardianId, stepCount));
    }
  }

  @override
  Widget build(BuildContext context) {
    return BlocListener<StepTrackingBloc, StepTrackingState>(
      listener: (context, state) {
        if (state is StepSubmissionSuccess) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(
                'Steps submitted successfully! Earned ${state.result.energyEarned} energy',
              ),
              backgroundColor: Colors.green,
            ),
          );
          _stepController.clear();
        } else if (state is StepTrackingError) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(state.message),
              backgroundColor: Colors.red,
            ),
          );
        }
      },
      child: Card(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Form(
            key: _formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Text(
                  'Submit Your Steps',
                  style: Theme.of(context).textTheme.titleLarge,
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: _stepController,
                  keyboardType: TextInputType.number,
                  decoration: const InputDecoration(
                    labelText: 'Number of Steps',
                    prefixIcon: Icon(Icons.directions_walk),
                    helperText: '10 steps = 1 energy',
                  ),
                  validator: (value) {
                    if (value == null || value.isEmpty) {
                      return 'Please enter the number of steps';
                    }
                    final steps = int.tryParse(value);
                    if (steps == null || steps <= 0) {
                      return 'Please enter a valid number of steps';
                    }
                    if (steps > 50000) {
                      return 'Maximum 50,000 steps allowed per submission';
                    }
                    return null;
                  },
                ),
                const SizedBox(height: 24),
                BlocBuilder<StepTrackingBloc, StepTrackingState>(
                  builder: (context, state) {
                    final isLoading = state is StepSubmissionInProgress;
                    return ElevatedButton(
                      onPressed: isLoading ? null : _submitSteps,
                      child: isLoading
                          ? const SizedBox(
                              height: 20,
                              width: 20,
                              child: CircularProgressIndicator(strokeWidth: 2),
                            )
                          : const Text('Submit Steps'),
                    );
                  },
                ),
                const SizedBox(height: 16),
                Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: Theme.of(context).colorScheme.surfaceContainerHighest,
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Icon(
                            Icons.info,
                            size: 16,
                            color: Theme.of(context).colorScheme.primary,
                          ),
                          const SizedBox(width: 8),
                          Text(
                            'Step Submission Info',
                            style: Theme.of(context).textTheme.titleSmall,
                          ),
                        ],
                      ),
                      const SizedBox(height: 8),
                      const Text(
                        '• 10 steps = 1 energy\n'
                        '• Maximum 50,000 steps per submission\n'
                        '• Steps are tracked daily\n'
                        '• Energy can be used for card battles',
                        style: TextStyle(fontSize: 12),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
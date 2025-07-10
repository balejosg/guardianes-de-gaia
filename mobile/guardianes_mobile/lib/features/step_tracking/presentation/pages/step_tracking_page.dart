import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import '../bloc/step_tracking_bloc.dart';
import '../bloc/step_tracking_event.dart';
import '../widgets/step_counter_widget.dart';
import '../widgets/energy_balance_widget.dart';
import '../widgets/step_history_widget.dart';
import '../widgets/step_submission_widget.dart';
import '../../../../core/auth/auth_service.dart';

class StepTrackingPage extends StatefulWidget {
  const StepTrackingPage({super.key});

  @override
  State<StepTrackingPage> createState() => _StepTrackingPageState();
}

class _StepTrackingPageState extends State<StepTrackingPage> {
  final _authService = AuthService();
  int _currentIndex = 0;

  @override
  void initState() {
    super.initState();
    _loadInitialData();
  }

  Future<void> _loadInitialData() async {
    final guardianId = await _authService.getCurrentGuardianId();
    if (guardianId != null && mounted) {
      context.read<StepTrackingBloc>().add(LoadCurrentStepCount(guardianId));
      context.read<StepTrackingBloc>().add(LoadEnergyBalance(guardianId));
    }
  }

  Future<void> _logout() async {
    await _authService.logout();
    if (mounted) {
      context.go('/login');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Guardianes de Gaia'),
        actions: [
          PopupMenuButton<String>(
            onSelected: (value) {
              if (value == 'logout') {
                _logout();
              }
            },
            itemBuilder: (context) => [
              const PopupMenuItem(
                value: 'logout',
                child: Row(
                  children: [
                    Icon(Icons.logout),
                    SizedBox(width: 8),
                    Text('Logout'),
                  ],
                ),
              ),
            ],
          ),
        ],
      ),
      body: IndexedStack(
        index: _currentIndex,
        children: const [
          _DashboardTab(),
          _HistoryTab(),
          _SubmissionTab(),
        ],
      ),
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _currentIndex,
        onTap: (index) => setState(() => _currentIndex = index),
        items: const [
          BottomNavigationBarItem(
            icon: Icon(Icons.dashboard),
            label: 'Dashboard',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.history),
            label: 'History',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.add),
            label: 'Submit Steps',
          ),
        ],
      ),
    );
  }
}

class _DashboardTab extends StatelessWidget {
  const _DashboardTab();

  @override
  Widget build(BuildContext context) {
    return const Padding(
      padding: EdgeInsets.all(16.0),
      child: Column(
        children: [
          StepCounterWidget(),
          SizedBox(height: 16),
          EnergyBalanceWidget(),
          SizedBox(height: 16),
          Expanded(
            child: Card(
              child: Padding(
                padding: EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Today\'s Progress',
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    SizedBox(height: 16),
                    Expanded(
                      child: Center(
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Icon(
                              Icons.directions_walk,
                              size: 64,
                              color: Colors.green,
                            ),
                            SizedBox(height: 16),
                            Text(
                              'Keep walking to earn more energy!',
                              style: TextStyle(fontSize: 16),
                              textAlign: TextAlign.center,
                            ),
                          ],
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _HistoryTab extends StatelessWidget {
  const _HistoryTab();

  @override
  Widget build(BuildContext context) {
    return const Padding(
      padding: EdgeInsets.all(16.0),
      child: StepHistoryWidget(),
    );
  }
}

class _SubmissionTab extends StatelessWidget {
  const _SubmissionTab();

  @override
  Widget build(BuildContext context) {
    return const Padding(
      padding: EdgeInsets.all(16.0),
      child: StepSubmissionWidget(),
    );
  }
}
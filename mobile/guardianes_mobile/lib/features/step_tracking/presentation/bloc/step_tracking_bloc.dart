import 'package:flutter_bloc/flutter_bloc.dart';
import '../../domain/usecases/get_current_step_count.dart';
import '../../domain/usecases/get_energy_balance.dart';
import '../../domain/usecases/get_step_history.dart';
import '../../domain/usecases/submit_steps.dart';
import 'step_tracking_event.dart';
import 'step_tracking_state.dart';

class StepTrackingBloc extends Bloc<StepTrackingEvent, StepTrackingState> {
  final GetCurrentStepCountUseCase _getCurrentStepCountUseCase;
  final GetEnergyBalanceUseCase _getEnergyBalanceUseCase;
  final GetStepHistoryUseCase _getStepHistoryUseCase;
  final SubmitStepsUseCase _submitStepsUseCase;

  StepTrackingBloc({
    required GetCurrentStepCountUseCase getCurrentStepCountUseCase,
    required GetEnergyBalanceUseCase getEnergyBalanceUseCase,
    required GetStepHistoryUseCase getStepHistoryUseCase,
    required SubmitStepsUseCase submitStepsUseCase,
  })  : _getCurrentStepCountUseCase = getCurrentStepCountUseCase,
        _getEnergyBalanceUseCase = getEnergyBalanceUseCase,
        _getStepHistoryUseCase = getStepHistoryUseCase,
        _submitStepsUseCase = submitStepsUseCase,
        super(StepTrackingInitial()) {
    on<LoadCurrentStepCount>(_onLoadCurrentStepCount);
    on<LoadEnergyBalance>(_onLoadEnergyBalance);
    on<LoadStepHistory>(_onLoadStepHistory);
    on<SubmitSteps>(_onSubmitSteps);
    on<RefreshData>(_onRefreshData);
  }

  Future<void> _onLoadCurrentStepCount(
    LoadCurrentStepCount event,
    Emitter<StepTrackingState> emit,
  ) async {
    try {
      final currentStepCount = await _getCurrentStepCountUseCase(event.guardianId);
      
      if (state is StepTrackingLoaded) {
        emit((state as StepTrackingLoaded).copyWith(
          currentStepCount: currentStepCount,
        ));
      } else {
        emit(StepTrackingLoaded(currentStepCount: currentStepCount));
      }
    } catch (e) {
      emit(StepTrackingError('Failed to load current step count: ${e.toString()}'));
    }
  }

  Future<void> _onLoadEnergyBalance(
    LoadEnergyBalance event,
    Emitter<StepTrackingState> emit,
  ) async {
    try {
      final energyBalance = await _getEnergyBalanceUseCase(event.guardianId);
      
      if (state is StepTrackingLoaded) {
        emit((state as StepTrackingLoaded).copyWith(
          energyBalance: energyBalance,
        ));
      } else {
        emit(StepTrackingLoaded(energyBalance: energyBalance));
      }
    } catch (e) {
      emit(StepTrackingError('Failed to load energy balance: ${e.toString()}'));
    }
  }

  Future<void> _onLoadStepHistory(
    LoadStepHistory event,
    Emitter<StepTrackingState> emit,
  ) async {
    try {
      final stepHistory = await _getStepHistoryUseCase(event.guardianId, days: event.days);
      
      if (state is StepTrackingLoaded) {
        emit((state as StepTrackingLoaded).copyWith(
          stepHistory: stepHistory,
        ));
      } else {
        emit(StepTrackingLoaded(stepHistory: stepHistory));
      }
    } catch (e) {
      emit(StepTrackingError('Failed to load step history: ${e.toString()}'));
    }
  }

  Future<void> _onSubmitSteps(
    SubmitSteps event,
    Emitter<StepTrackingState> emit,
  ) async {
    emit(StepSubmissionInProgress());
    
    try {
      final result = await _submitStepsUseCase(event.guardianId, event.stepCount);
      emit(StepSubmissionSuccess(result));
      
      // Refresh data after successful submission
      add(RefreshData(event.guardianId));
    } catch (e) {
      emit(StepTrackingError('Failed to submit steps: ${e.toString()}'));
    }
  }

  Future<void> _onRefreshData(
    RefreshData event,
    Emitter<StepTrackingState> emit,
  ) async {
    try {
      final currentStepCount = await _getCurrentStepCountUseCase(event.guardianId);
      final energyBalance = await _getEnergyBalanceUseCase(event.guardianId);
      final stepHistory = await _getStepHistoryUseCase(event.guardianId, days: 7);
      
      emit(StepTrackingLoaded(
        currentStepCount: currentStepCount,
        energyBalance: energyBalance,
        stepHistory: stepHistory,
      ));
    } catch (e) {
      emit(StepTrackingError('Failed to refresh data: ${e.toString()}'));
    }
  }
}
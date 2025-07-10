import '../../features/step_tracking/data/datasources/step_tracking_remote_data_source.dart';
import '../../features/step_tracking/data/repositories/step_tracking_repository_impl.dart';
import '../../features/step_tracking/domain/repositories/step_tracking_repository.dart';
import '../../features/step_tracking/domain/usecases/get_current_step_count.dart';
import '../../features/step_tracking/domain/usecases/get_energy_balance.dart';
import '../../features/step_tracking/domain/usecases/get_step_history.dart';
import '../../features/step_tracking/domain/usecases/submit_steps.dart';
import '../../features/step_tracking/presentation/bloc/step_tracking_bloc.dart';
import '../api/api_client.dart';

class DependencyInjection {
  static late ApiClient _apiClient;
  static late StepTrackingRemoteDataSource _stepTrackingRemoteDataSource;
  static late StepTrackingRepository _stepTrackingRepository;
  static late GetCurrentStepCountUseCase _getCurrentStepCountUseCase;
  static late GetEnergyBalanceUseCase _getEnergyBalanceUseCase;
  static late GetStepHistoryUseCase _getStepHistoryUseCase;
  static late SubmitStepsUseCase _submitStepsUseCase;

  static void initialize() {
    // Core
    _apiClient = ApiClient();
    _apiClient.initialize();

    // Data Sources
    _stepTrackingRemoteDataSource = StepTrackingRemoteDataSourceImpl(_apiClient);

    // Repositories
    _stepTrackingRepository = StepTrackingRepositoryImpl(_stepTrackingRemoteDataSource);

    // Use Cases
    _getCurrentStepCountUseCase = GetCurrentStepCountUseCase(_stepTrackingRepository);
    _getEnergyBalanceUseCase = GetEnergyBalanceUseCase(_stepTrackingRepository);
    _getStepHistoryUseCase = GetStepHistoryUseCase(_stepTrackingRepository);
    _submitStepsUseCase = SubmitStepsUseCase(_stepTrackingRepository);
  }

  static StepTrackingBloc createStepTrackingBloc() {
    return StepTrackingBloc(
      getCurrentStepCountUseCase: _getCurrentStepCountUseCase,
      getEnergyBalanceUseCase: _getEnergyBalanceUseCase,
      getStepHistoryUseCase: _getStepHistoryUseCase,
      submitStepsUseCase: _submitStepsUseCase,
    );
  }
}
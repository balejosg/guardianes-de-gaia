import 'package:get_it/get_it.dart';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';

// Features
import 'package:guardianes_mobile/features/auth/data/datasources/auth_local_datasource.dart';
import 'package:guardianes_mobile/features/auth/data/datasources/auth_remote_datasource.dart';
import 'package:guardianes_mobile/features/auth/data/datasources/guardian_profile_remote_datasource.dart';
import 'package:guardianes_mobile/features/auth/data/repositories/auth_repository_impl.dart';
import 'package:guardianes_mobile/features/auth/domain/repositories/auth_repository.dart';
import 'package:guardianes_mobile/features/auth/domain/usecases/login_guardian.dart';
import 'package:guardianes_mobile/features/auth/domain/usecases/register_guardian.dart';
import 'package:guardianes_mobile/features/auth/domain/usecases/get_guardian_profile.dart';
import 'package:guardianes_mobile/features/auth/presentation/bloc/auth_bloc.dart';
import 'package:guardianes_mobile/features/auth/presentation/bloc/guardian_profile_bloc.dart';

// Walking feature
import 'package:guardianes_mobile/features/walking/data/datasources/step_remote_datasource.dart';
import 'package:guardianes_mobile/features/walking/data/repositories/step_repository_impl.dart';
import 'package:guardianes_mobile/features/walking/data/services/pedometer_service.dart';
import 'package:guardianes_mobile/features/walking/domain/repositories/step_repository.dart';
import 'package:guardianes_mobile/features/walking/domain/usecases/get_current_steps.dart';
import 'package:guardianes_mobile/features/walking/domain/usecases/get_step_history.dart';
import 'package:guardianes_mobile/features/walking/domain/usecases/submit_steps.dart';
import 'package:guardianes_mobile/features/walking/presentation/bloc/step_bloc.dart';

// Cards feature
import 'package:guardianes_mobile/features/cards/data/datasources/card_remote_datasource.dart';
import 'package:guardianes_mobile/features/cards/data/repositories/card_repository_impl.dart';
import 'package:guardianes_mobile/features/cards/data/services/qr_scanner_service.dart';
import 'package:guardianes_mobile/features/cards/domain/repositories/card_repository.dart';
import 'package:guardianes_mobile/features/cards/domain/usecases/scan_qr_code.dart';
import 'package:guardianes_mobile/features/cards/domain/usecases/get_card_collection.dart';
import 'package:guardianes_mobile/features/cards/domain/usecases/get_collection_statistics.dart';
import 'package:guardianes_mobile/features/cards/domain/usecases/search_cards.dart';
import 'package:guardianes_mobile/features/cards/presentation/bloc/card_bloc.dart';

final getIt = GetIt.instance;

Future<void> configureDependencies() async {
  // External dependencies
  final sharedPreferences = await SharedPreferences.getInstance();
  getIt.registerLazySingleton(() => sharedPreferences);
  getIt.registerLazySingleton(() => http.Client());

  // Data sources
  getIt.registerLazySingleton<AuthLocalDataSource>(
    () => AuthLocalDataSourceImpl(sharedPreferences: getIt()),
  );

  getIt.registerLazySingleton<AuthRemoteDataSource>(
    () => AuthRemoteDataSourceImpl(client: getIt()),
  );

  getIt.registerLazySingleton<GuardianProfileRemoteDataSource>(
    () => GuardianProfileRemoteDataSourceImpl(client: getIt()),
  );

  getIt.registerLazySingleton<StepRemoteDataSource>(
    () => StepRemoteDataSourceImpl(getIt()),
  );

  // Services
  getIt.registerLazySingleton<PedometerService>(
    () => PedometerServiceImpl(),
  );

  // Repositories
  getIt.registerLazySingleton<AuthRepository>(
    () => AuthRepositoryImpl(
      remoteDataSource: getIt(),
      localDataSource: getIt(),
      profileDataSource: getIt(),
    ),
  );

  getIt.registerLazySingleton<StepRepository>(
    () => StepRepositoryImpl(getIt()),
  );

  // Use cases
  getIt.registerLazySingleton(() => LoginGuardian(getIt()));
  getIt.registerLazySingleton(() => RegisterGuardian(getIt()));
  getIt.registerLazySingleton(() => GetGuardianProfile(getIt()));
  getIt.registerLazySingleton(() => GetCurrentSteps(getIt()));
  getIt.registerLazySingleton(() => GetStepHistory(getIt()));
  getIt.registerLazySingleton(() => SubmitSteps(getIt()));

  // BLoCs
  getIt.registerFactory(
    () => AuthBloc(
      loginGuardian: getIt(),
      registerGuardian: getIt(),
      authRepository: getIt(),
    ),
  );

  getIt.registerFactory(
    () => StepBloc(
      getCurrentSteps: getIt(),
      getStepHistory: getIt(),
      submitSteps: getIt(),
    ),
  );

  getIt.registerFactory(
    () => GuardianProfileBloc(
      getGuardianProfile: getIt(),
    ),
  );

  // Card-related dependencies
  getIt.registerLazySingleton<CardRemoteDataSource>(
    () => CardRemoteDataSourceImpl(client: getIt()),
  );

  getIt.registerLazySingleton<QRScannerService>(
    () => QRScannerService(),
  );

  getIt.registerLazySingleton<CardRepository>(
    () => CardRepositoryImpl(remoteDataSource: getIt()),
  );

  // Card use cases
  getIt.registerLazySingleton(() => ScanQRCode(getIt()));
  getIt.registerLazySingleton(() => GetCardCollection(getIt()));
  getIt.registerLazySingleton(() => GetCollectionStatistics(getIt()));
  getIt.registerLazySingleton(() => SearchCards(getIt()));

  // Card BLoC
  getIt.registerFactory(
    () => CardBloc(
      scanQRCode: getIt(),
      getCardCollection: getIt(),
      getCollectionStatistics: getIt(),
      searchCards: getIt(),
      cardRepository: getIt(),
    ),
  );
}

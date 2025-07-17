import 'package:get_it/get_it.dart';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';

// Features
import 'package:guardianes_mobile/features/auth/data/datasources/auth_local_datasource.dart';
import 'package:guardianes_mobile/features/auth/data/datasources/auth_remote_datasource.dart';
import 'package:guardianes_mobile/features/auth/data/repositories/auth_repository_impl.dart';
import 'package:guardianes_mobile/features/auth/domain/repositories/auth_repository.dart';
import 'package:guardianes_mobile/features/auth/domain/usecases/login_guardian.dart';
import 'package:guardianes_mobile/features/auth/domain/usecases/register_guardian.dart';
import 'package:guardianes_mobile/features/auth/presentation/bloc/auth_bloc.dart';

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

  // Repositories
  getIt.registerLazySingleton<AuthRepository>(
    () => AuthRepositoryImpl(
      remoteDataSource: getIt(),
      localDataSource: getIt(),
    ),
  );

  // Use cases
  getIt.registerLazySingleton(() => LoginGuardian(getIt()));
  getIt.registerLazySingleton(() => RegisterGuardian(getIt()));

  // BLoCs
  getIt.registerFactory(
    () => AuthBloc(
      loginGuardian: getIt(),
      registerGuardian: getIt(),
      authRepository: getIt(),
    ),
  );
}
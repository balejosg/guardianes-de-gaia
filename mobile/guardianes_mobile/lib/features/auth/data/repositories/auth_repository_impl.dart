import 'package:guardianes_mobile/features/auth/data/datasources/auth_local_datasource.dart';
import 'package:guardianes_mobile/features/auth/data/datasources/auth_remote_datasource.dart';
import 'package:guardianes_mobile/features/auth/data/datasources/guardian_profile_remote_datasource.dart';
import 'package:guardianes_mobile/features/auth/domain/entities/auth_result.dart';
import 'package:guardianes_mobile/features/auth/domain/entities/guardian.dart';
import 'package:guardianes_mobile/features/auth/domain/repositories/auth_repository.dart';

class AuthRepositoryImpl implements AuthRepository {
  final AuthRemoteDataSource remoteDataSource;
  final AuthLocalDataSource localDataSource;
  final GuardianProfileRemoteDataSource profileDataSource;

  AuthRepositoryImpl({
    required this.remoteDataSource,
    required this.localDataSource,
    required this.profileDataSource,
  });

  @override
  Future<AuthResult> register({
    required String username,
    required String email,
    required String password,
    required String name,
    required DateTime birthDate,
  }) async {
    final result = await remoteDataSource.register(
      username: username,
      email: email,
      password: password,
      name: name,
      birthDate: birthDate,
    );

    await localDataSource.saveToken(result.token);
    await localDataSource.saveGuardian(result.guardianModel);

    return result;
  }

  @override
  Future<AuthResult> login({
    required String usernameOrEmail,
    required String password,
  }) async {
    final result = await remoteDataSource.login(
      usernameOrEmail: usernameOrEmail,
      password: password,
    );

    await localDataSource.saveToken(result.token);
    await localDataSource.saveGuardian(result.guardianModel);

    return result;
  }

  @override
  Future<void> logout() async {
    await localDataSource.removeToken();
    await localDataSource.removeGuardian();
  }

  @override
  Future<String?> getToken() async {
    return await localDataSource.getToken();
  }

  @override
  Future<void> saveToken(String token) async {
    await localDataSource.saveToken(token);
  }

  @override
  Future<Guardian?> getCurrentGuardian() async {
    return await localDataSource.getGuardian();
  }

  @override
  Future<Guardian> getGuardianProfile(int guardianId) async {
    final guardianModel = await profileDataSource.getGuardianProfile(guardianId);
    return guardianModel;
  }

  @override
  Future<bool> isLoggedIn() async {
    final token = await localDataSource.getToken();
    final guardian = await localDataSource.getGuardian();
    return token != null && guardian != null;
  }
}
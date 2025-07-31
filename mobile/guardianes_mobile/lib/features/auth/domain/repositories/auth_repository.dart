import 'package:guardianes_mobile/features/auth/domain/entities/auth_result.dart';
import 'package:guardianes_mobile/features/auth/domain/entities/guardian.dart';

abstract class AuthRepository {
  Future<AuthResult> register({
    required String username,
    required String email,
    required String password,
    required String name,
    required DateTime birthDate,
  });

  Future<AuthResult> login({
    required String usernameOrEmail,
    required String password,
  });

  Future<void> logout();

  Future<String?> getToken();

  Future<void> saveToken(String token);

  Future<Guardian?> getCurrentGuardian();

  Future<Guardian> getGuardianProfile(int guardianId);

  Future<bool> isLoggedIn();
}
import 'package:guardianes_mobile/features/auth/domain/entities/auth_result.dart';
import 'package:guardianes_mobile/features/auth/domain/repositories/auth_repository.dart';

class LoginGuardian {
  final AuthRepository repository;

  LoginGuardian(this.repository);

  Future<AuthResult> call({
    required String usernameOrEmail,
    required String password,
  }) async {
    return await repository.login(
      usernameOrEmail: usernameOrEmail,
      password: password,
    );
  }
}

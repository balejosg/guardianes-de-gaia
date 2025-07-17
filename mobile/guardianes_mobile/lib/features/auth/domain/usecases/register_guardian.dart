import 'package:guardianes_mobile/features/auth/domain/entities/auth_result.dart';
import 'package:guardianes_mobile/features/auth/domain/repositories/auth_repository.dart';

class RegisterGuardian {
  final AuthRepository repository;

  RegisterGuardian(this.repository);

  Future<AuthResult> call({
    required String username,
    required String email,
    required String password,
    required String name,
    required DateTime birthDate,
  }) async {
    return await repository.register(
      username: username,
      email: email,
      password: password,
      name: name,
      birthDate: birthDate,
    );
  }
}
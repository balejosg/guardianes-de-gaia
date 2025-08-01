import 'package:equatable/equatable.dart';
import 'package:guardianes_mobile/features/auth/domain/entities/guardian.dart';

class AuthResult extends Equatable {
  final String token;
  final Guardian guardian;

  const AuthResult({
    required this.token,
    required this.guardian,
  });

  @override
  List<Object?> get props => [token, guardian];
}

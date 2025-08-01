part of 'auth_bloc.dart';

abstract class AuthEvent extends Equatable {
  const AuthEvent();

  @override
  List<Object> get props => [];
}

class AuthCheckStatus extends AuthEvent {}

class AuthLoginRequested extends AuthEvent {
  final String usernameOrEmail;
  final String password;

  const AuthLoginRequested({
    required this.usernameOrEmail,
    required this.password,
  });

  @override
  List<Object> get props => [usernameOrEmail, password];
}

class AuthRegisterRequested extends AuthEvent {
  final String username;
  final String email;
  final String password;
  final String name;
  final DateTime birthDate;

  const AuthRegisterRequested({
    required this.username,
    required this.email,
    required this.password,
    required this.name,
    required this.birthDate,
  });

  @override
  List<Object> get props => [username, email, password, name, birthDate];
}

class AuthLogoutRequested extends AuthEvent {}

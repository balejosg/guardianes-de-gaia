import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:equatable/equatable.dart';
import 'package:guardianes_mobile/features/auth/domain/entities/guardian.dart';
import 'package:guardianes_mobile/features/auth/domain/usecases/login_guardian.dart';
import 'package:guardianes_mobile/features/auth/domain/usecases/register_guardian.dart';
import 'package:guardianes_mobile/features/auth/domain/repositories/auth_repository.dart';

part 'auth_event.dart';
part 'auth_state.dart';

class AuthBloc extends Bloc<AuthEvent, AuthState> {
  final LoginGuardian loginGuardian;
  final RegisterGuardian registerGuardian;
  final AuthRepository authRepository;

  AuthBloc({
    required this.loginGuardian,
    required this.registerGuardian,
    required this.authRepository,
  }) : super(AuthInitial()) {
    on<AuthCheckStatus>(_onCheckStatus);
    on<AuthLoginRequested>(_onLoginRequested);
    on<AuthRegisterRequested>(_onRegisterRequested);
    on<AuthLogoutRequested>(_onLogoutRequested);
  }

  Future<void> _onCheckStatus(
      AuthCheckStatus event, Emitter<AuthState> emit) async {
    emit(AuthLoading());
    try {
      final isLoggedIn = await authRepository.isLoggedIn();
      if (isLoggedIn) {
        final guardian = await authRepository.getCurrentGuardian();
        if (guardian != null) {
          emit(AuthAuthenticated(guardian: guardian));
        } else {
          emit(AuthUnauthenticated());
        }
      } else {
        emit(AuthUnauthenticated());
      }
    } catch (e) {
      emit(AuthUnauthenticated());
    }
  }

  Future<void> _onLoginRequested(
      AuthLoginRequested event, Emitter<AuthState> emit) async {
    emit(AuthLoading());
    try {
      final result = await loginGuardian(
        usernameOrEmail: event.usernameOrEmail,
        password: event.password,
      );
      emit(AuthAuthenticated(guardian: result.guardian));
    } catch (e) {
      emit(AuthError(message: e.toString()));
    }
  }

  Future<void> _onRegisterRequested(
      AuthRegisterRequested event, Emitter<AuthState> emit) async {
    emit(AuthLoading());
    try {
      final result = await registerGuardian(
        username: event.username,
        email: event.email,
        password: event.password,
        name: event.name,
        birthDate: event.birthDate,
      );
      emit(AuthAuthenticated(guardian: result.guardian));
    } catch (e) {
      emit(AuthError(message: e.toString()));
    }
  }

  Future<void> _onLogoutRequested(
      AuthLogoutRequested event, Emitter<AuthState> emit) async {
    try {
      await authRepository.logout();
    } catch (e) {
      // Even if logout fails, we should still emit unauthenticated state
      // to ensure the UI reflects that the user is logged out
    }
    emit(AuthUnauthenticated());
  }
}

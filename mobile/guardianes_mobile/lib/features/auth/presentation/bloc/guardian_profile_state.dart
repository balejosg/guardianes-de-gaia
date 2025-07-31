part of 'guardian_profile_bloc.dart';

abstract class GuardianProfileState extends Equatable {
  const GuardianProfileState();

  @override
  List<Object> get props => [];
}

class GuardianProfileInitial extends GuardianProfileState {}

class GuardianProfileLoading extends GuardianProfileState {}

class GuardianProfileLoaded extends GuardianProfileState {
  final Guardian guardian;

  const GuardianProfileLoaded({required this.guardian});

  @override
  List<Object> get props => [guardian];
}

class GuardianProfileError extends GuardianProfileState {
  final String message;

  const GuardianProfileError({required this.message});

  @override
  List<Object> get props => [message];
}
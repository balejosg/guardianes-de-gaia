part of 'guardian_profile_bloc.dart';

abstract class GuardianProfileEvent extends Equatable {
  const GuardianProfileEvent();

  @override
  List<Object> get props => [];
}

class LoadGuardianProfileEvent extends GuardianProfileEvent {
  final int guardianId;

  const LoadGuardianProfileEvent({required this.guardianId});

  @override
  List<Object> get props => [guardianId];
}

class RefreshGuardianProfileEvent extends GuardianProfileEvent {
  final int guardianId;

  const RefreshGuardianProfileEvent({required this.guardianId});

  @override
  List<Object> get props => [guardianId];
}
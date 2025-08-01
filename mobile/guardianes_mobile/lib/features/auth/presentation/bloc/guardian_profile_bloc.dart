import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:equatable/equatable.dart';
import '../../domain/entities/guardian.dart';
import '../../domain/usecases/get_guardian_profile.dart';

part 'guardian_profile_event.dart';
part 'guardian_profile_state.dart';

class GuardianProfileBloc
    extends Bloc<GuardianProfileEvent, GuardianProfileState> {
  final GetGuardianProfile getGuardianProfile;

  GuardianProfileBloc({
    required this.getGuardianProfile,
  }) : super(GuardianProfileInitial()) {
    on<LoadGuardianProfileEvent>(_onLoadGuardianProfile);
    on<RefreshGuardianProfileEvent>(_onRefreshGuardianProfile);
  }

  Future<void> _onLoadGuardianProfile(
    LoadGuardianProfileEvent event,
    Emitter<GuardianProfileState> emit,
  ) async {
    emit(GuardianProfileLoading());
    try {
      final guardian = await getGuardianProfile(event.guardianId);
      emit(GuardianProfileLoaded(guardian: guardian));
    } catch (e) {
      emit(GuardianProfileError(message: e.toString()));
    }
  }

  Future<void> _onRefreshGuardianProfile(
    RefreshGuardianProfileEvent event,
    Emitter<GuardianProfileState> emit,
  ) async {
    try {
      final guardian = await getGuardianProfile(event.guardianId);
      emit(GuardianProfileLoaded(guardian: guardian));
    } catch (e) {
      emit(GuardianProfileError(message: e.toString()));
    }
  }
}

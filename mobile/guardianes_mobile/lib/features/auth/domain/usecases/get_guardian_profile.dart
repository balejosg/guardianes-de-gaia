import '../entities/guardian.dart';
import '../repositories/auth_repository.dart';

class GetGuardianProfile {
  final AuthRepository repository;

  GetGuardianProfile(this.repository);

  Future<Guardian> call(int guardianId) async {
    if (guardianId <= 0) {
      throw Exception('Guardian ID must be positive');
    }

    return await repository.getGuardianProfile(guardianId);
  }
}

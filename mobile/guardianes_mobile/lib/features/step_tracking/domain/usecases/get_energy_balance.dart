import '../entities/energy_balance.dart';
import '../repositories/step_tracking_repository.dart';

class GetEnergyBalanceUseCase {
  final StepTrackingRepository _repository;

  GetEnergyBalanceUseCase(this._repository);

  Future<EnergyBalance> call(String guardianId) async {
    return await _repository.getEnergyBalance(guardianId);
  }
}
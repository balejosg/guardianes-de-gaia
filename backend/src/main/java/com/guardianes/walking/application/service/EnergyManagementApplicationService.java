package com.guardianes.walking.application.service;

import com.guardianes.walking.application.dto.EnergyBalanceResponse;
import com.guardianes.walking.application.dto.EnergySpendingRequest;
import com.guardianes.walking.application.dto.EnergySpendingResponse;

public interface EnergyManagementApplicationService {

    EnergyBalanceResponse getEnergyBalance(Long guardianId);

    EnergySpendingResponse spendEnergy(Long guardianId, EnergySpendingRequest request);
}

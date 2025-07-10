package com.guardianes.walking.application.service.impl;

import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.shared.infrastructure.metrics.BusinessMetricsService;
import com.guardianes.walking.application.dto.EnergyBalanceResponse;
import com.guardianes.walking.application.dto.EnergySpendingRequest;
import com.guardianes.walking.application.dto.EnergySpendingResponse;
import com.guardianes.walking.application.service.EnergyManagementApplicationService;
import com.guardianes.walking.domain.model.Energy;
import com.guardianes.walking.domain.model.EnergySource;
import com.guardianes.walking.domain.model.EnergyTransaction;
import com.guardianes.walking.domain.service.EnergyManagementDomainService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnergyManagementApplicationServiceImpl implements EnergyManagementApplicationService {
    private static final Logger logger =
            LoggerFactory.getLogger(EnergyManagementApplicationServiceImpl.class);

    private final EnergyManagementDomainService energyManagementDomainService;
    private final BusinessMetricsService metricsService;

    @Autowired
    public EnergyManagementApplicationServiceImpl(
            EnergyManagementDomainService energyManagementDomainService,
            BusinessMetricsService metricsService) {
        this.energyManagementDomainService = energyManagementDomainService;
        this.metricsService = metricsService;
    }

    @Override
    @Transactional(readOnly = true)
    public EnergyBalanceResponse getEnergyBalance(Long guardianId) {
        GuardianId domainGuardianId = GuardianId.of(guardianId);
        logger.debug("Getting energy balance for guardian {}", guardianId);

        // Delegate to domain service for balance calculation
        Energy currentBalance =
                energyManagementDomainService.calculateCurrentBalance(domainGuardianId);

        // Get recent transactions for summary
        List<EnergyTransaction> recentTransactions =
                energyManagementDomainService.getRecentTransactions(domainGuardianId, 10);

        logger.debug(
                "Energy balance for guardian {}: {} energy, {} recent transactions",
                guardianId,
                currentBalance.amount(),
                recentTransactions.size());

        return new EnergyBalanceResponse(domainGuardianId, currentBalance, recentTransactions);
    }

    @Override
    @Transactional
    public EnergySpendingResponse spendEnergy(Long guardianId, EnergySpendingRequest request) {
        logger.info(
                "Processing energy spending for guardian {}: {} energy for {}",
                guardianId,
                request.amount(),
                request.source());

        try {
            // Convert to domain value objects
            GuardianId domainGuardianId = GuardianId.of(guardianId);
            Energy amount = request.getEnergyAmount();
            EnergySource source = EnergySource.of(request.source().name());

            // Get current balance for metrics
            Energy currentBalance =
                    energyManagementDomainService.calculateCurrentBalance(domainGuardianId);
            logger.debug(
                    "Current energy balance for guardian {}: {}",
                    guardianId,
                    currentBalance.amount());

            // Delegate to domain service for spending logic
            EnergyTransaction transaction =
                    energyManagementDomainService.spendEnergy(domainGuardianId, amount, source);

            // Record metrics for successful energy spending
            metricsService.recordEnergySpent(guardianId, request.amount(), request.source().name());

            // Calculate new balance
            Energy newBalance = currentBalance.subtract(amount);

            EnergySpendingResponse response =
                    new EnergySpendingResponse(
                            domainGuardianId,
                            newBalance,
                            amount,
                            request.source(),
                            "Energy spent successfully");

            logger.info(
                    "Successfully processed energy spending for guardian {}: {} energy spent, {} remaining",
                    guardianId,
                    amount.amount(),
                    newBalance.amount());

            return response;
        } catch (com.guardianes.walking.domain.model.InsufficientEnergyException e) {
            // Record metrics for insufficient energy
            Energy currentBalance =
                    energyManagementDomainService.calculateCurrentBalance(
                            GuardianId.of(guardianId));
            metricsService.recordInsufficientEnergy(
                    guardianId, request.amount(), currentBalance.amount());

            logger.warn(
                    "Insufficient energy for guardian {}: requested {}, available {}",
                    guardianId,
                    request.amount(),
                    currentBalance.amount());
            throw e;
        } catch (Exception e) {
            logger.error(
                    "Error processing energy spending for guardian {}: {}",
                    guardianId,
                    e.getMessage(),
                    e);
            throw e;
        }
    }
}

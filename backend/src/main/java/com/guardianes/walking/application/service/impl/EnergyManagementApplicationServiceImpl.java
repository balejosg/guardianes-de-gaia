package com.guardianes.walking.application.service.impl;

import com.guardianes.shared.infrastructure.metrics.BusinessMetricsService;
import com.guardianes.walking.application.dto.EnergyBalanceResponse;
import com.guardianes.walking.application.dto.EnergySpendingRequest;
import com.guardianes.walking.application.dto.EnergySpendingResponse;
import com.guardianes.walking.application.service.EnergyManagementApplicationService;
import com.guardianes.walking.domain.EnergyRepository;
import com.guardianes.walking.domain.EnergyTransaction;
import com.guardianes.walking.domain.EnergyTransactionType;
import com.guardianes.walking.domain.InsufficientEnergyException;
import java.time.LocalDateTime;
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

  private final EnergyRepository energyRepository;
  private final BusinessMetricsService metricsService;

  @Autowired
  public EnergyManagementApplicationServiceImpl(
      EnergyRepository energyRepository, BusinessMetricsService metricsService) {
    this.energyRepository = energyRepository;
    this.metricsService = metricsService;
  }

  @Override
  @Transactional(readOnly = true)
  public EnergyBalanceResponse getEnergyBalance(Long guardianId) {
    logger.debug("Getting energy balance for guardian {}", guardianId);

    // Get current energy balance
    int currentBalance = energyRepository.getEnergyBalance(guardianId);

    // Get recent transactions for summary
    List<EnergyTransaction> recentTransactions =
        energyRepository.getRecentTransactions(guardianId, 10);

    logger.debug(
        "Energy balance for guardian {}: {} energy, {} recent transactions",
        guardianId,
        currentBalance,
        recentTransactions.size());

    return new EnergyBalanceResponse(guardianId, currentBalance, recentTransactions);
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
      // Get current balance
      int currentBalance = energyRepository.getEnergyBalance(guardianId);
      logger.debug("Current energy balance for guardian {}: {}", guardianId, currentBalance);

      // Check if sufficient energy
      if (currentBalance < request.amount()) {
        logger.warn(
            "Insufficient energy for guardian {}: requested {}, available {}",
            guardianId,
            request.amount(),
            currentBalance);
        metricsService.recordInsufficientEnergy(guardianId, request.amount(), currentBalance);
        throw new InsufficientEnergyException("Not enough energy available");
      }

      // Create spending transaction
      EnergyTransaction transaction =
          new EnergyTransaction(
              guardianId,
              EnergyTransactionType.SPENT,
              request.amount(),
              request.source().name(),
              LocalDateTime.now());

      // Save transaction
      energyRepository.saveTransaction(transaction);
      logger.debug("Saved energy transaction for guardian {}: {}", guardianId, transaction);

      // Record metrics for successful energy spending
      metricsService.recordEnergySpent(guardianId, request.amount(), request.source().name());

      // Calculate new balance
      int newBalance = currentBalance - request.amount();

      EnergySpendingResponse response =
          new EnergySpendingResponse(
              guardianId,
              newBalance,
              request.amount(),
              request.source(),
              "Energy spent successfully");

      logger.info(
          "Successfully processed energy spending for guardian {}: {} energy spent, {} remaining",
          guardianId,
          request.amount(),
          newBalance);

      return response;
    } catch (Exception e) {
      logger.error(
          "Error processing energy spending for guardian {}: {}", guardianId, e.getMessage(), e);
      throw e;
    }
  }
}

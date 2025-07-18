package com.guardianes.walking.application.service.impl;

import com.guardianes.walking.application.dto.CurrentStepCountResponse;
import com.guardianes.walking.application.dto.StepHistoryResponse;
import com.guardianes.walking.application.dto.StepSubmissionRequest;
import com.guardianes.walking.application.dto.StepSubmissionResponse;
import com.guardianes.walking.application.service.StepTrackingApplicationService;
import com.guardianes.walking.domain.DailyStepAggregate;
import com.guardianes.walking.domain.EnergyCalculationService;
import com.guardianes.walking.domain.EnergyRepository;
import com.guardianes.walking.domain.EnergyTransaction;
import com.guardianes.walking.domain.EnergyTransactionType;
import com.guardianes.walking.domain.RateLimitExceededException;
import com.guardianes.walking.domain.StepAggregationService;
import com.guardianes.walking.domain.StepRecord;
import com.guardianes.walking.domain.StepRepository;
import com.guardianes.walking.domain.StepValidationResult;
import com.guardianes.walking.domain.StepValidationService;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StepTrackingApplicationServiceImpl implements StepTrackingApplicationService {
  private static final Logger logger =
      LoggerFactory.getLogger(StepTrackingApplicationServiceImpl.class);

  private final StepAggregationService stepAggregationService;
  private final StepValidationService stepValidationService;
  private final EnergyCalculationService energyCalculationService;
  private final StepRepository stepRepository;
  private final EnergyRepository energyRepository;

  @Autowired
  public StepTrackingApplicationServiceImpl(
      StepAggregationService stepAggregationService,
      StepValidationService stepValidationService,
      EnergyCalculationService energyCalculationService,
      StepRepository stepRepository,
      EnergyRepository energyRepository) {
    this.stepAggregationService = stepAggregationService;
    this.stepValidationService = stepValidationService;
    this.energyCalculationService = energyCalculationService;
    this.stepRepository = stepRepository;
    this.energyRepository = energyRepository;
  }

  @Override
  @Transactional
  public StepSubmissionResponse submitSteps(Long guardianId, StepSubmissionRequest request) {
    logger.info(
        "Processing step submission for guardian {}: {} steps at {}",
        guardianId,
        request.stepCount(),
        request.timestamp());

    try {
      // Validate step submission
      StepValidationResult validationResult =
          stepValidationService.validateStepCount(
              guardianId, request.stepCount(), request.timestamp());

      if (!validationResult.isValid()) {
        logger.warn(
            "Step validation failed for guardian {}: {}",
            guardianId,
            validationResult.getErrorMessage());
        throw new IllegalArgumentException(validationResult.getErrorMessage());
      }

      // Check rate limiting
      if (!stepValidationService.isWithinSubmissionRateLimit(guardianId, request.timestamp())) {
        logger.warn("Rate limit exceeded for guardian {}", guardianId);
        throw new RateLimitExceededException("Too many step submissions");
      }

      // Create and save step record
      StepRecord stepRecord = new StepRecord(guardianId, request.stepCount(), request.timestamp());
      StepRecord savedStepRecord = stepRepository.save(stepRecord);
      logger.debug("Saved step record for guardian {}: {}", guardianId, savedStepRecord);

      // Aggregate steps for the day
      DailyStepAggregate dailyAggregate =
          stepAggregationService.aggregateDailySteps(guardianId, request.timestamp().toLocalDate());

      // Calculate energy earned
      int energyEarned = energyCalculationService.calculateEnergyFromSteps(request.stepCount());
      logger.debug(
          "Calculated energy for guardian {}: {} energy from {} steps",
          guardianId,
          energyEarned,
          request.stepCount());

      // Create energy transaction
      EnergyTransaction energyTransaction =
          new EnergyTransaction(
              guardianId, EnergyTransactionType.EARNED, energyEarned, "Steps", request.timestamp());
      EnergyTransaction savedEnergyTransaction =
          energyRepository.saveTransaction(energyTransaction);
      logger.debug(
          "Created energy transaction for guardian {}: {}", guardianId, savedEnergyTransaction);

      StepSubmissionResponse response =
          new StepSubmissionResponse(
              guardianId,
              dailyAggregate.getTotalSteps(),
              energyEarned,
              "Steps submitted successfully");

      logger.info(
          "Successfully processed step submission for guardian {}: {} total daily steps, {} energy earned",
          guardianId,
          dailyAggregate.getTotalSteps(),
          energyEarned);

      return response;
    } catch (Exception e) {
      logger.error(
          "Error processing step submission for guardian {}: {}", guardianId, e.getMessage(), e);
      throw e;
    }
  }

  @Override
  @Transactional(readOnly = true)
  public CurrentStepCountResponse getCurrentStepCount(Long guardianId) {
    logger.debug("Getting current step count for guardian {}", guardianId);

    LocalDate today = LocalDate.now();
    DailyStepAggregate todayAggregate =
        stepAggregationService.aggregateDailySteps(guardianId, today);
    int availableEnergy =
        energyCalculationService.calculateEnergyFromSteps(todayAggregate.getTotalSteps());

    logger.debug(
        "Current step count for guardian {}: {} steps, {} energy available",
        guardianId,
        todayAggregate.getTotalSteps(),
        availableEnergy);

    return new CurrentStepCountResponse(
        guardianId, todayAggregate.getTotalSteps(), availableEnergy, today);
  }

  @Override
  @Transactional(readOnly = true)
  public StepHistoryResponse getStepHistory(Long guardianId, LocalDate fromDate, LocalDate toDate) {
    logger.debug(
        "Getting step history for guardian {} from {} to {}", guardianId, fromDate, toDate);

    List<DailyStepAggregate> dailyAggregates =
        stepRepository.findDailyAggregatesByGuardianIdAndDateRange(guardianId, fromDate, toDate);

    logger.debug(
        "Retrieved step history for guardian {}: {} days of data",
        guardianId,
        dailyAggregates.size());

    return new StepHistoryResponse(guardianId, dailyAggregates);
  }
}

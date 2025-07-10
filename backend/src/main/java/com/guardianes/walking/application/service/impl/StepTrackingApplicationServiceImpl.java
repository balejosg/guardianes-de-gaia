package com.guardianes.walking.application.service.impl;

import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.walking.application.dto.CurrentStepCountResponse;
import com.guardianes.walking.application.dto.StepHistoryResponse;
import com.guardianes.walking.application.dto.StepSubmissionRequest;
import com.guardianes.walking.application.dto.StepSubmissionResponse;
import com.guardianes.walking.application.service.CachedStepAggregationService;
import com.guardianes.walking.application.service.StepTrackingApplicationService;
import com.guardianes.walking.domain.exception.RateLimitExceededException;
import com.guardianes.walking.domain.model.DailyStepAggregate;
import com.guardianes.walking.domain.model.Energy;
import com.guardianes.walking.domain.model.StepCount;
import com.guardianes.walking.domain.model.StepSubmissionResult;
import com.guardianes.walking.domain.model.StepValidationResult;
import com.guardianes.walking.domain.service.StepSubmissionDomainService;
import com.guardianes.walking.domain.service.StepValidationService;
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

    private final StepSubmissionDomainService stepSubmissionDomainService;
    private final StepValidationService stepValidationService;
    private final CachedStepAggregationService cachedStepAggregationService;

    @Autowired
    public StepTrackingApplicationServiceImpl(
            StepSubmissionDomainService stepSubmissionDomainService,
            StepValidationService stepValidationService,
            CachedStepAggregationService cachedStepAggregationService) {
        this.stepSubmissionDomainService = stepSubmissionDomainService;
        this.stepValidationService = stepValidationService;
        this.cachedStepAggregationService = cachedStepAggregationService;
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
            // Convert to domain value objects
            GuardianId domainGuardianId = GuardianId.of(guardianId);
            StepCount stepCount = StepCount.of(request.stepCount());

            // Validate step submission using existing validation service
            StepValidationResult validationResult =
                    stepValidationService.validateStepCount(
                            domainGuardianId, stepCount, request.timestamp());

            if (!validationResult.isValid()) {
                logger.warn(
                        "Step validation failed for guardian {}: {}",
                        guardianId,
                        validationResult.getErrorMessage());
                throw new IllegalArgumentException(validationResult.getErrorMessage());
            }

            // Check rate limiting
            if (!stepValidationService.isWithinSubmissionRateLimit(
                    domainGuardianId, request.timestamp())) {
                logger.warn("Rate limit exceeded for guardian {}", guardianId);
                throw new RateLimitExceededException("Too many step submissions");
            }

            // Delegate to domain service for core business logic
            StepSubmissionResult result =
                    stepSubmissionDomainService.processStepSubmission(domainGuardianId, stepCount);

            // Invalidate cache after successful submission
            cachedStepAggregationService.invalidateDailyAggregate(
                    domainGuardianId, LocalDate.now());

            // Convert domain result to application response
            StepSubmissionResponse response =
                    new StepSubmissionResponse(
                            guardianId,
                            result.getUpdatedDailyAggregate().getTotalSteps().value(),
                            result.getEnergyGenerated().amount(),
                            "Steps submitted successfully");

            logger.info(
                    "Successfully processed step submission for guardian {}: {} total daily steps, {} energy earned",
                    guardianId,
                    result.getUpdatedDailyAggregate().getTotalSteps().value(),
                    result.getEnergyGenerated().amount());

            return response;
        } catch (Exception e) {
            logger.error(
                    "Error processing step submission for guardian {}: {}",
                    guardianId,
                    e.getMessage(),
                    e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CurrentStepCountResponse getCurrentStepCount(Long guardianId) {
        logger.debug("Getting current step count for guardian {}", guardianId);

        // Use cached aggregation service for better performance
        LocalDate today = LocalDate.now();
        DailyStepAggregate todayAggregate =
                cachedStepAggregationService.getDailyAggregate(GuardianId.of(guardianId), today);

        // Calculate energy using domain logic
        StepCount stepCount = todayAggregate.getTotalSteps();
        Energy availableEnergy = todayAggregate.calculateTotalEnergyGenerated();

        logger.debug(
                "Current step count for guardian {}: {} steps, {} energy available",
                guardianId,
                stepCount.value(),
                availableEnergy.amount());

        return new CurrentStepCountResponse(
                guardianId, stepCount.value(), availableEnergy.amount(), today);
    }

    @Override
    @Transactional(readOnly = true)
    public StepHistoryResponse getStepHistory(
            Long guardianId, LocalDate fromDate, LocalDate toDate) {
        logger.debug(
                "Getting step history for guardian {} from {} to {}", guardianId, fromDate, toDate);

        // Use cached service for step history
        List<DailyStepAggregate> dailyAggregates =
                cachedStepAggregationService.getStepHistory(
                        GuardianId.of(guardianId), fromDate, toDate);

        logger.debug(
                "Retrieved step history for guardian {}: {} days of data",
                guardianId,
                dailyAggregates.size());

        return new StepHistoryResponse(guardianId, dailyAggregates);
    }
}

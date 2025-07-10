package com.guardianes.walking.domain.service;

import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.walking.domain.model.DailyStepAggregate;
import com.guardianes.walking.domain.model.Energy;
import com.guardianes.walking.domain.model.EnergySource;
import com.guardianes.walking.domain.model.EnergyTransaction;
import com.guardianes.walking.domain.model.StepCount;
import com.guardianes.walking.domain.model.StepRecord;
import com.guardianes.walking.domain.model.StepSubmissionResult;
import com.guardianes.walking.domain.repository.EnergyRepository;
import com.guardianes.walking.domain.repository.StepRepository;
import java.time.LocalDate;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Domain service that encapsulates the complex business logic for step submission. Coordinates
 * between multiple domain objects and repositories to ensure business rules are followed.
 */
@Service
public class StepSubmissionDomainService {
    private static final Logger logger = LoggerFactory.getLogger(StepSubmissionDomainService.class);

    private final StepRepository stepRepository;
    private final EnergyRepository energyRepository;

    public StepSubmissionDomainService(
            StepRepository stepRepository, EnergyRepository energyRepository) {
        this.stepRepository = stepRepository;
        this.energyRepository = energyRepository;
    }

    /**
     * Processes a step submission with full business rule validation. This is the core business
     * operation that coordinates all domain logic.
     */
    public StepSubmissionResult processStepSubmission(GuardianId guardianId, StepCount stepCount) {
        Objects.requireNonNull(guardianId, "Guardian ID cannot be null");
        Objects.requireNonNull(stepCount, "Step count cannot be null");

        logger.debug("Processing step submission for guardian {}: {} steps", guardianId, stepCount);

        // 1. Get current daily aggregate to validate against daily maximum
        LocalDate today = LocalDate.now();
        DailyStepAggregate currentAggregate = getCurrentDailyAggregate(guardianId, today);

        // 2. Validate that adding these steps won't exceed daily maximum
        if (stepCount.wouldExceedDailyMaximum(currentAggregate.getTotalSteps())) {
            throw new IllegalArgumentException(
                    String.format(
                            "Adding %d steps would exceed daily maximum. Current total: %d",
                            stepCount.value(), currentAggregate.getTotalSteps().value()));
        }

        // 3. Create the step record (this validates the step count itself)
        StepRecord stepRecord = StepRecord.create(guardianId, stepCount);

        // 4. Calculate energy generated
        Energy energyGenerated = stepRecord.calculateEnergyGenerated();

        // 5. Create energy transaction
        EnergyTransaction energyTransaction =
                EnergyTransaction.earned(guardianId, energyGenerated, EnergySource.steps());

        // 6. Update daily aggregate
        DailyStepAggregate updatedAggregate = currentAggregate.addSteps(stepCount);

        // 7. Persist all changes
        stepRepository.save(stepRecord);
        energyRepository.saveTransaction(energyTransaction);
        stepRepository.saveDailyAggregate(updatedAggregate);

        logger.info(
                "Successfully processed step submission for guardian {}: {} steps, {} energy generated",
                guardianId,
                stepCount,
                energyGenerated);

        return StepSubmissionResult.success(stepRecord, energyTransaction, updatedAggregate);
    }

    /** Validates if a step submission would be reasonable based on previous submissions. */
    public boolean isReasonableStepSubmission(GuardianId guardianId, StepCount stepCount) {
        // Get the most recent step record for this guardian
        // This would require a new repository method to get the latest record
        // For now, we'll implement basic validation

        if (stepCount.value() > 10000) {
            logger.warn(
                    "Large step submission detected for guardian {}: {} steps",
                    guardianId,
                    stepCount);
            return false;
        }

        return true;
    }

    /** Gets the current daily aggregate, creating an empty one if none exists. */
    private DailyStepAggregate getCurrentDailyAggregate(GuardianId guardianId, LocalDate date) {
        try {
            // Try to get existing aggregate
            return stepRepository
                    .findDailyAggregatesByGuardianIdAndDateRange(guardianId, date, date)
                    .stream()
                    .findFirst()
                    .orElse(DailyStepAggregate.empty(guardianId, date));
        } catch (Exception e) {
            logger.debug(
                    "No existing daily aggregate found for guardian {} on {}, creating empty",
                    guardianId,
                    date);
            return DailyStepAggregate.empty(guardianId, date);
        }
    }
}

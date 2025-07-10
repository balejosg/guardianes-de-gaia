package com.guardianes.walking.application.service;

import com.guardianes.walking.application.dto.CurrentStepCountResponse;
import com.guardianes.walking.application.dto.StepHistoryResponse;
import com.guardianes.walking.application.dto.StepSubmissionRequest;
import com.guardianes.walking.application.dto.StepSubmissionResponse;
import java.time.LocalDate;

public interface StepTrackingApplicationService {

    StepSubmissionResponse submitSteps(Long guardianId, StepSubmissionRequest request);

    CurrentStepCountResponse getCurrentStepCount(Long guardianId);

    StepHistoryResponse getStepHistory(Long guardianId, LocalDate fromDate, LocalDate toDate);
}

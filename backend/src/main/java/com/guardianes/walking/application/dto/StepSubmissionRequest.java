package com.guardianes.walking.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "Request to submit step count for a guardian")
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore unknown fields like maliciousField
public record StepSubmissionRequest(
        @Schema(description = "Number of steps taken", example = "2500", minimum = "1")
                @NotNull(message = "Step count is required")
                @Min(value = 1, message = "Step count must be positive")
                Integer stepCount,
        @Schema(description = "Timestamp when steps were taken", example = "2025-07-06T14:30:00")
                @NotNull(message = "Timestamp is required")
                LocalDateTime timestamp) {}

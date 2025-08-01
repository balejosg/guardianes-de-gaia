class StepValidationResult {
  final bool isValid;
  final String? errorMessage;
  final List<String> warnings;

  const StepValidationResult({
    required this.isValid,
    this.errorMessage,
    this.warnings = const [],
  });

  factory StepValidationResult.valid({List<String> warnings = const []}) {
    return StepValidationResult(
      isValid: true,
      warnings: warnings,
    );
  }

  factory StepValidationResult.invalid(String errorMessage,
      {List<String> warnings = const []}) {
    return StepValidationResult(
      isValid: false,
      errorMessage: errorMessage,
      warnings: warnings,
    );
  }
}

class StepValidator {
  static const int MIN_STEP_COUNT = 1;
  static const int MAX_STEP_COUNT = 50000;
  static const int MAX_DAILY_STEPS = 100000;
  static const int ANOMALY_THRESHOLD =
      15000; // Steps that seem unusually high for single submission

  /// Validates a single step submission
  static StepValidationResult validateStepSubmission({
    required int stepCount,
    required String timestamp,
    required int guardianId,
    int currentDailyTotal = 0,
  }) {
    final warnings = <String>[];

    // Validate guardian ID
    if (guardianId <= 0) {
      return StepValidationResult.invalid('Invalid guardian ID');
    }

    // Validate step count range
    if (stepCount < MIN_STEP_COUNT) {
      return StepValidationResult.invalid(
          'Step count must be at least $MIN_STEP_COUNT');
    }

    if (stepCount > MAX_STEP_COUNT) {
      return StepValidationResult.invalid(
          'Step count cannot exceed $MAX_STEP_COUNT');
    }

    // Validate timestamp format
    if (timestamp.isEmpty) {
      return StepValidationResult.invalid('Timestamp is required');
    }

    try {
      final parsedTime = DateTime.parse(timestamp);
      final now = DateTime.now();

      // Check if timestamp is in the future
      if (parsedTime.isAfter(now)) {
        return StepValidationResult.invalid(
            'Timestamp cannot be in the future');
      }

      // Check if timestamp is too old (more than 24 hours)
      final dayAgo = now.subtract(const Duration(hours: 24));
      if (parsedTime.isBefore(dayAgo)) {
        warnings.add('Step count is more than 24 hours old');
      }
    } catch (e) {
      return StepValidationResult.invalid('Invalid timestamp format');
    }

    // Check daily totals
    final newDailyTotal = currentDailyTotal + stepCount;
    if (newDailyTotal > MAX_DAILY_STEPS) {
      return StepValidationResult.invalid(
          'Daily step count would exceed $MAX_DAILY_STEPS');
    }

    // Anomaly detection
    if (stepCount > ANOMALY_THRESHOLD) {
      warnings.add('Unusually high step count detected');
    }

    return StepValidationResult.valid(warnings: warnings);
  }

  /// Validates step rate (steps per minute)
  static StepValidationResult validateStepRate({
    required int stepCount,
    required Duration timePeriod,
  }) {
    final warnings = <String>[];

    if (timePeriod.inMinutes == 0) {
      return StepValidationResult.valid();
    }

    final stepsPerMinute = stepCount / timePeriod.inMinutes;

    // Normal walking pace is about 100-120 steps per minute
    // Running can be 160-180 steps per minute
    // Anything above 200 is suspicious
    if (stepsPerMinute > 200) {
      return StepValidationResult.invalid(
          'Step rate too high (${stepsPerMinute.toInt()} steps/min)');
    }

    if (stepsPerMinute > 180) {
      warnings
          .add('High step rate detected (${stepsPerMinute.toInt()} steps/min)');
    }

    return StepValidationResult.valid(warnings: warnings);
  }

  /// Validates energy calculation
  static StepValidationResult validateEnergyCalculation({
    required int stepCount,
    required int expectedEnergy,
  }) {
    const int stepsPerEnergy = 10;
    final calculatedEnergy = stepCount ~/ stepsPerEnergy;

    if (calculatedEnergy != expectedEnergy) {
      return StepValidationResult.invalid(
        'Energy calculation mismatch. Expected: $calculatedEnergy, Got: $expectedEnergy',
      );
    }

    return StepValidationResult.valid();
  }

  /// Validates submission frequency (rate limiting)
  static StepValidationResult validateSubmissionRate({
    required List<DateTime> recentSubmissions,
    required Duration timeWindow,
    required int maxSubmissions,
  }) {
    final now = DateTime.now();
    final windowStart = now.subtract(timeWindow);

    final recentCount = recentSubmissions
        .where((submission) => submission.isAfter(windowStart))
        .length;

    if (recentCount >= maxSubmissions) {
      return StepValidationResult.invalid(
        'Too many submissions. Maximum $maxSubmissions submissions per ${timeWindow.inMinutes} minutes',
      );
    }

    return StepValidationResult.valid();
  }

  /// Comprehensive validation for step submission
  static StepValidationResult validateCompleteSubmission({
    required int stepCount,
    required String timestamp,
    required int guardianId,
    int currentDailyTotal = 0,
    Duration? timeSinceLastSubmission,
    List<DateTime> recentSubmissions = const [],
  }) {
    // Basic validation
    final basicValidation = validateStepSubmission(
      stepCount: stepCount,
      timestamp: timestamp,
      guardianId: guardianId,
      currentDailyTotal: currentDailyTotal,
    );

    if (!basicValidation.isValid) {
      return basicValidation;
    }

    final allWarnings = List<String>.from(basicValidation.warnings);

    // Rate validation if time period is provided
    if (timeSinceLastSubmission != null) {
      final rateValidation = validateStepRate(
        stepCount: stepCount,
        timePeriod: timeSinceLastSubmission,
      );

      if (!rateValidation.isValid) {
        return rateValidation;
      }

      allWarnings.addAll(rateValidation.warnings);
    }

    // Submission frequency validation
    if (recentSubmissions.isNotEmpty) {
      final frequencyValidation = validateSubmissionRate(
        recentSubmissions: recentSubmissions,
        timeWindow: const Duration(hours: 1),
        maxSubmissions: 100, // Max 100 submissions per hour
      );

      if (!frequencyValidation.isValid) {
        return frequencyValidation;
      }
    }

    return StepValidationResult.valid(warnings: allWarnings);
  }
}

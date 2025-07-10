package com.guardianes.walking.domain.model;

public class StepValidationResult {
    private boolean valid;
    private String errorMessage;

    public StepValidationResult(boolean valid, String errorMessage) {
        this.valid = valid;
        this.errorMessage = errorMessage;
    }

    public static StepValidationResult valid() {
        return new StepValidationResult(true, null);
    }

    public static StepValidationResult invalid(String errorMessage) {
        return new StepValidationResult(false, errorMessage);
    }

    public boolean isValid() {
        return valid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

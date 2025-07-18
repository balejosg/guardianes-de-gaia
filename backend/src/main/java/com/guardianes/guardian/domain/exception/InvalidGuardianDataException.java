package com.guardianes.guardian.domain.exception;

public class InvalidGuardianDataException extends RuntimeException {

  public InvalidGuardianDataException(String message) {
    super(message);
  }

  public InvalidGuardianDataException(String message, Throwable cause) {
    super(message, cause);
  }
}

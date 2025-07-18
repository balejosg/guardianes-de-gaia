package com.guardianes.guardian.domain.exception;

public class GuardianAlreadyExistsException extends RuntimeException {

  public GuardianAlreadyExistsException(String message) {
    super(message);
  }

  public GuardianAlreadyExistsException(String message, Throwable cause) {
    super(message, cause);
  }
}

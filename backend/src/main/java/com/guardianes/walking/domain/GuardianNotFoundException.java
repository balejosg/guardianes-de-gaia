package com.guardianes.walking.domain;

public class GuardianNotFoundException extends RuntimeException {

  public GuardianNotFoundException(String message) {
    super(message);
  }

  public GuardianNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}

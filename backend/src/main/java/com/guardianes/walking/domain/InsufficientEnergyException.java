package com.guardianes.walking.domain;

public class InsufficientEnergyException extends RuntimeException {
  public InsufficientEnergyException(String message) {
    super(message);
  }
}

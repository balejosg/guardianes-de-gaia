package com.guardianes.guardian.domain.service;

import com.guardianes.guardian.domain.exception.GuardianAlreadyExistsException;
import com.guardianes.guardian.domain.exception.InvalidGuardianDataException;
import com.guardianes.guardian.domain.model.Guardian;
import com.guardianes.guardian.domain.repository.GuardianRepository;
import java.time.LocalDate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class GuardianRegistrationService {

  private final GuardianRepository guardianRepository;
  private final PasswordEncoder passwordEncoder;

  public GuardianRegistrationService(
      GuardianRepository guardianRepository, PasswordEncoder passwordEncoder) {
    this.guardianRepository = guardianRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public Guardian registerGuardian(
      String username, String email, String password, String name, LocalDate birthDate) {
    validateRegistrationData(username, email, password, name, birthDate);

    if (guardianRepository.existsByUsername(username)) {
      throw new GuardianAlreadyExistsException(
          "Guardian with username '" + username + "' already exists");
    }

    if (guardianRepository.existsByEmail(email)) {
      throw new GuardianAlreadyExistsException(
          "Guardian with email '" + email + "' already exists");
    }

    String hashedPassword = passwordEncoder.encode(password);
    Guardian guardian = new Guardian(username, email, hashedPassword, name, birthDate);

    return guardianRepository.save(guardian);
  }

  private void validateRegistrationData(
      String username, String email, String password, String name, LocalDate birthDate) {
    if (username == null || username.trim().isEmpty()) {
      throw new InvalidGuardianDataException("Username cannot be empty");
    }

    if (username.length() < 3 || username.length() > 20) {
      throw new InvalidGuardianDataException("Username must be between 3 and 20 characters");
    }

    if (!username.matches("^[a-zA-Z0-9_]+$")) {
      throw new InvalidGuardianDataException(
          "Username can only contain letters, numbers, and underscores");
    }

    if (email == null || email.trim().isEmpty()) {
      throw new InvalidGuardianDataException("Email cannot be empty");
    }

    if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
      throw new InvalidGuardianDataException("Invalid email format");
    }

    if (password == null || password.length() < 6) {
      throw new InvalidGuardianDataException("Password must be at least 6 characters long");
    }

    if (name == null || name.trim().isEmpty()) {
      throw new InvalidGuardianDataException("Name cannot be empty");
    }

    if (name.length() > 50) {
      throw new InvalidGuardianDataException("Name cannot exceed 50 characters");
    }

    if (birthDate == null) {
      throw new InvalidGuardianDataException("Birth date cannot be null");
    }

    if (birthDate.isAfter(LocalDate.now())) {
      throw new InvalidGuardianDataException("Birth date cannot be in the future");
    }

    LocalDate minBirthDate = LocalDate.now().minusYears(100);
    if (birthDate.isBefore(minBirthDate)) {
      throw new InvalidGuardianDataException("Invalid birth date");
    }
  }
}

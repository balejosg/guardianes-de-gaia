package com.guardianes.guardian.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Guardian {
  private Long id;
  private String username;
  private String email;
  private String passwordHash;
  private String name;
  private LocalDate birthDate;
  private GuardianLevel level;
  private int experiencePoints;
  private int totalSteps;
  private int totalEnergyGenerated;
  private LocalDateTime createdAt;
  private LocalDateTime lastActiveAt;
  private boolean active;

  public Guardian(
      String username, String email, String passwordHash, String name, LocalDate birthDate) {
    this.username = username;
    this.email = email;
    this.passwordHash = passwordHash;
    this.name = name;
    this.birthDate = birthDate;
    this.level = GuardianLevel.INITIATE;
    this.experiencePoints = 0;
    this.totalSteps = 0;
    this.totalEnergyGenerated = 0;
    this.createdAt = LocalDateTime.now();
    this.lastActiveAt = LocalDateTime.now();
    this.active = true;
  }

  public Guardian(
      Long id,
      String username,
      String email,
      String passwordHash,
      String name,
      LocalDate birthDate,
      GuardianLevel level,
      int experiencePoints,
      int totalSteps,
      int totalEnergyGenerated,
      LocalDateTime createdAt,
      LocalDateTime lastActiveAt,
      boolean active) {
    this.id = id;
    this.username = username;
    this.email = email;
    this.passwordHash = passwordHash;
    this.name = name;
    this.birthDate = birthDate;
    this.level = level;
    this.experiencePoints = experiencePoints;
    this.totalSteps = totalSteps;
    this.totalEnergyGenerated = totalEnergyGenerated;
    this.createdAt = createdAt;
    this.lastActiveAt = lastActiveAt;
    this.active = active;
  }

  public void addExperience(int points) {
    this.experiencePoints += points;
    updateLevel();
  }

  public void addSteps(int steps) {
    this.totalSteps += steps;
  }

  public void addEnergyGenerated(int energy) {
    this.totalEnergyGenerated += energy;
  }

  public void updateLastActive() {
    this.lastActiveAt = LocalDateTime.now();
  }

  public void deactivate() {
    this.active = false;
  }

  public void activate() {
    this.active = true;
  }

  private void updateLevel() {
    GuardianLevel newLevel = GuardianLevel.fromExperiencePoints(this.experiencePoints);
    if (newLevel != this.level) {
      this.level = newLevel;
    }
  }

  public int getAge() {
    return LocalDate.now().getYear() - birthDate.getYear();
  }

  public boolean isChild() {
    return getAge() <= 12;
  }

  public boolean isAdult() {
    return getAge() > 12;
  }

  public Long getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getEmail() {
    return email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public String getName() {
    return name;
  }

  public LocalDate getBirthDate() {
    return birthDate;
  }

  public GuardianLevel getLevel() {
    return level;
  }

  public int getExperiencePoints() {
    return experiencePoints;
  }

  public int getTotalSteps() {
    return totalSteps;
  }

  public int getTotalEnergyGenerated() {
    return totalEnergyGenerated;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getLastActiveAt() {
    return lastActiveAt;
  }

  public boolean isActive() {
    return active;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Guardian guardian = (Guardian) o;
    return Objects.equals(id, guardian.id)
        && Objects.equals(username, guardian.username)
        && Objects.equals(email, guardian.email);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, username, email);
  }

  @Override
  public String toString() {
    return "Guardian{"
        + "id="
        + id
        + ", username='"
        + username
        + '\''
        + ", email='"
        + email
        + '\''
        + ", name='"
        + name
        + '\''
        + ", level="
        + level
        + ", experiencePoints="
        + experiencePoints
        + ", active="
        + active
        + '}';
  }
}

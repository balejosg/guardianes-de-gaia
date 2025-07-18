package com.guardianes.guardian.domain.model;

public enum GuardianLevel {
  INITIATE(0, "Iniciado"),
  APPRENTICE(100, "Aprendiz"),
  PROTECTOR(250, "Protector"),
  KEEPER(500, "Guardián"),
  GUARDIAN(1000, "Gran Guardián"),
  ELDER(2000, "Anciano"),
  SAGE(3500, "Sabio"),
  MASTER(5500, "Maestro"),
  LEGEND(8500, "Leyenda"),
  CHAMPION(13000, "Campeón");

  private final int requiredExperience;
  private final String displayName;

  GuardianLevel(int requiredExperience, String displayName) {
    this.requiredExperience = requiredExperience;
    this.displayName = displayName;
  }

  public static GuardianLevel fromExperiencePoints(int experiencePoints) {
    GuardianLevel[] levels = values();
    for (int i = levels.length - 1; i >= 0; i--) {
      if (experiencePoints >= levels[i].requiredExperience) {
        return levels[i];
      }
    }
    return INITIATE;
  }

  public int getRequiredExperience() {
    return requiredExperience;
  }

  public String getDisplayName() {
    return displayName;
  }

  public GuardianLevel getNextLevel() {
    GuardianLevel[] levels = values();
    int currentIndex = this.ordinal();
    if (currentIndex < levels.length - 1) {
      return levels[currentIndex + 1];
    }
    return this;
  }

  public int getExperienceToNextLevel(int currentExperience) {
    GuardianLevel nextLevel = getNextLevel();
    if (nextLevel == this) {
      return 0;
    }
    return nextLevel.requiredExperience - currentExperience;
  }
}

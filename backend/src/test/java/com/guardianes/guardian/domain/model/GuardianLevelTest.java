package com.guardianes.guardian.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class GuardianLevelTest {

    @Test
    void shouldHaveCorrectLevelOrder() {
        // Given & When
        GuardianLevel[] levels = GuardianLevel.values();

        // Then
        assertEquals(10, levels.length);
        assertEquals(GuardianLevel.INITIATE, levels[0]);
        assertEquals(GuardianLevel.APPRENTICE, levels[1]);
        assertEquals(GuardianLevel.PROTECTOR, levels[2]);
        assertEquals(GuardianLevel.KEEPER, levels[3]);
        assertEquals(GuardianLevel.GUARDIAN, levels[4]);
        assertEquals(GuardianLevel.ELDER, levels[5]);
        assertEquals(GuardianLevel.SAGE, levels[6]);
        assertEquals(GuardianLevel.MASTER, levels[7]);
        assertEquals(GuardianLevel.LEGEND, levels[8]);
        assertEquals(GuardianLevel.CHAMPION, levels[9]);
    }

    @ParameterizedTest
    @CsvSource({
        "0, INITIATE",
        "50, INITIATE",
        "99, INITIATE",
        "100, APPRENTICE",
        "249, APPRENTICE",
        "250, PROTECTOR",
        "499, PROTECTOR",
        "500, KEEPER",
        "999, KEEPER",
        "1000, GUARDIAN",
        "1999, GUARDIAN",
        "2000, ELDER",
        "3499, ELDER",
        "3500, SAGE",
        "5499, SAGE",
        "5500, MASTER",
        "8499, MASTER",
        "8500, LEGEND",
        "12999, LEGEND",
        "13000, CHAMPION",
        "50000, CHAMPION"
    })
    void shouldReturnCorrectLevelFromExperiencePoints(int experiencePoints, GuardianLevel expectedLevel) {
        // When
        GuardianLevel actualLevel = GuardianLevel.fromExperiencePoints(experiencePoints);

        // Then
        assertEquals(expectedLevel, actualLevel);
    }

    @Test
    void shouldReturnCorrectRequiredExperience() {
        // When & Then
        assertEquals(0, GuardianLevel.INITIATE.getRequiredExperience());
        assertEquals(100, GuardianLevel.APPRENTICE.getRequiredExperience());
        assertEquals(250, GuardianLevel.PROTECTOR.getRequiredExperience());
        assertEquals(500, GuardianLevel.KEEPER.getRequiredExperience());
        assertEquals(1000, GuardianLevel.GUARDIAN.getRequiredExperience());
        assertEquals(2000, GuardianLevel.ELDER.getRequiredExperience());
        assertEquals(3500, GuardianLevel.SAGE.getRequiredExperience());
        assertEquals(5500, GuardianLevel.MASTER.getRequiredExperience());
        assertEquals(8500, GuardianLevel.LEGEND.getRequiredExperience());
        assertEquals(13000, GuardianLevel.CHAMPION.getRequiredExperience());
    }

    @Test
    void shouldReturnCorrectDisplayNames() {
        // When & Then
        assertEquals("Iniciado", GuardianLevel.INITIATE.getDisplayName());
        assertEquals("Aprendiz", GuardianLevel.APPRENTICE.getDisplayName());
        assertEquals("Protector", GuardianLevel.PROTECTOR.getDisplayName());
        assertEquals("Guardián", GuardianLevel.KEEPER.getDisplayName());
        assertEquals("Gran Guardián", GuardianLevel.GUARDIAN.getDisplayName());
        assertEquals("Anciano", GuardianLevel.ELDER.getDisplayName());
        assertEquals("Sabio", GuardianLevel.SAGE.getDisplayName());
        assertEquals("Maestro", GuardianLevel.MASTER.getDisplayName());
        assertEquals("Leyenda", GuardianLevel.LEGEND.getDisplayName());
        assertEquals("Campeón", GuardianLevel.CHAMPION.getDisplayName());
    }

    @Test
    void shouldReturnCorrectNextLevel() {
        // When & Then
        assertEquals(GuardianLevel.APPRENTICE, GuardianLevel.INITIATE.getNextLevel());
        assertEquals(GuardianLevel.PROTECTOR, GuardianLevel.APPRENTICE.getNextLevel());
        assertEquals(GuardianLevel.KEEPER, GuardianLevel.PROTECTOR.getNextLevel());
        assertEquals(GuardianLevel.GUARDIAN, GuardianLevel.KEEPER.getNextLevel());
        assertEquals(GuardianLevel.ELDER, GuardianLevel.GUARDIAN.getNextLevel());
        assertEquals(GuardianLevel.SAGE, GuardianLevel.ELDER.getNextLevel());
        assertEquals(GuardianLevel.MASTER, GuardianLevel.SAGE.getNextLevel());
        assertEquals(GuardianLevel.LEGEND, GuardianLevel.MASTER.getNextLevel());
        assertEquals(GuardianLevel.CHAMPION, GuardianLevel.LEGEND.getNextLevel());
        
        // Champion is the max level, so next level should be itself
        assertEquals(GuardianLevel.CHAMPION, GuardianLevel.CHAMPION.getNextLevel());
    }

    @ParameterizedTest
    @CsvSource({
        "INITIATE, 50, 50",
        "INITIATE, 99, 1",
        "APPRENTICE, 150, 100",
        "APPRENTICE, 249, 1",
        "PROTECTOR, 300, 200",
        "KEEPER, 750, 250",
        "GUARDIAN, 1500, 500",
        "ELDER, 2500, 1000",
        "SAGE, 4000, 1500",
        "MASTER, 6000, 2500",
        "LEGEND, 10000, 3000",
        "CHAMPION, 20000, 0"
    })
    void shouldCalculateExperienceToNextLevel(GuardianLevel level, int currentExperience, int expectedExperienceNeeded) {
        // When
        int experienceToNext = level.getExperienceToNextLevel(currentExperience);

        // Then
        assertEquals(expectedExperienceNeeded, experienceToNext);
    }

    @Test
    void shouldReturnZeroExperienceToNextLevelForMaxLevel() {
        // Given
        GuardianLevel maxLevel = GuardianLevel.CHAMPION;
        int currentExperience = 50000;

        // When
        int experienceToNext = maxLevel.getExperienceToNextLevel(currentExperience);

        // Then
        assertEquals(0, experienceToNext);
    }
}
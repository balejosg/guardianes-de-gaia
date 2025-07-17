package com.guardianes.guardian.domain.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class GuardianTest {

    @Test
    void shouldCreateGuardianWithBasicConstructor() {
        // Given
        String username = "testuser";
        String email = "test@example.com";
        String passwordHash = "hashedPassword";
        String name = "Test User";
        LocalDate birthDate = LocalDate.of(2010, 1, 15);

        // When
        Guardian guardian = new Guardian(username, email, passwordHash, name, birthDate);

        // Then
        assertNotNull(guardian);
        assertEquals(username, guardian.getUsername());
        assertEquals(email, guardian.getEmail());
        assertEquals(passwordHash, guardian.getPasswordHash());
        assertEquals(name, guardian.getName());
        assertEquals(birthDate, guardian.getBirthDate());
        assertEquals(GuardianLevel.INITIATE, guardian.getLevel());
        assertEquals(0, guardian.getExperiencePoints());
        assertEquals(0, guardian.getTotalSteps());
        assertEquals(0, guardian.getTotalEnergyGenerated());
        assertTrue(guardian.isActive());
        assertNotNull(guardian.getCreatedAt());
        assertNotNull(guardian.getLastActiveAt());
    }

    @Test
    void shouldCreateGuardianWithFullConstructor() {
        // Given
        Long id = 1L;
        String username = "testuser";
        String email = "test@example.com";
        String passwordHash = "hashedPassword";
        String name = "Test User";
        LocalDate birthDate = LocalDate.of(2010, 1, 15);
        GuardianLevel level = GuardianLevel.PROTECTOR;
        int experiencePoints = 500;
        int totalSteps = 10000;
        int totalEnergyGenerated = 1000;
        LocalDateTime createdAt = LocalDateTime.now().minusDays(30);
        LocalDateTime lastActiveAt = LocalDateTime.now().minusHours(1);
        boolean active = true;

        // When
        Guardian guardian = new Guardian(id, username, email, passwordHash, name, birthDate,
                level, experiencePoints, totalSteps, totalEnergyGenerated, createdAt, lastActiveAt, active);

        // Then
        assertEquals(id, guardian.getId());
        assertEquals(username, guardian.getUsername());
        assertEquals(email, guardian.getEmail());
        assertEquals(passwordHash, guardian.getPasswordHash());
        assertEquals(name, guardian.getName());
        assertEquals(birthDate, guardian.getBirthDate());
        assertEquals(level, guardian.getLevel());
        assertEquals(experiencePoints, guardian.getExperiencePoints());
        assertEquals(totalSteps, guardian.getTotalSteps());
        assertEquals(totalEnergyGenerated, guardian.getTotalEnergyGenerated());
        assertEquals(createdAt, guardian.getCreatedAt());
        assertEquals(lastActiveAt, guardian.getLastActiveAt());
        assertEquals(active, guardian.isActive());
    }

    @Test
    void shouldAddExperienceAndUpdateLevel() {
        // Given
        Guardian guardian = createTestGuardian();
        assertEquals(GuardianLevel.INITIATE, guardian.getLevel());

        // When - Add enough experience to reach APPRENTICE level
        guardian.addExperience(150);

        // Then
        assertEquals(150, guardian.getExperiencePoints());
        assertEquals(GuardianLevel.APPRENTICE, guardian.getLevel());
    }

    @Test
    void shouldAddStepsCorrectly() {
        // Given
        Guardian guardian = createTestGuardian();
        assertEquals(0, guardian.getTotalSteps());

        // When
        guardian.addSteps(1000);
        guardian.addSteps(500);

        // Then
        assertEquals(1500, guardian.getTotalSteps());
    }

    @Test
    void shouldAddEnergyGeneratedCorrectly() {
        // Given
        Guardian guardian = createTestGuardian();
        assertEquals(0, guardian.getTotalEnergyGenerated());

        // When
        guardian.addEnergyGenerated(100);
        guardian.addEnergyGenerated(50);

        // Then
        assertEquals(150, guardian.getTotalEnergyGenerated());
    }

    @Test
    void shouldUpdateLastActiveTime() {
        // Given
        Guardian guardian = createTestGuardian();
        LocalDateTime originalLastActive = guardian.getLastActiveAt();

        // When
        try {
            Thread.sleep(1); // Ensure time difference
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        guardian.updateLastActive();

        // Then
        assertTrue(guardian.getLastActiveAt().isAfter(originalLastActive));
    }

    @Test
    void shouldActivateAndDeactivateGuardian() {
        // Given
        Guardian guardian = createTestGuardian();
        assertTrue(guardian.isActive());

        // When
        guardian.deactivate();

        // Then
        assertFalse(guardian.isActive());

        // When
        guardian.activate();

        // Then
        assertTrue(guardian.isActive());
    }

    @Test
    void shouldCalculateAgeCorrectly() {
        // Given
        LocalDate birthDate = LocalDate.now().minusYears(10).minusMonths(6);
        Guardian guardian = new Guardian("test", "test@example.com", "hash", "Test", birthDate);

        // When
        int age = guardian.getAge();

        // Then
        assertEquals(10, age);
    }

    @Test
    void shouldIdentifyChildCorrectly() {
        // Given - Child Guardian (age 8)
        LocalDate childBirthDate = LocalDate.now().minusYears(8);
        Guardian childGuardian = new Guardian("child", "child@example.com", "hash", "Child", childBirthDate);

        // Adult Guardian (age 25)
        LocalDate adultBirthDate = LocalDate.now().minusYears(25);
        Guardian adultGuardian = new Guardian("adult", "adult@example.com", "hash", "Adult", adultBirthDate);

        // When & Then
        assertTrue(childGuardian.isChild());
        assertFalse(childGuardian.isAdult());

        assertFalse(adultGuardian.isChild());
        assertTrue(adultGuardian.isAdult());
    }

    @Test
    void shouldHandleEqualityCorrectly() {
        // Given
        Guardian guardian1 = new Guardian(1L, "test", "test@example.com", "hash", "Test",
                LocalDate.of(2010, 1, 1), GuardianLevel.INITIATE, 0, 0, 0,
                LocalDateTime.now(), LocalDateTime.now(), true);

        Guardian guardian2 = new Guardian(1L, "test", "test@example.com", "hash", "Test",
                LocalDate.of(2010, 1, 1), GuardianLevel.INITIATE, 0, 0, 0,
                LocalDateTime.now(), LocalDateTime.now(), true);

        Guardian guardian3 = new Guardian(2L, "different", "different@example.com", "hash", "Different",
                LocalDate.of(2010, 1, 1), GuardianLevel.INITIATE, 0, 0, 0,
                LocalDateTime.now(), LocalDateTime.now(), true);

        // When & Then
        assertEquals(guardian1, guardian2);
        assertEquals(guardian1.hashCode(), guardian2.hashCode());
        
        assertNotEquals(guardian1, guardian3);
        assertNotEquals(guardian1.hashCode(), guardian3.hashCode());
    }

    @Test
    void shouldGenerateToStringCorrectly() {
        // Given
        Guardian guardian = createTestGuardian();

        // When
        String result = guardian.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Guardian{"));
        assertTrue(result.contains("username='testuser'"));
        assertTrue(result.contains("level=INITIATE"));
        assertTrue(result.contains("active=true"));
    }

    private Guardian createTestGuardian() {
        return new Guardian("testuser", "test@example.com", "hashedPassword", 
                "Test User", LocalDate.of(2010, 1, 15));
    }
}
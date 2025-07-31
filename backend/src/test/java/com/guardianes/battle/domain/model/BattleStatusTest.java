package com.guardianes.battle.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BattleStatus Enum Tests")
class BattleStatusTest {

  @DisplayName("Battle Status Properties Tests")
  class BattleStatusPropertiesTests {

    @Test
    @DisplayName("should have correct properties for PENDING")
    void shouldHaveCorrectPropertiesForPending() {
      // Given
      BattleStatus pending = BattleStatus.PENDING;

      // Then
      assertEquals("Pending", pending.getDisplayName());
      assertEquals("Battle is waiting to begin", pending.getDescription());
      assertTrue(pending.canStart());
      assertFalse(pending.isActive());
      assertFalse(pending.isFinished());
      assertFalse(pending.allowsMoves());
    }

    @Test
    @DisplayName("should have correct properties for IN_PROGRESS")
    void shouldHaveCorrectPropertiesForInProgress() {
      // Given
      BattleStatus inProgress = BattleStatus.IN_PROGRESS;

      // Then
      assertEquals("In Progress", inProgress.getDisplayName());
      assertEquals("Battle is ongoing", inProgress.getDescription());
      assertFalse(inProgress.canStart());
      assertTrue(inProgress.isActive());
      assertFalse(inProgress.isFinished());
      assertTrue(inProgress.allowsMoves());
    }

    @Test
    @DisplayName("should have correct properties for COMPLETED")
    void shouldHaveCorrectPropertiesForCompleted() {
      // Given
      BattleStatus completed = BattleStatus.COMPLETED;

      // Then
      assertEquals("Completed", completed.getDisplayName());
      assertEquals("Battle has ended", completed.getDescription());
      assertFalse(completed.canStart());
      assertFalse(completed.isActive());
      assertTrue(completed.isFinished());
      assertFalse(completed.allowsMoves());
    }

    @Test
    @DisplayName("should have correct properties for ABANDONED")
    void shouldHaveCorrectPropertiesForAbandoned() {
      // Given
      BattleStatus abandoned = BattleStatus.ABANDONED;

      // Then
      assertEquals("Abandoned", abandoned.getDisplayName());
      assertEquals("Battle was abandoned", abandoned.getDescription());
      assertFalse(abandoned.canStart());
      assertFalse(abandoned.isActive());
      assertTrue(abandoned.isFinished());
      assertFalse(abandoned.allowsMoves());
    }
  }

  @DisplayName("Battle Status Transitions Tests")
  class BattleStatusTransitionsTests {

    @Test
    @DisplayName("should have correct valid transitions from PENDING")
    void shouldHaveCorrectValidTransitionsFromPending() {
      // Given
      BattleStatus pending = BattleStatus.PENDING;

      // When
      BattleStatus[] validTransitions = pending.getValidTransitions();

      // Then
      assertEquals(2, validTransitions.length);
      assertTrue(contains(validTransitions, BattleStatus.IN_PROGRESS));
      assertTrue(contains(validTransitions, BattleStatus.ABANDONED));
    }

    @Test
    @DisplayName("should have correct valid transitions from IN_PROGRESS")
    void shouldHaveCorrectValidTransitionsFromInProgress() {
      // Given
      BattleStatus inProgress = BattleStatus.IN_PROGRESS;

      // When
      BattleStatus[] validTransitions = inProgress.getValidTransitions();

      // Then
      assertEquals(2, validTransitions.length);
      assertTrue(contains(validTransitions, BattleStatus.COMPLETED));
      assertTrue(contains(validTransitions, BattleStatus.ABANDONED));
    }

    @Test
    @DisplayName("should have no valid transitions from COMPLETED")
    void shouldHaveNoValidTransitionsFromCompleted() {
      // Given
      BattleStatus completed = BattleStatus.COMPLETED;

      // When
      BattleStatus[] validTransitions = completed.getValidTransitions();

      // Then
      assertEquals(0, validTransitions.length);
    }

    @Test
    @DisplayName("should have no valid transitions from ABANDONED")
    void shouldHaveNoValidTransitionsFromAbandoned() {
      // Given
      BattleStatus abandoned = BattleStatus.ABANDONED;

      // When
      BattleStatus[] validTransitions = abandoned.getValidTransitions();

      // Then
      assertEquals(0, validTransitions.length);
    }

    @Test
    @DisplayName("should validate transition from PENDING to IN_PROGRESS")
    void shouldValidateTransitionFromPendingToInProgress() {
      // Given
      BattleStatus pending = BattleStatus.PENDING;

      // Then
      assertTrue(pending.canTransitionTo(BattleStatus.IN_PROGRESS));
      assertTrue(pending.canTransitionTo(BattleStatus.ABANDONED));
      assertFalse(pending.canTransitionTo(BattleStatus.COMPLETED));
      assertFalse(pending.canTransitionTo(BattleStatus.PENDING));
    }

    @Test
    @DisplayName("should validate transition from IN_PROGRESS to final states")
    void shouldValidateTransitionFromInProgressToFinalStates() {
      // Given
      BattleStatus inProgress = BattleStatus.IN_PROGRESS;

      // Then
      assertTrue(inProgress.canTransitionTo(BattleStatus.COMPLETED));
      assertTrue(inProgress.canTransitionTo(BattleStatus.ABANDONED));
      assertFalse(inProgress.canTransitionTo(BattleStatus.IN_PROGRESS));
      assertFalse(inProgress.canTransitionTo(BattleStatus.PENDING));
    }

    @Test
    @DisplayName("should not allow transitions from COMPLETED")
    void shouldNotAllowTransitionsFromCompleted() {
      // Given
      BattleStatus completed = BattleStatus.COMPLETED;

      // Then
      assertFalse(completed.canTransitionTo(BattleStatus.PENDING));
      assertFalse(completed.canTransitionTo(BattleStatus.IN_PROGRESS));
      assertFalse(completed.canTransitionTo(BattleStatus.ABANDONED));
      assertFalse(completed.canTransitionTo(BattleStatus.COMPLETED));
    }

    @Test
    @DisplayName("should not allow transitions from ABANDONED")
    void shouldNotAllowTransitionsFromAbandoned() {
      // Given
      BattleStatus abandoned = BattleStatus.ABANDONED;

      // Then
      assertFalse(abandoned.canTransitionTo(BattleStatus.PENDING));
      assertFalse(abandoned.canTransitionTo(BattleStatus.IN_PROGRESS));
      assertFalse(abandoned.canTransitionTo(BattleStatus.COMPLETED));
      assertFalse(abandoned.canTransitionTo(BattleStatus.ABANDONED));
    }

    private boolean contains(BattleStatus[] array, BattleStatus status) {
      for (BattleStatus s : array) {
        if (s == status) {
          return true;
        }
      }
      return false;
    }
  }

  @DisplayName("Battle Status String Representation Tests")
  class BattleStatusStringRepresentationTests {

    @Test
    @DisplayName("should return display name for toString")
    void shouldReturnDisplayNameForToString() {
      // Given
      BattleStatus inProgress = BattleStatus.IN_PROGRESS;

      // Then
      assertEquals("In Progress", inProgress.toString());
      assertEquals(inProgress.getDisplayName(), inProgress.toString());
    }

    @Test
    @DisplayName("should have non-null display names")
    void shouldHaveNonNullDisplayNames() {
      // Then
      assertNotNull(BattleStatus.PENDING.getDisplayName());
      assertNotNull(BattleStatus.IN_PROGRESS.getDisplayName());
      assertNotNull(BattleStatus.COMPLETED.getDisplayName());
      assertNotNull(BattleStatus.ABANDONED.getDisplayName());

      assertFalse(BattleStatus.PENDING.getDisplayName().trim().isEmpty());
      assertFalse(BattleStatus.IN_PROGRESS.getDisplayName().trim().isEmpty());
      assertFalse(BattleStatus.COMPLETED.getDisplayName().trim().isEmpty());
      assertFalse(BattleStatus.ABANDONED.getDisplayName().trim().isEmpty());
    }

    @Test
    @DisplayName("should have non-null descriptions")
    void shouldHaveNonNullDescriptions() {
      // Then
      assertNotNull(BattleStatus.PENDING.getDescription());
      assertNotNull(BattleStatus.IN_PROGRESS.getDescription());
      assertNotNull(BattleStatus.COMPLETED.getDescription());
      assertNotNull(BattleStatus.ABANDONED.getDescription());

      assertFalse(BattleStatus.PENDING.getDescription().trim().isEmpty());
      assertFalse(BattleStatus.IN_PROGRESS.getDescription().trim().isEmpty());
      assertFalse(BattleStatus.COMPLETED.getDescription().trim().isEmpty());
      assertFalse(BattleStatus.ABANDONED.getDescription().trim().isEmpty());
    }
  }

  @DisplayName("Battle Status Enum Coverage Tests")
  class BattleStatusEnumCoverageTests {

    @Test
    @DisplayName("should have exactly four battle statuses defined")
    void shouldHaveExactlyFourBattleStatusesDefined() {
      // When
      BattleStatus[] battleStatuses = BattleStatus.values();

      // Then
      assertEquals(4, battleStatuses.length);
      assertEquals(BattleStatus.PENDING, battleStatuses[0]);
      assertEquals(BattleStatus.IN_PROGRESS, battleStatuses[1]);
      assertEquals(BattleStatus.COMPLETED, battleStatuses[2]);
      assertEquals(BattleStatus.ABANDONED, battleStatuses[3]);
    }

    @Test
    @DisplayName("should be able to convert from string")
    void shouldBeAbleToConvertFromString() {
      // When & Then
      assertEquals(BattleStatus.PENDING, BattleStatus.valueOf("PENDING"));
      assertEquals(BattleStatus.IN_PROGRESS, BattleStatus.valueOf("IN_PROGRESS"));
      assertEquals(BattleStatus.COMPLETED, BattleStatus.valueOf("COMPLETED"));
      assertEquals(BattleStatus.ABANDONED, BattleStatus.valueOf("ABANDONED"));
    }

    @Test
    @DisplayName("should throw exception for invalid enum value")
    void shouldThrowExceptionForInvalidEnumValue() {
      // When & Then
      assertThrows(IllegalArgumentException.class, () -> BattleStatus.valueOf("INVALID_STATUS"));
    }
  }

  @DisplayName("Battle Status Logic Tests")
  class BattleStatusLogicTests {

    @Test
    @DisplayName("should have correct lifecycle progression")
    void shouldHaveCorrectLifecycleProgression() {
      // Test normal flow: PENDING -> IN_PROGRESS -> COMPLETED
      assertTrue(BattleStatus.PENDING.canTransitionTo(BattleStatus.IN_PROGRESS));
      assertTrue(BattleStatus.IN_PROGRESS.canTransitionTo(BattleStatus.COMPLETED));

      // Test abandonment flow: PENDING -> ABANDONED or IN_PROGRESS -> ABANDONED
      assertTrue(BattleStatus.PENDING.canTransitionTo(BattleStatus.ABANDONED));
      assertTrue(BattleStatus.IN_PROGRESS.canTransitionTo(BattleStatus.ABANDONED));

      // Test that final states don't transition
      assertFalse(BattleStatus.COMPLETED.canTransitionTo(BattleStatus.ABANDONED));
      assertFalse(BattleStatus.ABANDONED.canTransitionTo(BattleStatus.COMPLETED));
    }

    @Test
    @DisplayName("should only allow moves in IN_PROGRESS state")
    void shouldOnlyAllowMovesInInProgressState() {
      // Then
      assertFalse(BattleStatus.PENDING.allowsMoves());
      assertTrue(BattleStatus.IN_PROGRESS.allowsMoves());
      assertFalse(BattleStatus.COMPLETED.allowsMoves());
      assertFalse(BattleStatus.ABANDONED.allowsMoves());
    }

    @Test
    @DisplayName("should correctly identify active states")
    void shouldCorrectlyIdentifyActiveStates() {
      // Then
      assertFalse(BattleStatus.PENDING.isActive());
      assertTrue(BattleStatus.IN_PROGRESS.isActive());
      assertFalse(BattleStatus.COMPLETED.isActive());
      assertFalse(BattleStatus.ABANDONED.isActive());
    }

    @Test
    @DisplayName("should correctly identify finished states")
    void shouldCorrectlyIdentifyFinishedStates() {
      // Then
      assertFalse(BattleStatus.PENDING.isFinished());
      assertFalse(BattleStatus.IN_PROGRESS.isFinished());
      assertTrue(BattleStatus.COMPLETED.isFinished());
      assertTrue(BattleStatus.ABANDONED.isFinished());
    }
  }
}

package com.guardianes.guardian.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.guardianes.guardian.application.dto.GuardianProfileResponse;
import com.guardianes.guardian.application.dto.GuardianRegistrationRequest;
import com.guardianes.guardian.application.service.GuardianApplicationService;
import com.guardianes.guardian.domain.model.Guardian;
import com.guardianes.guardian.domain.model.GuardianLevel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class GuardianControllerTest {

  @Mock private GuardianApplicationService guardianApplicationService;

  private GuardianController guardianController;

  @BeforeEach
  void setUp() {
    guardianController = new GuardianController(guardianApplicationService);
  }

  @Test
  public void shouldCreateGuardianProfile() {
    // Given
    GuardianRegistrationRequest request =
        new GuardianRegistrationRequest(
            "testuser", "test@example.com", "password123", "Test User", LocalDate.of(2015, 5, 15));

    Guardian guardian =
        new Guardian(
            1L,
            "testuser",
            "test@example.com",
            "hashedPassword",
            "Test User",
            LocalDate.of(2015, 5, 15),
            GuardianLevel.INITIATE,
            0,
            0,
            0,
            LocalDateTime.now(),
            LocalDateTime.now(),
            true);

    when(guardianApplicationService.createGuardianProfile(any(GuardianRegistrationRequest.class)))
        .thenReturn(guardian);

    // When
    ResponseEntity<Guardian> response = guardianController.createGuardianProfile(request);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getId()).isEqualTo(1L);
    assertThat(response.getBody().getUsername()).isEqualTo("testuser");
    assertThat(response.getBody().getName()).isEqualTo("Test User");
  }

  @Test
  public void shouldGetGuardianProfile() {
    // Given
    Long guardianId = 1L;
    Guardian guardian =
        new Guardian(
            guardianId,
            "testuser",
            "test@example.com",
            "hashedPassword",
            "Test User",
            LocalDate.of(2015, 5, 15),
            GuardianLevel.INITIATE,
            0,
            0,
            0,
            LocalDateTime.now(),
            LocalDateTime.now(),
            true);

    GuardianProfileResponse profileResponse =
        new GuardianProfileResponse(
            guardian.getId(),
            guardian.getUsername(),
            guardian.getEmail(),
            guardian.getName(),
            guardian.getBirthDate(),
            guardian.getAge(),
            guardian.getLevel(),
            guardian.getExperiencePoints(),
            guardian.getLevel().getExperienceToNextLevel(guardian.getExperiencePoints()),
            guardian.getTotalSteps(),
            guardian.getTotalEnergyGenerated(),
            guardian.getCreatedAt(),
            guardian.getLastActiveAt(),
            guardian.isChild());

    when(guardianApplicationService.getGuardianProfile(eq(guardianId)))
        .thenReturn(Optional.of(profileResponse));

    // When
    ResponseEntity<GuardianProfileResponse> response =
        guardianController.getGuardianProfile(guardianId);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().id()).isEqualTo(1L);
    assertThat(response.getBody().username()).isEqualTo("testuser");
    assertThat(response.getBody().name()).isEqualTo("Test User");
  }

  @Test
  public void shouldReturn404WhenGuardianNotFound() {
    // Given
    Long guardianId = 999L;
    when(guardianApplicationService.getGuardianProfile(eq(guardianId)))
        .thenReturn(Optional.empty());

    // When
    ResponseEntity<GuardianProfileResponse> response =
        guardianController.getGuardianProfile(guardianId);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNull();
  }
}

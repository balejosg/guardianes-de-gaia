package com.guardianes.walking.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.guardianes.testconfig.GuardianTestConfiguration;
import com.guardianes.walking.application.dto.StepSubmissionRequest;
import com.guardianes.walking.application.dto.StepSubmissionResponse;
import com.guardianes.walking.application.service.StepTrackingApplicationService;
import com.guardianes.walking.domain.EnergyRepository;
import com.guardianes.walking.domain.EnergyTransaction;
import com.guardianes.walking.domain.EnergyTransactionType;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(GuardianTestConfiguration.class)
public class StepSubmissionEnergyIntegrationTest {

  @Autowired private StepTrackingApplicationService stepTrackingService;

  @Autowired private EnergyRepository energyRepository;

  @Test
  public void shouldCreateEnergyTransactionWhenStepsAreSubmitted() {
    // Given
    Long guardianId = 1L;
    int stepCount = 1000; // Should generate 100 energy (1000 steps / 10)
    LocalDateTime timestamp = LocalDateTime.now();
    StepSubmissionRequest request = new StepSubmissionRequest(stepCount, timestamp);

    // When
    StepSubmissionResponse response = stepTrackingService.submitSteps(guardianId, request);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.energyEarned()).isEqualTo(100);

    // Verify that an energy transaction was created
    List<EnergyTransaction> transactions =
        energyRepository.findTransactionsByGuardianId(guardianId);
    assertThat(transactions).hasSize(1);

    EnergyTransaction transaction = transactions.get(0);
    assertThat(transaction.getGuardianId()).isEqualTo(guardianId);
    assertThat(transaction.getType()).isEqualTo(EnergyTransactionType.EARNED);
    assertThat(transaction.getAmount()).isEqualTo(100);
    assertThat(transaction.getSource()).isEqualTo("Steps");
    assertThat(transaction.getTimestamp()).isEqualTo(timestamp);

    // Verify energy balance is updated
    int balance = energyRepository.getEnergyBalance(guardianId);
    assertThat(balance).isEqualTo(100);
  }

  @Test
  public void shouldAccumulateEnergyFromMultipleStepSubmissions() {
    // Given
    Long guardianId = 2L;
    LocalDateTime timestamp1 = LocalDateTime.now().minusHours(1);
    LocalDateTime timestamp2 = LocalDateTime.now();

    StepSubmissionRequest request1 = new StepSubmissionRequest(500, timestamp1); // 50 energy
    StepSubmissionRequest request2 = new StepSubmissionRequest(750, timestamp2); // 75 energy

    // When
    stepTrackingService.submitSteps(guardianId, request1);
    stepTrackingService.submitSteps(guardianId, request2);

    // Then
    List<EnergyTransaction> transactions =
        energyRepository.findTransactionsByGuardianId(guardianId);
    assertThat(transactions).hasSize(2);

    int totalBalance = energyRepository.getEnergyBalance(guardianId);
    assertThat(totalBalance).isEqualTo(125); // 50 + 75 = 125
  }
}

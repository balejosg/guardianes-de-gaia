package com.guardianes.walking.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.guardianes.shared.domain.model.GuardianId;
import com.guardianes.walking.application.dto.CurrentStepCountResponse;
import com.guardianes.walking.application.dto.EnergyBalanceResponse;
import com.guardianes.walking.application.dto.EnergySpendingResponse;
import com.guardianes.walking.application.dto.StepHistoryResponse;
import com.guardianes.walking.application.dto.StepSubmissionResponse;
import com.guardianes.walking.domain.model.DailyStepAggregate;
import com.guardianes.walking.domain.model.Energy;
import com.guardianes.walking.domain.model.EnergySpendingSource;
import com.guardianes.walking.domain.model.StepCount;
import java.time.LocalDate;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

@JsonTest
@DisplayName("JSON Serialization Tests")
class JsonSerializationTest {

    @Autowired private JacksonTester<StepSubmissionResponse> stepSubmissionResponseJson;

    @Autowired private JacksonTester<CurrentStepCountResponse> currentStepCountResponseJson;

    @Autowired private JacksonTester<StepHistoryResponse> stepHistoryResponseJson;

    @Autowired private JacksonTester<EnergyBalanceResponse> energyBalanceResponseJson;

    @Autowired private JacksonTester<EnergySpendingResponse> energySpendingResponseJson;

    @Test
    @DisplayName("Should serialize StepSubmissionResponse correctly")
    void shouldSerializeStepSubmissionResponse() throws Exception {
        StepSubmissionResponse response =
                new StepSubmissionResponse(1L, 3500, 350, "Steps submitted successfully");

        String json = stepSubmissionResponseJson.write(response).getJson();

        assertThat(json).containsSubsequence("\"guardianId\":1");
        assertThat(json).containsSubsequence("\"totalDailySteps\":3500");
        assertThat(json).containsSubsequence("\"energyEarned\":350");
        assertThat(json).containsSubsequence("\"message\":\"Steps submitted successfully\"");
    }

    @Test
    @DisplayName("Should serialize CurrentStepCountResponse correctly")
    void shouldSerializeCurrentStepCountResponse() throws Exception {
        CurrentStepCountResponse response =
                new CurrentStepCountResponse(1L, 4200, 420, LocalDate.of(2025, 7, 8));

        String json = currentStepCountResponseJson.write(response).getJson();

        assertThat(json).containsSubsequence("\"guardianId\":1");
        assertThat(json).containsSubsequence("\"currentSteps\":4200");
        assertThat(json).containsSubsequence("\"availableEnergy\":420");
        assertThat(json).containsSubsequence("\"date\":\"2025-07-08\"");
    }

    @Test
    @DisplayName("Should serialize StepHistoryResponse correctly")
    void shouldSerializeStepHistoryResponse() throws Exception {
        StepHistoryResponse response =
                new StepHistoryResponse(
                        1L,
                        Arrays.asList(
                                DailyStepAggregate.create(
                                        GuardianId.of(1L),
                                        LocalDate.of(2025, 7, 1),
                                        StepCount.of(2000)),
                                DailyStepAggregate.create(
                                        GuardianId.of(1L),
                                        LocalDate.of(2025, 7, 2),
                                        StepCount.of(3000))));

        String json = stepHistoryResponseJson.write(response).getJson();

        assertThat(json).containsSubsequence("\"guardianId\":1");
        assertThat(json).containsSubsequence("\"dailySteps\":[");
        assertThat(json).containsSubsequence("\"totalSteps\":{\"value\":2000}");
        assertThat(json).containsSubsequence("\"totalSteps\":{\"value\":3000}");
    }

    @Test
    @DisplayName("Should serialize EnergyBalanceResponse correctly")
    void shouldSerializeEnergyBalanceResponse() throws Exception {
        EnergyBalanceResponse response =
                new EnergyBalanceResponse(GuardianId.of(1L), Energy.of(750), Arrays.asList());

        String json = energyBalanceResponseJson.write(response).getJson();

        assertThat(json).containsSubsequence("\"guardianId\":{\"value\":1}");
        assertThat(json).containsSubsequence("\"currentBalance\":{\"amount\":750}");
        assertThat(json).containsSubsequence("\"transactionSummary\":[]");
    }

    @Test
    @DisplayName("Should serialize EnergySpendingResponse correctly")
    void shouldSerializeEnergySpendingResponse() throws Exception {
        EnergySpendingResponse response =
                new EnergySpendingResponse(
                        GuardianId.of(1L),
                        Energy.of(400),
                        Energy.of(100),
                        EnergySpendingSource.BATTLE,
                        "Energy spent successfully");

        String json = energySpendingResponseJson.write(response).getJson();

        assertThat(json).containsSubsequence("\"guardianId\":{\"value\":1}");
        assertThat(json).containsSubsequence("\"newBalance\":{\"amount\":400}");
        assertThat(json).containsSubsequence("\"amountSpent\":{\"amount\":100}");
        assertThat(json).containsSubsequence("\"source\":\"BATTLE\"");
        assertThat(json).containsSubsequence("\"message\":\"Energy spent successfully\"");
    }
}

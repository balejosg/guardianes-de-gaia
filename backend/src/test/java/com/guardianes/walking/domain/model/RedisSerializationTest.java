package com.guardianes.walking.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guardianes.shared.domain.model.GuardianId;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

@JsonTest
@DisplayName("Redis Serialization Tests")
class RedisSerializationTest {

    @Autowired private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should serialize and deserialize DailyStepAggregate")
    void shouldSerializeDailyStepAggregate() throws Exception {
        DailyStepAggregate original =
                DailyStepAggregate.create(
                        GuardianId.of(1L), LocalDate.of(2025, 7, 8), StepCount.of(2500));

        String json = objectMapper.writeValueAsString(original);
        DailyStepAggregate deserialized = objectMapper.readValue(json, DailyStepAggregate.class);

        assertThat(deserialized).isEqualTo(original);
        assertThat(deserialized.getGuardianId()).isEqualTo(original.getGuardianId());
        assertThat(deserialized.getDate()).isEqualTo(original.getDate());
        assertThat(deserialized.getTotalSteps()).isEqualTo(original.getTotalSteps());
    }

    @Test
    @DisplayName("Should serialize and deserialize StepCount")
    void shouldSerializeStepCount() throws Exception {
        StepCount original = StepCount.of(3500);

        String json = objectMapper.writeValueAsString(original);
        StepCount deserialized = objectMapper.readValue(json, StepCount.class);

        assertThat(deserialized).isEqualTo(original);
        assertThat(deserialized.value()).isEqualTo(original.value());
    }

    @Test
    @DisplayName("Should serialize and deserialize Energy")
    void shouldSerializeEnergy() throws Exception {
        Energy original = Energy.of(350);

        String json = objectMapper.writeValueAsString(original);
        Energy deserialized = objectMapper.readValue(json, Energy.class);

        assertThat(deserialized).isEqualTo(original);
        assertThat(deserialized.amount()).isEqualTo(original.amount());
    }

    @Test
    @DisplayName("Should exclude computed fields from serialization")
    void shouldExcludeComputedFields() throws Exception {
        DailyStepAggregate aggregate =
                DailyStepAggregate.create(GuardianId.of(1L), LocalDate.now(), StepCount.of(2500));

        String json = objectMapper.writeValueAsString(aggregate);

        // Should NOT contain computed fields
        assertThat(json).doesNotContain("\"today\"");
        assertThat(json).doesNotContain("\"guardianIdValue\"");
        assertThat(json).doesNotContain("\"totalStepsValue\"");

        // Should contain actual fields
        assertThat(json).contains("\"guardianId\"");
        assertThat(json).contains("\"date\"");
        assertThat(json).contains("\"totalSteps\"");
    }

    @Test
    @DisplayName("Should exclude computed fields from StepCount serialization")
    void shouldExcludeComputedFieldsFromStepCount() throws Exception {
        StepCount stepCount = StepCount.of(0);

        String json = objectMapper.writeValueAsString(stepCount);

        // Should NOT contain computed fields
        assertThat(json).doesNotContain("\"zero\"");

        // Should contain actual field
        assertThat(json).contains("\"value\"");
    }

    @Test
    @DisplayName("Should exclude computed fields from Energy serialization")
    void shouldExcludeComputedFieldsFromEnergy() throws Exception {
        Energy energy = Energy.of(0);

        String json = objectMapper.writeValueAsString(energy);

        // Should NOT contain computed fields
        assertThat(json).doesNotContain("\"zero\"");

        // Should contain actual field
        assertThat(json).contains("\"amount\"");
    }
}

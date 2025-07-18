package com.guardianes.testconfig;

import com.guardianes.walking.domain.EnergyRepository;
import com.guardianes.walking.domain.StepRepository;
import com.guardianes.walking.infrastructure.repository.InMemoryEnergyRepository;
import com.guardianes.walking.infrastructure.repository.InMemoryStepRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class TestRepositoryConfiguration {

  @Bean
  @Primary
  public StepRepository stepRepository() {
    return new InMemoryStepRepository();
  }

  @Bean
  @Primary
  public EnergyRepository energyRepository() {
    return new InMemoryEnergyRepository();
  }
}

package com.guardianes.shared.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.guardianes")
@ConditionalOnProperty(name = "guardianes.jpa.enabled", havingValue = "true", matchIfMissing = true)
public class JpaConfig {
}
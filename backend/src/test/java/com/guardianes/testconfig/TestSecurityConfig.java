package com.guardianes.testconfig;

import static org.mockito.Mockito.mock;

import com.guardianes.shared.infrastructure.security.JwtAuthenticationFilter;
import com.guardianes.shared.infrastructure.security.JwtService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
public class TestSecurityConfig {

  @Bean
  @Primary
  public JwtService jwtService() {
    return mock(JwtService.class);
  }

  @Bean
  @Primary
  public JwtAuthenticationFilter jwtAuthenticationFilter() {
    return mock(JwtAuthenticationFilter.class);
  }

  @Bean
  @Primary
  public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .build();
  }
}

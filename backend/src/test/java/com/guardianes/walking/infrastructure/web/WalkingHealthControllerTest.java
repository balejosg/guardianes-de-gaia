package com.guardianes.walking.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    value = WalkingHealthController.class,
    excludeAutoConfiguration = {
      org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
      org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class,
      org.springframework.boot.autoconfigure.data.web.SpringDataWebAutoConfiguration.class,
      org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration.class,
      org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class,
      org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration.class
    })
@TestPropertySource(properties = {"guardianes.jwt.enabled=false", "guardianes.jpa.enabled=false"})
@WithMockUser(
    username = "admin",
    roles = {"ADMIN"})
@DisplayName("Walking Health Controller Tests")
class WalkingHealthControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("Should return health check successfully")
  void shouldReturnHealthCheckSuccessfully() throws Exception {
    // When & Then
    mockMvc
        .perform(get("/api/v1/walking/health"))
        .andExpect(status().isOk())
        .andExpect(content().string("Walking and energy services are operational"));
  }
}

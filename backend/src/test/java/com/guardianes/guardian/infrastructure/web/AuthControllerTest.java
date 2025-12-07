package com.guardianes.guardian.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guardianes.guardian.application.dto.GuardianAuthResponse;
import com.guardianes.guardian.application.dto.GuardianProfileResponse;
import com.guardianes.guardian.application.service.GuardianApplicationService;
import com.guardianes.guardian.domain.model.GuardianLevel;
import com.guardianes.guardian.infrastructure.web.dto.LoginRequest;
import com.guardianes.guardian.infrastructure.web.dto.RegisterRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Controller tests for AuthController. Tests HTTP request/response handling, validation, and error
 * responses. Addresses Gap #5: Backend has no controller tests for auth endpoints.
 */
@WebMvcTest(
    value = AuthController.class,
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
    username = "testuser",
    roles = {"USER"})
@DisplayName("Auth Controller Tests")
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private GuardianApplicationService guardianApplicationService;

  private GuardianProfileResponse testGuardian;
  private GuardianAuthResponse testAuthResponse;

  @BeforeEach
  void setUp() {
    testGuardian =
        new GuardianProfileResponse(
            1L,
            "testuser",
            "test@example.com",
            "Test User",
            LocalDate.of(2010, 1, 15),
            14,
            GuardianLevel.INITIATE,
            0,
            500,
            0,
            0,
            LocalDateTime.now(),
            LocalDateTime.now(),
            true);
    testAuthResponse = new GuardianAuthResponse("jwt.token.here", testGuardian);
  }

  @Nested
  @DisplayName("POST /api/auth/register")
  class RegisterEndpoint {

    @Test
    @DisplayName("should return 201 CREATED with auth response on successful registration")
    void shouldReturn201OnSuccessfulRegistration() throws Exception {
      RegisterRequest request = new RegisterRequest();
      request.setUsername("testuser");
      request.setEmail("test@example.com");
      request.setPassword("password123");
      request.setName("Test User");
      request.setBirthDate(LocalDate.of(2010, 1, 15));

      when(guardianApplicationService.registerGuardian(any())).thenReturn(testAuthResponse);

      mockMvc
          .perform(
              post("/api/auth/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
                  .with(csrf()))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.token").value("jwt.token.here"))
          .andExpect(jsonPath("$.guardian.username").value("testuser"))
          .andExpect(jsonPath("$.guardian.email").value("test@example.com"));
    }

    @Test
    @DisplayName("should return 409 CONFLICT when username already exists")
    void shouldReturn409WhenUsernameExists() throws Exception {
      RegisterRequest request = new RegisterRequest();
      request.setUsername("existinguser");
      request.setEmail("new@example.com");
      request.setPassword("password123");
      request.setName("New User");
      request.setBirthDate(LocalDate.of(2010, 1, 15));

      when(guardianApplicationService.registerGuardian(any()))
          .thenThrow(new RuntimeException("Guardian with username 'existinguser' already exists"));

      mockMvc
          .perform(
              post("/api/auth/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
                  .with(csrf()))
          .andExpect(status().isConflict())
          .andExpect(
              jsonPath("$.error").value("Guardian with username 'existinguser' already exists"));
    }

    @Test
    @DisplayName("should return 409 CONFLICT when email already exists")
    void shouldReturn409WhenEmailExists() throws Exception {
      RegisterRequest request = new RegisterRequest();
      request.setUsername("newuser");
      request.setEmail("existing@example.com");
      request.setPassword("password123");
      request.setName("New User");
      request.setBirthDate(LocalDate.of(2010, 1, 15));

      when(guardianApplicationService.registerGuardian(any()))
          .thenThrow(
              new RuntimeException("Guardian with email 'existing@example.com' already exists"));

      mockMvc
          .perform(
              post("/api/auth/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
                  .with(csrf()))
          .andExpect(status().isConflict())
          .andExpect(
              jsonPath("$.error")
                  .value("Guardian with email 'existing@example.com' already exists"));
    }

    @Test
    @DisplayName("should return response in correct JSON format")
    void shouldReturnCorrectJsonFormat() throws Exception {
      RegisterRequest request = new RegisterRequest();
      request.setUsername("testuser");
      request.setEmail("test@example.com");
      request.setPassword("password123");
      request.setName("Test User");
      request.setBirthDate(LocalDate.of(2010, 1, 15));

      when(guardianApplicationService.registerGuardian(any())).thenReturn(testAuthResponse);

      mockMvc
          .perform(
              post("/api/auth/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
                  .with(csrf()))
          .andExpect(status().isCreated())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.token").exists())
          .andExpect(jsonPath("$.guardian").exists())
          .andExpect(jsonPath("$.guardian.id").exists())
          .andExpect(jsonPath("$.guardian.username").exists())
          .andExpect(jsonPath("$.guardian.email").exists())
          .andExpect(jsonPath("$.guardian.name").exists());
    }
  }

  @Nested
  @DisplayName("POST /api/auth/login")
  class LoginEndpoint {

    @Test
    @DisplayName("should return 200 OK with auth response on successful login")
    void shouldReturn200OnSuccessfulLogin() throws Exception {
      LoginRequest request = new LoginRequest();
      request.setUsernameOrEmail("testuser");
      request.setPassword("password123");

      when(guardianApplicationService.loginGuardian(any()))
          .thenReturn(Optional.of(testAuthResponse));

      mockMvc
          .perform(
              post("/api/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
                  .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.token").value("jwt.token.here"))
          .andExpect(jsonPath("$.guardian.username").value("testuser"));
    }

    @Test
    @DisplayName("should return 401 UNAUTHORIZED for invalid credentials")
    void shouldReturn401ForInvalidCredentials() throws Exception {
      LoginRequest request = new LoginRequest();
      request.setUsernameOrEmail("testuser");
      request.setPassword("wrongpassword");

      when(guardianApplicationService.loginGuardian(any())).thenReturn(Optional.empty());

      mockMvc
          .perform(
              post("/api/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
                  .with(csrf()))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    @DisplayName("should accept email as login identifier")
    void shouldAcceptEmailAsLoginIdentifier() throws Exception {
      LoginRequest request = new LoginRequest();
      request.setUsernameOrEmail("test@example.com");
      request.setPassword("password123");

      when(guardianApplicationService.loginGuardian(any()))
          .thenReturn(Optional.of(testAuthResponse));

      mockMvc
          .perform(
              post("/api/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
                  .with(csrf()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.token").exists());
    }
  }

  @Nested
  @DisplayName("Response Contract Tests")
  class ResponseContractTests {

    @Test
    @DisplayName("Register response should match mobile app expected format")
    void registerResponseShouldMatchMobileAppExpectedFormat() throws Exception {
      RegisterRequest request = new RegisterRequest();
      request.setUsername("testuser");
      request.setEmail("test@example.com");
      request.setPassword("password123");
      request.setName("Test User");
      request.setBirthDate(LocalDate.of(2010, 1, 15));

      when(guardianApplicationService.registerGuardian(any())).thenReturn(testAuthResponse);

      // Mobile expects: { "token": "...", "guardian": {...} }
      mockMvc
          .perform(
              post("/api/auth/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
                  .with(csrf()))
          .andExpect(status().isCreated())
          // Token field
          .andExpect(jsonPath("$.token").isString())
          // Guardian object with all expected fields
          .andExpect(jsonPath("$.guardian.id").isNumber())
          .andExpect(jsonPath("$.guardian.username").isString())
          .andExpect(jsonPath("$.guardian.email").isString())
          .andExpect(jsonPath("$.guardian.name").isString())
          .andExpect(jsonPath("$.guardian.level").exists())
          .andExpect(jsonPath("$.guardian.experiencePoints").isNumber())
          .andExpect(jsonPath("$.guardian.isChild").isBoolean());
    }

    @Test
    @DisplayName("Login response should match mobile app expected format")
    void loginResponseShouldMatchMobileAppExpectedFormat() throws Exception {
      LoginRequest request = new LoginRequest();
      request.setUsernameOrEmail("testuser");
      request.setPassword("password123");

      when(guardianApplicationService.loginGuardian(any()))
          .thenReturn(Optional.of(testAuthResponse));

      mockMvc
          .perform(
              post("/api/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
                  .with(csrf()))
          .andExpect(status().isOk())
          // Token field
          .andExpect(jsonPath("$.token").isString())
          // Guardian object with all expected fields
          .andExpect(jsonPath("$.guardian.id").isNumber())
          .andExpect(jsonPath("$.guardian.username").isString())
          .andExpect(jsonPath("$.guardian.email").isString())
          .andExpect(jsonPath("$.guardian.name").isString())
          .andExpect(jsonPath("$.guardian.level").exists())
          .andExpect(jsonPath("$.guardian.experiencePoints").isNumber())
          .andExpect(jsonPath("$.guardian.isChild").isBoolean());
    }

    @Test
    @DisplayName("Error response should have consistent structure")
    void errorResponseShouldHaveConsistentStructure() throws Exception {
      LoginRequest request = new LoginRequest();
      request.setUsernameOrEmail("testuser");
      request.setPassword("wrong");

      when(guardianApplicationService.loginGuardian(any())).thenReturn(Optional.empty());

      // Error responses should always have 'error' field
      mockMvc
          .perform(
              post("/api/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
                  .with(csrf()))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.error").exists())
          .andExpect(jsonPath("$.error").isString());
    }
  }
}

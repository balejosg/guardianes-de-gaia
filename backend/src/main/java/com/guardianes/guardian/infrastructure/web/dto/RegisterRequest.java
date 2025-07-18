package com.guardianes.guardian.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class RegisterRequest {

  @NotBlank(message = "Username is required")
  @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
  private String username;

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 6, message = "Password must be at least 6 characters")
  private String password;

  @NotBlank(message = "Name is required")
  @Size(max = 50, message = "Name must not exceed 50 characters")
  private String name;

  @NotNull(message = "Birth date is required")
  private LocalDate birthDate;

  public RegisterRequest() {}

  public RegisterRequest(
      String username, String email, String password, String name, LocalDate birthDate) {
    this.username = username;
    this.email = email;
    this.password = password;
    this.name = name;
    this.birthDate = birthDate;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public LocalDate getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(LocalDate birthDate) {
    this.birthDate = birthDate;
  }
}

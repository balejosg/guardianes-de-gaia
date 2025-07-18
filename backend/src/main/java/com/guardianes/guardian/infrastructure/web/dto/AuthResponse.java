package com.guardianes.guardian.infrastructure.web.dto;

import com.guardianes.guardian.application.dto.GuardianProfileResponse;

public class AuthResponse {

  private String token;
  private GuardianProfileResponse guardian;

  public AuthResponse() {}

  public AuthResponse(String token, GuardianProfileResponse guardian) {
    this.token = token;
    this.guardian = guardian;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public GuardianProfileResponse getGuardian() {
    return guardian;
  }

  public void setGuardian(GuardianProfileResponse guardian) {
    this.guardian = guardian;
  }
}

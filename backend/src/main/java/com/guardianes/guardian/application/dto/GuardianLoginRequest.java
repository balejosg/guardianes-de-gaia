package com.guardianes.guardian.application.dto;

import jakarta.validation.constraints.NotBlank;

public record GuardianLoginRequest(
    @NotBlank(message = "Username or email is required") String usernameOrEmail,
    @NotBlank(message = "Password is required") String password) {}

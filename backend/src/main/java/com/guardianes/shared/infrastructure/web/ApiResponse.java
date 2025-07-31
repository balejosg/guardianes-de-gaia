package com.guardianes.shared.infrastructure.web;

import java.time.LocalDateTime;

public class ApiResponse<T> {
  private boolean success;
  private String message;
  private T data;
  private LocalDateTime timestamp;

  private ApiResponse(boolean success, String message, T data) {
    this.success = success;
    this.message = message;
    this.data = data;
    this.timestamp = LocalDateTime.now();
  }

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(true, "Success", data);
  }

  public static <T> ApiResponse<T> success(T data, String message) {
    return new ApiResponse<>(true, message, data);
  }

  public static <T> ApiResponse<T> error(String message) {
    return new ApiResponse<>(false, message, null);
  }

  // Getters
  public boolean isSuccess() {
    return success;
  }

  public String getMessage() {
    return message;
  }

  public T getData() {
    return data;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }
}

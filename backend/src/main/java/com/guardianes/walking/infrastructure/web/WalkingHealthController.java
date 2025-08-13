package com.guardianes.walking.infrastructure.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/walking/health")
@Tag(name = "Walking Health", description = "Health checks for walking and energy services")
public class WalkingHealthController {

  @GetMapping
  @Operation(
      summary = "Walking service health check",
      description = "Simple health check endpoint for the walking and energy management services.")
  @ApiResponses(
      value = {@ApiResponse(responseCode = "200", description = "Walking services are healthy")})
  public ResponseEntity<String> healthCheck() {
    return ResponseEntity.ok("Walking and energy services are operational");
  }
}

package com.guardianes.guardian.infrastructure.web;

import com.guardianes.guardian.application.dto.GuardianProfileResponse;
import com.guardianes.guardian.application.dto.GuardianRegistrationRequest;
import com.guardianes.guardian.application.service.GuardianApplicationService;
import com.guardianes.guardian.domain.model.Guardian;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/guardians")
@Tag(name = "Guardian Profile", description = "Guardian profile management endpoints")
public class GuardianController {

  private final GuardianApplicationService guardianApplicationService;

  public GuardianController(GuardianApplicationService guardianApplicationService) {
    this.guardianApplicationService = guardianApplicationService;
  }

  @PostMapping
  @Operation(
      summary = "Create Guardian profile",
      description = "Creates a new Guardian profile and returns the created guardian")
  public ResponseEntity<Guardian> createGuardianProfile(
      @Valid @RequestBody GuardianRegistrationRequest request) {
    Guardian guardian = guardianApplicationService.createGuardianProfile(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(guardian);
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get Guardian profile",
      description = "Returns Guardian profile information by ID")
  public ResponseEntity<GuardianProfileResponse> getGuardianProfile(@PathVariable Long id) {
    Optional<GuardianProfileResponse> guardian = guardianApplicationService.getGuardianProfile(id);

    if (guardian.isPresent()) {
      return ResponseEntity.ok(guardian.get());
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }
}

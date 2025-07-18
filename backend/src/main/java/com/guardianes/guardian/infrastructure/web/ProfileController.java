package com.guardianes.guardian.infrastructure.web;

// TODO: Reimplement ProfileController with new service architecture
/*
import com.guardianes.guardian.application.service.GuardianApplicationService;
import com.guardianes.guardian.domain.model.Guardian;
import com.guardianes.guardian.infrastructure.web.dto.ChangePasswordRequest;
import com.guardianes.shared.infrastructure.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "Guardian Profile", description = "Guardian profile management")
public class ProfileController {

    private final GuardianApplicationService guardianApplicationService;
    private final JwtService jwtService;

    public ProfileController(GuardianApplicationService guardianApplicationService, JwtService jwtService) {
        this.guardianApplicationService = guardianApplicationService;
        this.jwtService = jwtService;
    }

    @GetMapping
    @Operation(summary = "Get Guardian profile", description = "Returns the authenticated Guardian's profile information")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            Long guardianId = jwtService.extractGuardianId(authHeader);
            Optional<Guardian> guardian = guardianApplicationService.getGuardianById(guardianId);

            if (guardian.isPresent()) {
                return ResponseEntity.ok(guardian.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Guardian not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Changes the authenticated Guardian's password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String authHeader,
                                          @Valid @RequestBody ChangePasswordRequest request) {
        try {
            Long guardianId = jwtService.extractGuardianId(authHeader);
            boolean success = guardianApplicationService.changePassword(
                guardianId, request.getCurrentPassword(), request.getNewPassword()
            );

            if (success) {
                return ResponseEntity.ok(new SuccessResponse("Password changed successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid current password"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping
    @Operation(summary = "Update profile", description = "Updates the authenticated Guardian's profile")
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String authHeader,
                                         @RequestBody String updateData) {
        try {
            Long guardianId = jwtService.extractGuardianId(authHeader);
            Optional<Guardian> guardian = guardianApplicationService.updateGuardianProfile(guardianId, updateData);

            if (guardian.isPresent()) {
                return ResponseEntity.ok(guardian.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Guardian not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/deactivate")
    @Operation(summary = "Deactivate profile", description = "Deactivates the authenticated Guardian's profile")
    public ResponseEntity<?> deactivateProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            Long guardianId = jwtService.extractGuardianId(authHeader);
            boolean success = guardianApplicationService.deactivateGuardian(guardianId);

            if (success) {
                return ResponseEntity.ok(new SuccessResponse("Profile deactivated successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Guardian not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/stats")
    @Operation(summary = "Get Guardian stats", description = "Returns the authenticated Guardian's stats")
    public ResponseEntity<?> getStats(@RequestHeader("Authorization") String authHeader) {
        try {
            Long guardianId = jwtService.extractGuardianId(authHeader);
            Optional<Guardian> guardian = guardianApplicationService.getGuardianById(guardianId);

            if (guardian.isPresent()) {
                Guardian g = guardian.get();
                GuardianStats stats = new GuardianStats(
                    g.getLevel(),
                    g.getExperiencePoints(),
                    g.getTotalSteps(),
                    g.getTotalEnergyGenerated(),
                    g.getAge()
                );
                return ResponseEntity.ok(stats);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Guardian not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(e.getMessage()));
        }
    }

    public record ErrorResponse(String error) {}
    public record SuccessResponse(String message) {}
    public record GuardianStats(
        com.guardianes.guardian.domain.model.GuardianLevel level,
        int experiencePoints,
        int totalSteps,
        int totalEnergyGenerated,
        int age
    ) {}
}
*/

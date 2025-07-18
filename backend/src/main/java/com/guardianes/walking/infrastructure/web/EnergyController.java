package com.guardianes.walking.infrastructure.web;

import com.guardianes.walking.application.dto.EnergyBalanceResponse;
import com.guardianes.walking.application.dto.EnergySpendingRequest;
import com.guardianes.walking.application.dto.EnergySpendingResponse;
import com.guardianes.walking.application.service.EnergyManagementApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/guardians/{guardianId}/energy")
@Tag(name = "Energy", description = "Energy management for card battles and challenges")
public class EnergyController {

  private final EnergyManagementApplicationService energyService;

  @Autowired
  public EnergyController(EnergyManagementApplicationService energyService) {
    this.energyService = energyService;
  }

  @GetMapping("/balance")
  @Operation(
      summary = "Get energy balance",
      description =
          "Retrieve the current energy balance and recent transaction summary for a guardian.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Energy balance retrieved successfully",
            content = @Content(schema = @Schema(implementation = EnergyBalanceResponse.class))),
        @ApiResponse(responseCode = "404", description = "Guardian not found")
      })
  public ResponseEntity<EnergyBalanceResponse> getEnergyBalance(
      @Parameter(description = "Guardian ID", required = true) @PathVariable Long guardianId) {
    EnergyBalanceResponse response = energyService.getEnergyBalance(guardianId);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/spend")
  @Operation(
      summary = "Spend energy",
      description =
          "Spend energy for battles, challenges, or shop purchases. Validates sufficient balance before spending.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Energy spent successfully",
            content = @Content(schema = @Schema(implementation = EnergySpendingResponse.class))),
        @ApiResponse(responseCode = "400", description = "Insufficient energy or invalid request"),
        @ApiResponse(responseCode = "404", description = "Guardian not found")
      })
  public ResponseEntity<EnergySpendingResponse> spendEnergy(
      @Parameter(description = "Guardian ID", required = true) @PathVariable Long guardianId,
      @Parameter(description = "Energy spending details", required = true) @Valid @RequestBody
          EnergySpendingRequest request) {
    EnergySpendingResponse response = energyService.spendEnergy(guardianId, request);
    return ResponseEntity.ok(response);
  }
}

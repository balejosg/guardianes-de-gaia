package com.guardianes.guardian.infrastructure.web;

import com.guardianes.guardian.application.service.GuardianApplicationService;
import com.guardianes.guardian.application.dto.GuardianAuthResponse;
import com.guardianes.guardian.application.dto.GuardianLoginRequest;
import com.guardianes.guardian.application.dto.GuardianRegistrationRequest;
import com.guardianes.guardian.infrastructure.web.dto.AuthResponse;
import com.guardianes.guardian.infrastructure.web.dto.LoginRequest;
import com.guardianes.guardian.infrastructure.web.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Guardian authentication and registration")
public class AuthController {
    
    private final GuardianApplicationService guardianApplicationService;
    
    public AuthController(GuardianApplicationService guardianApplicationService) {
        this.guardianApplicationService = guardianApplicationService;
    }
    
    @PostMapping("/register")
    @Operation(summary = "Register a new Guardian", description = "Creates a new Guardian account and returns authentication token")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            GuardianRegistrationRequest registrationRequest = new GuardianRegistrationRequest(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getName(),
                request.getBirthDate()
            );
            
            GuardianAuthResponse authResponse = guardianApplicationService.registerGuardian(registrationRequest);
            AuthResponse response = new AuthResponse(authResponse.token(), authResponse.guardian());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    @Operation(summary = "Login Guardian", description = "Authenticates Guardian and returns authentication token")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        GuardianLoginRequest loginRequest = new GuardianLoginRequest(
            request.getUsernameOrEmail(),
            request.getPassword()
        );
        
        Optional<GuardianAuthResponse> authResponse = guardianApplicationService.loginGuardian(loginRequest);
        
        if (authResponse.isPresent()) {
            GuardianAuthResponse response = authResponse.get();
            AuthResponse authResult = new AuthResponse(response.token(), response.guardian());
            return ResponseEntity.ok(authResult);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Invalid credentials"));
        }
    }
    
    public record ErrorResponse(String error) {}
}
package com.guardianes.shared.infrastructure.web;

import com.guardianes.shared.infrastructure.security.JwtTokenProvider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired private AuthenticationManager authenticationManager;

    @Autowired private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    loginRequest.username(), loginRequest.password()));

            String token = jwtTokenProvider.generateToken(authentication);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("type", "Bearer");
            response.put("username", loginRequest.username());
            response.put("message", "Login successful");

            logger.info("User {} logged in successfully", loginRequest.username());
            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            logger.warn("Failed login attempt for user: {}", loginRequest.username());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid credentials");
            errorResponse.put("message", "Username or password is incorrect");

            return ResponseEntity.status(401).body(errorResponse);
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(400)
                        .body(Map.of("error", "Invalid Authorization header format"));
            }

            String token = authHeader.substring(7);
            boolean isValid = jwtTokenProvider.validateToken(token);

            if (isValid) {
                String username = jwtTokenProvider.getUsernameFromToken(token);
                return ResponseEntity.ok(
                        Map.of(
                                "valid",
                                true,
                                "username",
                                username,
                                "expiresAt",
                                jwtTokenProvider.getExpirationDateFromToken(token)));
            } else {
                return ResponseEntity.status(401)
                        .body(Map.of("valid", false, "error", "Token is invalid or expired"));
            }
        } catch (Exception e) {
            logger.error("Error validating token", e);
            return ResponseEntity.status(500).body(Map.of("error", "Token validation failed"));
        }
    }

    public record LoginRequest(
            @NotBlank(message = "Username is required") String username,
            @NotBlank(message = "Password is required") String password) {}
}

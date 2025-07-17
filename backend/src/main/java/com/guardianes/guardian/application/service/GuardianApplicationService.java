package com.guardianes.guardian.application.service;

import com.guardianes.guardian.application.dto.GuardianAuthResponse;
import com.guardianes.guardian.application.dto.GuardianLoginRequest;
import com.guardianes.guardian.application.dto.GuardianProfileResponse;
import com.guardianes.guardian.application.dto.GuardianRegistrationRequest;
import com.guardianes.guardian.domain.model.Guardian;
import com.guardianes.guardian.domain.service.GuardianAuthenticationService;
import com.guardianes.guardian.domain.service.GuardianRegistrationService;
import com.guardianes.shared.infrastructure.security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class GuardianApplicationService {
    
    private final GuardianRegistrationService registrationService;
    private final GuardianAuthenticationService authenticationService;
    private final JwtService jwtService;
    
    public GuardianApplicationService(
            GuardianRegistrationService registrationService,
            GuardianAuthenticationService authenticationService,
            JwtService jwtService) {
        this.registrationService = registrationService;
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
    }
    
    public GuardianAuthResponse registerGuardian(GuardianRegistrationRequest request) {
        Guardian guardian = registrationService.registerGuardian(
            request.username(),
            request.email(),
            request.password(),
            request.name(),
            request.birthDate()
        );
        
        String token = jwtService.generateToken(guardian.getId(), guardian.getUsername());
        GuardianProfileResponse profile = mapToProfileResponse(guardian);
        
        return new GuardianAuthResponse(token, profile);
    }
    
    public Optional<GuardianAuthResponse> loginGuardian(GuardianLoginRequest request) {
        Optional<Guardian> authenticatedGuardian = authenticationService.authenticate(
            request.usernameOrEmail(),
            request.password()
        );
        
        if (authenticatedGuardian.isPresent()) {
            Guardian guardian = authenticatedGuardian.get();
            String token = jwtService.generateToken(guardian.getId(), guardian.getUsername());
            GuardianProfileResponse profile = mapToProfileResponse(guardian);
            
            return Optional.of(new GuardianAuthResponse(token, profile));
        }
        
        return Optional.empty();
    }
    
    private GuardianProfileResponse mapToProfileResponse(Guardian guardian) {
        return new GuardianProfileResponse(
            guardian.getId(),
            guardian.getUsername(),
            guardian.getEmail(),
            guardian.getName(),
            guardian.getBirthDate(),
            guardian.getAge(),
            guardian.getLevel(),
            guardian.getExperiencePoints(),
            guardian.getLevel().getExperienceToNextLevel(guardian.getExperiencePoints()),
            guardian.getTotalSteps(),
            guardian.getTotalEnergyGenerated(),
            guardian.getCreatedAt(),
            guardian.getLastActiveAt(),
            guardian.isChild()
        );
    }
}
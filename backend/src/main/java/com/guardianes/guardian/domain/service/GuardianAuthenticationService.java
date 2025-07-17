package com.guardianes.guardian.domain.service;

import com.guardianes.guardian.domain.exception.InvalidGuardianDataException;
import com.guardianes.guardian.domain.model.Guardian;
import com.guardianes.guardian.domain.repository.GuardianRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GuardianAuthenticationService {
    
    private final GuardianRepository guardianRepository;
    private final PasswordEncoder passwordEncoder;
    
    public GuardianAuthenticationService(GuardianRepository guardianRepository, PasswordEncoder passwordEncoder) {
        this.guardianRepository = guardianRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public Optional<Guardian> authenticate(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || password == null) {
            return Optional.empty();
        }
        
        Optional<Guardian> guardian = findGuardianByUsernameOrEmail(usernameOrEmail);
        
        if (guardian.isPresent() && guardian.get().isActive()) {
            if (passwordEncoder.matches(password, guardian.get().getPasswordHash())) {
                Guardian authenticatedGuardian = guardian.get();
                authenticatedGuardian.updateLastActive();
                guardianRepository.save(authenticatedGuardian);
                return Optional.of(authenticatedGuardian);
            }
        }
        
        return Optional.empty();
    }
    
    private Optional<Guardian> findGuardianByUsernameOrEmail(String usernameOrEmail) {
        if (usernameOrEmail.contains("@")) {
            return guardianRepository.findByEmail(usernameOrEmail);
        } else {
            return guardianRepository.findByUsername(usernameOrEmail);
        }
    }
    
    public boolean changePassword(Long guardianId, String currentPassword, String newPassword) {
        Optional<Guardian> guardianOpt = guardianRepository.findById(guardianId);
        
        if (guardianOpt.isEmpty()) {
            return false;
        }
        
        Guardian guardian = guardianOpt.get();
        
        if (!passwordEncoder.matches(currentPassword, guardian.getPasswordHash())) {
            return false;
        }
        
        if (newPassword == null || newPassword.length() < 6) {
            throw new InvalidGuardianDataException("New password must be at least 6 characters long");
        }
        
        String newHashedPassword = passwordEncoder.encode(newPassword);
        Guardian updatedGuardian = new Guardian(
            guardian.getId(),
            guardian.getUsername(),
            guardian.getEmail(),
            newHashedPassword,
            guardian.getName(),
            guardian.getBirthDate(),
            guardian.getLevel(),
            guardian.getExperiencePoints(),
            guardian.getTotalSteps(),
            guardian.getTotalEnergyGenerated(),
            guardian.getCreatedAt(),
            guardian.getLastActiveAt(),
            guardian.isActive()
        );
        
        guardianRepository.save(updatedGuardian);
        return true;
    }
}
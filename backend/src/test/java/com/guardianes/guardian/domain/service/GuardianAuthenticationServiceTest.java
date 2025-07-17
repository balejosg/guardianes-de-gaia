package com.guardianes.guardian.domain.service;

import com.guardianes.guardian.domain.exception.InvalidGuardianDataException;
import com.guardianes.guardian.domain.model.Guardian;
import com.guardianes.guardian.domain.model.GuardianLevel;
import com.guardianes.guardian.domain.repository.GuardianRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuardianAuthenticationServiceTest {

    @Mock
    private GuardianRepository guardianRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private GuardianAuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new GuardianAuthenticationService(guardianRepository, passwordEncoder);
    }

    @Test
    void shouldAuthenticateWithUsernameSuccessfully() {
        // Given
        String username = "testuser";
        String password = "password123";
        String hashedPassword = "hashedPassword123";
        
        Guardian guardian = createTestGuardian(1L, username, "test@example.com", hashedPassword);
        
        when(guardianRepository.findByUsername(username)).thenReturn(Optional.of(guardian));
        when(passwordEncoder.matches(password, hashedPassword)).thenReturn(true);
        when(guardianRepository.save(any(Guardian.class))).thenReturn(guardian);

        // When
        Optional<Guardian> result = authenticationService.authenticate(username, password);

        // Then
        assertTrue(result.isPresent());
        assertEquals(guardian, result.get());
        
        verify(guardianRepository).findByUsername(username);
        verify(passwordEncoder).matches(password, hashedPassword);
        verify(guardianRepository).save(any(Guardian.class));
    }

    @Test
    void shouldAuthenticateWithEmailSuccessfully() {
        // Given
        String email = "test@example.com";
        String password = "password123";
        String hashedPassword = "hashedPassword123";
        
        Guardian guardian = createTestGuardian(1L, "testuser", email, hashedPassword);
        
        when(guardianRepository.findByEmail(email)).thenReturn(Optional.of(guardian));
        when(passwordEncoder.matches(password, hashedPassword)).thenReturn(true);
        when(guardianRepository.save(any(Guardian.class))).thenReturn(guardian);

        // When
        Optional<Guardian> result = authenticationService.authenticate(email, password);

        // Then
        assertTrue(result.isPresent());
        assertEquals(guardian, result.get());
        
        verify(guardianRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, hashedPassword);
        verify(guardianRepository).save(any(Guardian.class));
    }

    @Test
    void shouldFailAuthenticationWithWrongPassword() {
        // Given
        String username = "testuser";
        String password = "wrongpassword";
        String hashedPassword = "hashedPassword123";
        
        Guardian guardian = createTestGuardian(1L, username, "test@example.com", hashedPassword);
        
        when(guardianRepository.findByUsername(username)).thenReturn(Optional.of(guardian));
        when(passwordEncoder.matches(password, hashedPassword)).thenReturn(false);

        // When
        Optional<Guardian> result = authenticationService.authenticate(username, password);

        // Then
        assertFalse(result.isPresent());
        
        verify(guardianRepository).findByUsername(username);
        verify(passwordEncoder).matches(password, hashedPassword);
        verify(guardianRepository, never()).save(any(Guardian.class));
    }

    @Test
    void shouldFailAuthenticationWithNonExistentUser() {
        // Given
        String username = "nonexistent";
        String password = "password123";
        
        when(guardianRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When
        Optional<Guardian> result = authenticationService.authenticate(username, password);

        // Then
        assertFalse(result.isPresent());
        
        verify(guardianRepository).findByUsername(username);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(guardianRepository, never()).save(any(Guardian.class));
    }

    @Test
    void shouldFailAuthenticationWithInactiveGuardian() {
        // Given
        String username = "testuser";
        String password = "password123";
        String hashedPassword = "hashedPassword123";
        
        Guardian inactiveGuardian = createInactiveTestGuardian(1L, username, "test@example.com", hashedPassword);
        
        when(guardianRepository.findByUsername(username)).thenReturn(Optional.of(inactiveGuardian));

        // When
        Optional<Guardian> result = authenticationService.authenticate(username, password);

        // Then
        assertFalse(result.isPresent());
        
        verify(guardianRepository).findByUsername(username);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(guardianRepository, never()).save(any(Guardian.class));
    }

    @Test
    void shouldFailAuthenticationWithNullCredentials() {
        // When & Then
        assertFalse(authenticationService.authenticate(null, "password").isPresent());
        assertFalse(authenticationService.authenticate("username", null).isPresent());
        assertFalse(authenticationService.authenticate(null, null).isPresent());
        
        verify(guardianRepository, never()).findByUsername(anyString());
        verify(guardianRepository, never()).findByEmail(anyString());
    }

    @Test
    void shouldChangePasswordSuccessfully() {
        // Given
        Long guardianId = 1L;
        String currentPassword = "oldpassword";
        String newPassword = "newpassword123";
        String currentHashedPassword = "hashedOldPassword";
        String newHashedPassword = "hashedNewPassword";
        
        Guardian guardian = createTestGuardian(guardianId, "testuser", "test@example.com", currentHashedPassword);
        
        when(guardianRepository.findById(guardianId)).thenReturn(Optional.of(guardian));
        when(passwordEncoder.matches(currentPassword, currentHashedPassword)).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(newHashedPassword);
        when(guardianRepository.save(any(Guardian.class))).thenReturn(guardian);

        // When
        boolean result = authenticationService.changePassword(guardianId, currentPassword, newPassword);

        // Then
        assertTrue(result);
        
        verify(guardianRepository).findById(guardianId);
        verify(passwordEncoder).matches(currentPassword, currentHashedPassword);
        verify(passwordEncoder).encode(newPassword);
        verify(guardianRepository).save(any(Guardian.class));
    }

    @Test
    void shouldFailChangePasswordWithWrongCurrentPassword() {
        // Given
        Long guardianId = 1L;
        String currentPassword = "wrongpassword";
        String newPassword = "newpassword123";
        String currentHashedPassword = "hashedOldPassword";
        
        Guardian guardian = createTestGuardian(guardianId, "testuser", "test@example.com", currentHashedPassword);
        
        when(guardianRepository.findById(guardianId)).thenReturn(Optional.of(guardian));
        when(passwordEncoder.matches(currentPassword, currentHashedPassword)).thenReturn(false);

        // When
        boolean result = authenticationService.changePassword(guardianId, currentPassword, newPassword);

        // Then
        assertFalse(result);
        
        verify(guardianRepository).findById(guardianId);
        verify(passwordEncoder).matches(currentPassword, currentHashedPassword);
        verify(passwordEncoder, never()).encode(anyString());
        verify(guardianRepository, never()).save(any(Guardian.class));
    }

    @Test
    void shouldFailChangePasswordWithNonExistentGuardian() {
        // Given
        Long guardianId = 999L;
        String currentPassword = "oldpassword";
        String newPassword = "newpassword123";
        
        when(guardianRepository.findById(guardianId)).thenReturn(Optional.empty());

        // When
        boolean result = authenticationService.changePassword(guardianId, currentPassword, newPassword);

        // Then
        assertFalse(result);
        
        verify(guardianRepository).findById(guardianId);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(guardianRepository, never()).save(any(Guardian.class));
    }

    @Test
    void shouldFailChangePasswordWithInvalidNewPassword() {
        // Given
        Long guardianId = 1L;
        String currentPassword = "oldpassword";
        String[] invalidNewPasswords = {null, "", "12345", "short"};
        String currentHashedPassword = "hashedOldPassword";
        
        Guardian guardian = createTestGuardian(guardianId, "testuser", "test@example.com", currentHashedPassword);
        
        when(guardianRepository.findById(guardianId)).thenReturn(Optional.of(guardian));
        when(passwordEncoder.matches(currentPassword, currentHashedPassword)).thenReturn(true);

        for (String invalidNewPassword : invalidNewPasswords) {
            // When & Then
            assertThrows(InvalidGuardianDataException.class,
                    () -> authenticationService.changePassword(guardianId, currentPassword, invalidNewPassword));
        }
    }

    private Guardian createTestGuardian(Long id, String username, String email, String passwordHash) {
        return new Guardian(id, username, email, passwordHash, "Test User", LocalDate.of(2010, 1, 15),
                GuardianLevel.INITIATE, 0, 0, 0, LocalDateTime.now(), LocalDateTime.now(), true);
    }

    private Guardian createInactiveTestGuardian(Long id, String username, String email, String passwordHash) {
        return new Guardian(id, username, email, passwordHash, "Test User", LocalDate.of(2010, 1, 15),
                GuardianLevel.INITIATE, 0, 0, 0, LocalDateTime.now(), LocalDateTime.now(), false);
    }
}
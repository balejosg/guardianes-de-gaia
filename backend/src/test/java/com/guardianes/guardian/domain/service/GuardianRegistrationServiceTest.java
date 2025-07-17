package com.guardianes.guardian.domain.service;

import com.guardianes.guardian.domain.exception.GuardianAlreadyExistsException;
import com.guardianes.guardian.domain.exception.InvalidGuardianDataException;
import com.guardianes.guardian.domain.model.Guardian;
import com.guardianes.guardian.domain.repository.GuardianRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuardianRegistrationServiceTest {

    @Mock
    private GuardianRepository guardianRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private GuardianRegistrationService registrationService;

    @BeforeEach
    void setUp() {
        registrationService = new GuardianRegistrationService(guardianRepository, passwordEncoder);
    }

    @Test
    void shouldRegisterGuardianSuccessfully() {
        // Given
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";
        String name = "Test User";
        LocalDate birthDate = LocalDate.of(2010, 1, 15);
        String hashedPassword = "hashedPassword123";

        when(guardianRepository.existsByUsername(username)).thenReturn(false);
        when(guardianRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(hashedPassword);
        when(guardianRepository.save(any(Guardian.class))).thenAnswer(invocation -> {
            Guardian guardian = invocation.getArgument(0);
            return new Guardian(1L, guardian.getUsername(), guardian.getEmail(), guardian.getPasswordHash(),
                    guardian.getName(), guardian.getBirthDate(), guardian.getLevel(), guardian.getExperiencePoints(),
                    guardian.getTotalSteps(), guardian.getTotalEnergyGenerated(), guardian.getCreatedAt(),
                    guardian.getLastActiveAt(), guardian.isActive());
        });

        // When
        Guardian result = registrationService.registerGuardian(username, email, password, name, birthDate);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(username, result.getUsername());
        assertEquals(email, result.getEmail());
        assertEquals(hashedPassword, result.getPasswordHash());
        assertEquals(name, result.getName());
        assertEquals(birthDate, result.getBirthDate());

        verify(guardianRepository).existsByUsername(username);
        verify(guardianRepository).existsByEmail(email);
        verify(passwordEncoder).encode(password);
        verify(guardianRepository).save(any(Guardian.class));
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        // Given
        String username = "existinguser";
        String email = "test@example.com";
        String password = "password123";
        String name = "Test User";
        LocalDate birthDate = LocalDate.of(2010, 1, 15);

        when(guardianRepository.existsByUsername(username)).thenReturn(true);

        // When & Then
        GuardianAlreadyExistsException exception = assertThrows(GuardianAlreadyExistsException.class,
                () -> registrationService.registerGuardian(username, email, password, name, birthDate));

        assertEquals("Guardian with username 'existinguser' already exists", exception.getMessage());

        verify(guardianRepository).existsByUsername(username);
        verify(guardianRepository, never()).existsByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(guardianRepository, never()).save(any(Guardian.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        String username = "testuser";
        String email = "existing@example.com";
        String password = "password123";
        String name = "Test User";
        LocalDate birthDate = LocalDate.of(2010, 1, 15);

        when(guardianRepository.existsByUsername(username)).thenReturn(false);
        when(guardianRepository.existsByEmail(email)).thenReturn(true);

        // When & Then
        GuardianAlreadyExistsException exception = assertThrows(GuardianAlreadyExistsException.class,
                () -> registrationService.registerGuardian(username, email, password, name, birthDate));

        assertEquals("Guardian with email 'existing@example.com' already exists", exception.getMessage());

        verify(guardianRepository).existsByUsername(username);
        verify(guardianRepository).existsByEmail(email);
        verify(passwordEncoder, never()).encode(anyString());
        verify(guardianRepository, never()).save(any(Guardian.class));
    }

    @Test
    void shouldThrowExceptionForInvalidUsername() {
        // Given
        String[] invalidUsernames = {null, "", " ", "ab", "a".repeat(21), "user@name", "user name", "user-name"};

        for (String invalidUsername : invalidUsernames) {
            // When & Then
            assertThrows(InvalidGuardianDataException.class,
                    () -> registrationService.registerGuardian(invalidUsername, "test@example.com", 
                            "password123", "Test User", LocalDate.of(2010, 1, 15)));
        }
    }

    @Test
    void shouldThrowExceptionForInvalidEmail() {
        // Given
        String[] invalidEmails = {null, "", " ", "invalid", "invalid@", "@example.com", "invalid.com"};

        for (String invalidEmail : invalidEmails) {
            // When & Then
            assertThrows(InvalidGuardianDataException.class,
                    () -> registrationService.registerGuardian("testuser", invalidEmail, 
                            "password123", "Test User", LocalDate.of(2010, 1, 15)));
        }
    }

    @Test
    void shouldThrowExceptionForInvalidPassword() {
        // Given
        String[] invalidPasswords = {null, "", "12345", "short"};

        for (String invalidPassword : invalidPasswords) {
            // When & Then
            assertThrows(InvalidGuardianDataException.class,
                    () -> registrationService.registerGuardian("testuser", "test@example.com", 
                            invalidPassword, "Test User", LocalDate.of(2010, 1, 15)));
        }
    }

    @Test
    void shouldThrowExceptionForInvalidName() {
        // Given
        String[] invalidNames = {null, "", " ", "a".repeat(51)};

        for (String invalidName : invalidNames) {
            // When & Then
            assertThrows(InvalidGuardianDataException.class,
                    () -> registrationService.registerGuardian("testuser", "test@example.com", 
                            "password123", invalidName, LocalDate.of(2010, 1, 15)));
        }
    }

    @Test
    void shouldThrowExceptionForInvalidBirthDate() {
        // Given
        LocalDate[] invalidBirthDates = {
                null,
                LocalDate.now().plusDays(1), // Future date
                LocalDate.now().minusYears(101) // Too old
        };

        for (LocalDate invalidBirthDate : invalidBirthDates) {
            // When & Then
            assertThrows(InvalidGuardianDataException.class,
                    () -> registrationService.registerGuardian("testuser", "test@example.com", 
                            "password123", "Test User", invalidBirthDate));
        }
    }

    @Test
    void shouldAcceptValidUsernames() {
        // Given
        String[] validUsernames = {"abc", "testuser", "user123", "test_user", "a".repeat(20)};
        
        when(guardianRepository.existsByUsername(anyString())).thenReturn(false);
        when(guardianRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(guardianRepository.save(any(Guardian.class))).thenReturn(
                new Guardian(1L, "test", "test@example.com", "hash", "Test", LocalDate.of(2010, 1, 1),
                        com.guardianes.guardian.domain.model.GuardianLevel.INITIATE, 0, 0, 0,
                        java.time.LocalDateTime.now(), java.time.LocalDateTime.now(), true));

        for (String validUsername : validUsernames) {
            // When & Then
            assertDoesNotThrow(() -> registrationService.registerGuardian(validUsername, "test@example.com", 
                    "password123", "Test User", LocalDate.of(2010, 1, 15)));
        }
    }

    @Test
    void shouldAcceptValidEmails() {
        // Given
        String[] validEmails = {"test@example.com", "user.name@domain.co.uk", "123@test.org"};
        
        when(guardianRepository.existsByUsername(anyString())).thenReturn(false);
        when(guardianRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(guardianRepository.save(any(Guardian.class))).thenReturn(
                new Guardian(1L, "test", "test@example.com", "hash", "Test", LocalDate.of(2010, 1, 1),
                        com.guardianes.guardian.domain.model.GuardianLevel.INITIATE, 0, 0, 0,
                        java.time.LocalDateTime.now(), java.time.LocalDateTime.now(), true));

        for (String validEmail : validEmails) {
            // When & Then
            assertDoesNotThrow(() -> registrationService.registerGuardian("testuser", validEmail, 
                    "password123", "Test User", LocalDate.of(2010, 1, 15)));
        }
    }
}
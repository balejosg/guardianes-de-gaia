package com.guardianes.testconfig;

import com.guardianes.guardian.domain.repository.GuardianRepository;
import com.guardianes.guardian.domain.service.GuardianAuthenticationService;
import com.guardianes.guardian.domain.service.GuardianRegistrationService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
public class GuardianTestConfiguration {

    @MockBean
    private GuardianRepository guardianRepository;

    @MockBean
    private GuardianRegistrationService guardianRegistrationService;

    @MockBean
    private GuardianAuthenticationService guardianAuthenticationService;

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
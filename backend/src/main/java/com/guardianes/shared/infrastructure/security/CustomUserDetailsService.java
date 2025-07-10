package com.guardianes.shared.infrastructure.security;

import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Primary
public class CustomUserDetailsService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.admin.username:admin}")
    private String adminUsername;

    @Value("${app.security.admin.password:admin123}")
    private String adminPassword;

    public CustomUserDetailsService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // For MVP, we'll use hardcoded users. In production, this would query a database.

        if (adminUsername.equals(username)) {
            return createUser(username, adminPassword, List.of("ROLE_ADMIN", "ROLE_GUARDIAN"));
        }

        // Default guardian users (for testing)
        if (username.startsWith("guardian")) {
            return createUser(username, "guardian123", List.of("ROLE_GUARDIAN"));
        }

        // Test user for Postman testing
        if ("testuser".equals(username)) {
            return createUser(username, "test123", List.of("ROLE_GUARDIAN"));
        }

        throw new UsernameNotFoundException("User not found with username: " + username);
    }

    private UserDetails createUser(
            String username, String rawPassword, Collection<String> authorities) {
        return User.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .authorities(
                        authorities.stream()
                                .map(SimpleGrantedAuthority::new)
                                .toArray(SimpleGrantedAuthority[]::new))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    // Helper method for creating guardian users with specific IDs
    public UserDetails createGuardianUser(Long guardianId) {
        String username = "guardian" + guardianId;
        return createUser(username, "guardian123", List.of("ROLE_GUARDIAN"));
    }
}

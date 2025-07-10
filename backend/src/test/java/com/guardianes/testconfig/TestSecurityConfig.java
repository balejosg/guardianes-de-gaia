package com.guardianes.testconfig;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@TestConfiguration
public class TestSecurityConfig {

    @Bean("customUserDetailsService")
    @Primary
    public UserDetailsService testUserDetailsService() {
        UserDetails admin =
                User.builder()
                        .username("admin")
                        .password("{noop}admin123")
                        .roles("ADMIN", "GUARDIAN")
                        .build();

        UserDetails guardian =
                User.builder()
                        .username("guardian1")
                        .password("{noop}guardian123")
                        .roles("GUARDIAN")
                        .build();

        return new InMemoryUserDetailsManager(admin, guardian);
    }
}

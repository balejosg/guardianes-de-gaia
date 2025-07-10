package com.guardianes.shared.infrastructure.config;

import com.guardianes.shared.infrastructure.security.JwtAuthenticationEntryPoint;
import com.guardianes.shared.infrastructure.security.JwtAuthenticationFilter;
import com.guardianes.shared.infrastructure.security.RateLimitingFilter;
import com.guardianes.shared.infrastructure.security.SecurityHeadersFilter;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final SecurityHeadersFilter securityHeadersFilter;

    public SecurityConfig(
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RateLimitingFilter rateLimitingFilter,
            SecurityHeadersFilter securityHeadersFilter) {
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.rateLimitingFilter = rateLimitingFilter;
        this.securityHeadersFilter = securityHeadersFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(
                List.of("http://localhost:*", "https://*.guardianes.app"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for stateless API
                .csrf(csrf -> csrf.disable())

                // Configure CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Configure session management
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configure security headers
                .headers(
                        headers ->
                                headers.frameOptions(frame -> frame.deny())
                                        .contentTypeOptions(content -> {})
                                        .httpStrictTransportSecurity(
                                                hstsConfig ->
                                                        hstsConfig
                                                                .maxAgeInSeconds(31536000)
                                                                .includeSubDomains(true)
                                                                .preload(true))
                                        .referrerPolicy(
                                                policy ->
                                                        policy.policy(
                                                                ReferrerPolicyHeaderWriter
                                                                        .ReferrerPolicy
                                                                        .STRICT_ORIGIN_WHEN_CROSS_ORIGIN)))

                // Configure authorization rules
                .authorizeHttpRequests(
                        authz ->
                                authz
                                        // Public endpoints
                                        .requestMatchers("/actuator/health", "/actuator/info")
                                        .permitAll()
                                        .requestMatchers("/api/v1/auth/**")
                                        .permitAll()
                                        .requestMatchers("/api/v1/public/**")
                                        .permitAll()

                                        // Admin endpoints
                                        .requestMatchers("/actuator/**", "/admin/**")
                                        .hasRole("ADMIN")

                                        // Guardian endpoints - require GUARDIAN or ADMIN role
                                        .requestMatchers("/api/v1/guardians/*/steps/**")
                                        .hasAnyRole("GUARDIAN", "ADMIN")
                                        .requestMatchers("/api/v1/guardians/*/energy/**")
                                        .hasAnyRole("GUARDIAN", "ADMIN")

                                        // Battle and card endpoints
                                        .requestMatchers("/api/v1/battles/**")
                                        .hasAnyRole("GUARDIAN", "ADMIN")
                                        .requestMatchers("/api/v1/cards/**")
                                        .hasAnyRole("GUARDIAN", "ADMIN")

                                        // All other requests require authentication
                                        .anyRequest()
                                        .authenticated())

                // Configure exception handling
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))

                // Add custom filters
                .addFilterBefore(securityHeadersFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(
                        jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

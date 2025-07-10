package com.guardianes.testconfig;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.guardianes.shared.infrastructure.security.JwtAuthenticationEntryPoint;
import com.guardianes.shared.infrastructure.security.JwtAuthenticationFilter;
import com.guardianes.shared.infrastructure.security.JwtTokenProvider;
import com.guardianes.shared.infrastructure.security.RateLimitingFilter;
import com.guardianes.shared.infrastructure.security.SecurityHeadersFilter;
import com.guardianes.shared.infrastructure.validation.InputSanitizer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
public class ComprehensiveTestConfiguration {

    @Bean
    @Primary
    public RedisConnectionFactory mockRedisConnectionFactory() {
        RedisConnectionFactory factory = mock(RedisConnectionFactory.class);
        RedisConnection connection = mock(RedisConnection.class);
        when(factory.getConnection()).thenReturn(connection);
        return factory;
    }

    @Bean("redisTemplate")
    @Primary
    @SuppressWarnings("unchecked")
    public RedisTemplate<String, Object> mockRedisTemplate() {
        return mock(RedisTemplate.class);
    }

    @Bean
    @Primary
    public RedisKeyValueAdapter mockRedisKeyValueAdapter() {
        return mock(RedisKeyValueAdapter.class);
    }

    @Bean
    @Primary
    public CacheManager testCacheManager() {
        return new ConcurrentMapCacheManager();
    }

    @Bean
    @Primary
    public JwtTokenProvider mockJwtTokenProvider() {
        JwtTokenProvider provider = mock(JwtTokenProvider.class);
        when(provider.generateToken(any(Authentication.class))).thenReturn("mock-jwt-token");
        when(provider.generateTokenForUser(anyString())).thenReturn("mock-jwt-token");
        when(provider.validateToken(anyString())).thenReturn(true);
        when(provider.getUsernameFromToken(anyString())).thenReturn("admin");
        when(provider.getExpirationDateFromToken(anyString()))
                .thenReturn(new Date(System.currentTimeMillis() + 86400000));
        when(provider.isTokenExpired(anyString())).thenReturn(false);

        return provider;
    }

    @Bean
    @Primary
    public JwtAuthenticationEntryPoint mockJwtAuthenticationEntryPoint() {
        return mock(JwtAuthenticationEntryPoint.class);
    }

    @Bean
    @Primary
    public JwtAuthenticationFilter mockJwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(mockJwtTokenProvider(), mockUserDetailsService()) {
            @Override
            protected void doFilterInternal(
                    @NonNull HttpServletRequest request,
                    @NonNull HttpServletResponse response,
                    @NonNull FilterChain filterChain)
                    throws ServletException, IOException {
                // Skip JWT processing in tests
                filterChain.doFilter(request, response);
            }
        };
    }

    @Bean
    @Primary
    public RateLimitingFilter mockRateLimitingFilter() {
        return new RateLimitingFilter() {
            @Override
            protected void doFilterInternal(
                    @NonNull HttpServletRequest request,
                    @NonNull HttpServletResponse response,
                    @NonNull FilterChain filterChain)
                    throws ServletException, IOException {
                // Skip rate limiting in tests
                filterChain.doFilter(request, response);
            }
        };
    }

    @Bean
    @Primary
    public SecurityHeadersFilter mockSecurityHeadersFilter() {
        return new SecurityHeadersFilter() {
            @Override
            protected void doFilterInternal(
                    @NonNull HttpServletRequest request,
                    @NonNull HttpServletResponse response,
                    @NonNull FilterChain filterChain)
                    throws ServletException, IOException {
                // Add basic security headers for tests
                response.setHeader("X-Frame-Options", "DENY");
                response.setHeader("X-Content-Type-Options", "nosniff");
                response.setHeader(
                        "Strict-Transport-Security", "max-age=31536000; includeSubDomains");
                response.setHeader("X-RateLimit-Limit", "100");
                response.setHeader("X-RateLimit-Remaining", "99");
                response.setHeader("X-RateLimit-Reset", "3600");
                filterChain.doFilter(request, response);
            }
        };
    }

    @Bean
    @Primary
    public InputSanitizer mockInputSanitizer() {
        InputSanitizer sanitizer = mock(InputSanitizer.class);
        when(sanitizer.sanitizeStepSubmission(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(sanitizer.sanitizeGuardianId(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(sanitizer.sanitizeString(anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        return sanitizer;
    }

    @Bean("customUserDetailsService")
    @Primary
    public UserDetailsService mockUserDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username)
                    throws UsernameNotFoundException {
                if ("admin".equals(username)) {
                    return User.builder()
                            .username("admin")
                            .password(passwordEncoder().encode("admin123"))
                            .authorities(Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                            .build();
                } else if ("testuser".equals(username)) {
                    return User.builder()
                            .username("testuser")
                            .password(passwordEncoder().encode("test123"))
                            .authorities(Arrays.asList(new SimpleGrantedAuthority("ROLE_GUARDIAN")))
                            .build();
                }
                throw new UsernameNotFoundException("User not found: " + username);
            }
        };
    }

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public AuthenticationManager mockAuthenticationManager() {
        return mock(AuthenticationManager.class);
    }
}

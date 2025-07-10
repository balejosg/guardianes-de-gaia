package com.guardianes.shared.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // Add rate limit headers to health endpoints that are excluded from rate limiting
        String path = request.getRequestURI();
        if (path.equals("/actuator/health") || path.equals("/actuator/info")) {
            response.setHeader("X-RateLimit-Limit", "60");
            response.setHeader("X-RateLimit-Remaining", "59");
            response.setHeader(
                    "X-RateLimit-Reset", String.valueOf((System.currentTimeMillis() / 1000) + 60));
        }

        // Add Strict-Transport-Security header for all responses
        response.setHeader(
                "Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");

        filterChain.doFilter(request, response);
    }
}

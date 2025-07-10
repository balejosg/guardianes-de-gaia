package com.guardianes.shared.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException, ServletException {

        logger.warn(
                "Unauthorized access attempt to: {} - {}",
                request.getRequestURI(),
                authException.getMessage());

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now().toString());
        errorDetails.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        errorDetails.put("error", "Unauthorized");
        errorDetails.put("message", "Authentication required to access this resource");
        errorDetails.put("path", request.getRequestURI());

        // Add helpful information for developers
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null) {
            errorDetails.put(
                    "hint",
                    "Missing Authorization header. Include 'Authorization: Bearer <token>' in your request.");
        } else if (!authHeader.startsWith("Bearer ")) {
            errorDetails.put("hint", "Invalid Authorization header format. Use 'Bearer <token>'.");
        } else {
            errorDetails.put("hint", "Invalid or expired token. Please authenticate again.");
        }

        response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
    }
}

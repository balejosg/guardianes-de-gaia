package com.guardianes.shared.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.security.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    @Value("${app.security.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    // IP -> (minute -> request count)
    private final Map<String, Map<String, AtomicInteger>> requestCounts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        if (!rateLimitEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIpAddress(request);
        String currentMinute = getCurrentMinuteKey();

        // Clean old entries (older than 2 minutes)
        cleanupOldEntries();

        // Get or create request count for this IP and minute
        Map<String, AtomicInteger> ipRequests =
                requestCounts.computeIfAbsent(clientIp, k -> new ConcurrentHashMap<>());
        AtomicInteger minuteCount =
                ipRequests.computeIfAbsent(currentMinute, k -> new AtomicInteger(0));

        int currentCount = minuteCount.incrementAndGet();

        if (currentCount > requestsPerMinute) {
            logger.warn(
                    "Rate limit exceeded for IP: {} - {} requests in current minute",
                    clientIp,
                    currentCount);
            handleRateLimitExceeded(request, response);
            return;
        }

        // Add rate limit headers for all requests (not just rate-limited ones)
        addRateLimitHeaders(response, currentCount);

        filterChain.doFilter(request, response);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private String getCurrentMinuteKey() {
        return String.valueOf(System.currentTimeMillis() / 60000); // Current minute as key
    }

    private void cleanupOldEntries() {
        long currentMinute = System.currentTimeMillis() / 60000;
        String twoMinutesAgo = String.valueOf(currentMinute - 2);

        requestCounts
                .values()
                .forEach(
                        ipMap ->
                                ipMap.entrySet()
                                        .removeIf(
                                                entry ->
                                                        entry.getKey().compareTo(twoMinutesAgo)
                                                                < 0));

        // Remove empty IP entries
        requestCounts.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    private void addRateLimitHeaders(HttpServletResponse response, int currentCount) {
        response.setHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
        response.setHeader(
                "X-RateLimit-Remaining",
                String.valueOf(Math.max(0, requestsPerMinute - currentCount)));
        response.setHeader(
                "X-RateLimit-Reset", String.valueOf((System.currentTimeMillis() / 1000) + 60));
    }

    private void handleRateLimitExceeded(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setStatus(429); // HTTP 429 Too Many Requests

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now().toString());
        errorDetails.put("status", 429);
        errorDetails.put("error", "Too Many Requests");
        errorDetails.put(
                "message",
                "Rate limit exceeded. Maximum "
                        + requestsPerMinute
                        + " requests per minute allowed.");
        errorDetails.put("path", request.getRequestURI());
        errorDetails.put("retryAfter", 60); // seconds

        response.setHeader("Retry-After", "60");
        response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Don't rate limit health checks and info endpoints
        return path.equals("/actuator/health") || path.equals("/actuator/info");
    }
}

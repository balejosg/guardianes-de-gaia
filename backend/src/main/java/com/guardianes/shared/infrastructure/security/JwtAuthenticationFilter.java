package com.guardianes.shared.infrastructure.security;

import com.guardianes.guardian.domain.model.Guardian;
import com.guardianes.guardian.domain.repository.GuardianRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@ConditionalOnProperty(name = "guardianes.jwt.enabled", havingValue = "true", matchIfMissing = true)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final GuardianRepository guardianRepository;

  public JwtAuthenticationFilter(JwtService jwtService, GuardianRepository guardianRepository) {
    this.jwtService = jwtService;
    this.guardianRepository = guardianRepository;
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    if (isPublicEndpoint(request.getServletPath())) {
      filterChain.doFilter(request, response);
      return;
    }

    final String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      final String jwt = authHeader.substring(7);
      final String username = jwtService.extractUsername(jwt);
      final Long guardianId = jwtService.extractGuardianId(jwt);

      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        Optional<Guardian> guardianOpt = guardianRepository.findByUsername(username);

        if (guardianOpt.isPresent()) {
          Guardian guardian = guardianOpt.get();

          if (guardian.isActive()
              && guardian.getId().equals(guardianId)
              && jwtService.isTokenValid(jwt, username)) {

            UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                    guardian, null, List.of(new SimpleGrantedAuthority("ROLE_GUARDIAN")));
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
          }
        }
      }
    } catch (Exception e) {
      logger.error("Cannot set user authentication", e);
    }

    filterChain.doFilter(request, response);
  }

  private boolean isPublicEndpoint(String path) {
    return path.startsWith("/api/v1/auth/")
        || path.startsWith("/actuator/")
        || path.startsWith("/swagger-ui/")
        || path.startsWith("/v3/api-docs")
        || path.equals("/swagger-ui.html")
        || path.equals("/health")
        || path.equals("/metrics");
  }
}

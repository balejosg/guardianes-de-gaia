package com.guardianes.guardian.domain.repository;

import com.guardianes.guardian.domain.model.Guardian;
import com.guardianes.guardian.domain.model.GuardianLevel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GuardianRepository {
    Guardian save(Guardian guardian);
    Optional<Guardian> findById(Long id);
    Optional<Guardian> findByUsername(String username);
    Optional<Guardian> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsById(Long id);
    void deleteById(Long id);
    void deleteAll();
    List<Guardian> findByActiveTrue();
    List<Guardian> findByActiveFalse();
    List<Guardian> findByLevel(GuardianLevel level);
    List<Guardian> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Guardian> findByLastActiveAtAfter(LocalDateTime cutoff);
}
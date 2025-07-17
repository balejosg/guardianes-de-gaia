package com.guardianes.guardian.infrastructure.persistence.repository;

import com.guardianes.guardian.domain.model.GuardianLevel;
import com.guardianes.guardian.infrastructure.persistence.entity.GuardianEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GuardianJpaRepository extends JpaRepository<GuardianEntity, Long> {
    Optional<GuardianEntity> findByUsername(String username);
    Optional<GuardianEntity> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<GuardianEntity> findByActiveTrue();
    List<GuardianEntity> findByActiveFalse();
    List<GuardianEntity> findByLevel(GuardianLevel level);
    List<GuardianEntity> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<GuardianEntity> findByLastActiveAtAfter(LocalDateTime cutoff);
}
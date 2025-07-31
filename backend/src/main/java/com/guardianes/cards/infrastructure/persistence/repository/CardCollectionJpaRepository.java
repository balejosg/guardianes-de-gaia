package com.guardianes.cards.infrastructure.persistence.repository;

import com.guardianes.cards.infrastructure.persistence.entity.CardCollectionEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardCollectionJpaRepository extends JpaRepository<CardCollectionEntity, Long> {

  Optional<CardCollectionEntity> findByGuardianId(Long guardianId);

  boolean existsByGuardianId(Long guardianId);

  void deleteByGuardianId(Long guardianId);
}

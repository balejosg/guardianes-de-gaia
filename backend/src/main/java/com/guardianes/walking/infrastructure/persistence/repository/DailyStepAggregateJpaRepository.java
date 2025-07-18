package com.guardianes.walking.infrastructure.persistence.repository;

import com.guardianes.walking.infrastructure.persistence.entity.DailyStepAggregateEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DailyStepAggregateJpaRepository
    extends JpaRepository<DailyStepAggregateEntity, Long> {

  Optional<DailyStepAggregateEntity> findByGuardianIdAndDate(Long guardianId, LocalDate date);

  @Query(
      "SELECT d FROM DailyStepAggregateEntity d WHERE d.guardianId = :guardianId AND d.date BETWEEN :fromDate AND :toDate ORDER BY d.date")
  List<DailyStepAggregateEntity> findByGuardianIdAndDateRange(
      @Param("guardianId") Long guardianId,
      @Param("fromDate") LocalDate fromDate,
      @Param("toDate") LocalDate toDate);
}

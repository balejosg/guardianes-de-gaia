package com.guardianes.walking.infrastructure.persistence.repository;

import com.guardianes.walking.infrastructure.persistence.entity.StepRecordEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StepRecordJpaRepository extends JpaRepository<StepRecordEntity, Long> {

  @Query(
      "SELECT s FROM StepRecordEntity s WHERE s.guardianId = :guardianId AND CAST(s.timestamp AS date) = :date")
  List<StepRecordEntity> findByGuardianIdAndDate(
      @Param("guardianId") Long guardianId, @Param("date") LocalDate date);

  @Query(
      "SELECT s FROM StepRecordEntity s WHERE s.guardianId = :guardianId AND CAST(s.timestamp AS date) BETWEEN :fromDate AND :toDate")
  List<StepRecordEntity> findByGuardianIdAndDateRange(
      @Param("guardianId") Long guardianId,
      @Param("fromDate") LocalDate fromDate,
      @Param("toDate") LocalDate toDate);

  @Query(
      "SELECT COUNT(s) FROM StepRecordEntity s WHERE s.guardianId = :guardianId AND s.timestamp > :oneHourAgo AND s.timestamp < :timestamp")
  int countSubmissionsInLastHour(
      @Param("guardianId") Long guardianId,
      @Param("oneHourAgo") LocalDateTime oneHourAgo,
      @Param("timestamp") LocalDateTime timestamp);
}

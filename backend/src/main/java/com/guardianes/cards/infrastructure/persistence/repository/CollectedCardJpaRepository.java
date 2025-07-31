package com.guardianes.cards.infrastructure.persistence.repository;

import com.guardianes.cards.infrastructure.persistence.entity.CardElementEntity;
import com.guardianes.cards.infrastructure.persistence.entity.CardRarityEntity;
import com.guardianes.cards.infrastructure.persistence.entity.CollectedCardEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CollectedCardJpaRepository extends JpaRepository<CollectedCardEntity, Long> {

  Optional<CollectedCardEntity> findByCollectionIdAndCardId(Long collectionId, Long cardId);

  List<CollectedCardEntity> findByCollectionId(Long collectionId);

  List<CollectedCardEntity> findByCardElement(CardElementEntity element);

  List<CollectedCardEntity> findByCardRarity(CardRarityEntity rarity);

  @Query("SELECT cc FROM CollectedCardEntity cc WHERE cc.collection.guardianId = :guardianId")
  List<CollectedCardEntity> findByGuardianId(@Param("guardianId") Long guardianId);

  @Query(
      "SELECT cc FROM CollectedCardEntity cc WHERE cc.collection.guardianId = :guardianId"
          + " AND cc.card.element = :element")
  List<CollectedCardEntity> findByGuardianIdAndCardElement(
      @Param("guardianId") Long guardianId, @Param("element") CardElementEntity element);

  @Query(
      "SELECT cc FROM CollectedCardEntity cc WHERE cc.collection.guardianId = :guardianId"
          + " AND cc.card.rarity = :rarity")
  List<CollectedCardEntity> findByGuardianIdAndCardRarity(
      @Param("guardianId") Long guardianId, @Param("rarity") CardRarityEntity rarity);

  @Query(
      "SELECT cc FROM CollectedCardEntity cc WHERE cc.collection.guardianId = :guardianId"
          + " ORDER BY cc.lastCollectedAt DESC")
  List<CollectedCardEntity> findByGuardianIdOrderByLastCollectedAtDesc(
      @Param("guardianId") Long guardianId);

  @Query(
      "SELECT COUNT(cc) FROM CollectedCardEntity cc WHERE cc.collection.guardianId ="
          + " :guardianId")
  int countByGuardianId(@Param("guardianId") Long guardianId);

  @Query(
      "SELECT SUM(cc.count) FROM CollectedCardEntity cc WHERE cc.collection.guardianId ="
          + " :guardianId")
  int sumCountByGuardianId(@Param("guardianId") Long guardianId);

  @Query("SELECT cc.collection.guardianId FROM CollectedCardEntity cc WHERE cc.card.id = :cardId")
  List<Long> findGuardianIdsByCardId(@Param("cardId") Long cardId);

  void deleteByCollectionId(Long collectionId);
}

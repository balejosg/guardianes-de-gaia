package com.guardianes.cards.infrastructure.persistence.repository;

import com.guardianes.cards.infrastructure.persistence.entity.CardElementEntity;
import com.guardianes.cards.infrastructure.persistence.entity.CardEntity;
import com.guardianes.cards.infrastructure.persistence.entity.CardRarityEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CardJpaRepository extends JpaRepository<CardEntity, Long> {

  Optional<CardEntity> findByQrCode(String qrCode);

  Optional<CardEntity> findByNfcCode(String nfcCode);

  List<CardEntity> findByActiveTrue();

  List<CardEntity> findByElement(CardElementEntity element);

  List<CardEntity> findByRarity(CardRarityEntity rarity);

  List<CardEntity> findByElementAndRarity(CardElementEntity element, CardRarityEntity rarity);

  long countByActiveTrue();

  long countByElement(CardElementEntity element);

  long countByRarity(CardRarityEntity rarity);

  List<CardEntity> findByNameContainingIgnoreCase(String name);

  boolean existsByQrCode(String qrCode);

  boolean existsByNfcCode(String nfcCode);

  @Query("SELECT c FROM CardEntity c WHERE c.active = true AND c.energyCost <= :maxCost")
  List<CardEntity> findSuitableForLevel(@Param("maxCost") int maxCost);

  @Query("SELECT c FROM CardEntity c WHERE c.nfcCode IS NOT NULL AND c.active = true")
  List<CardEntity> findPremiumCards();

  @Query(
      "SELECT c FROM CardEntity c WHERE c.active = true AND"
          + " (c.attackPower + c.defensePower) BETWEEN :minPower AND :maxPower")
  List<CardEntity> findByPowerRange(
      @Param("minPower") int minPower, @Param("maxPower") int maxPower);
}

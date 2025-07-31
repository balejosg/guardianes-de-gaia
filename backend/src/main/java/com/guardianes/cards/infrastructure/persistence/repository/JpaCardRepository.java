package com.guardianes.cards.infrastructure.persistence.repository;

import com.guardianes.cards.domain.model.Card;
import com.guardianes.cards.domain.model.CardElement;
import com.guardianes.cards.domain.model.CardRarity;
import com.guardianes.cards.domain.repository.CardRepository;
import com.guardianes.cards.infrastructure.persistence.entity.CardEntity;
import com.guardianes.cards.infrastructure.persistence.mapper.CardMapper;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class JpaCardRepository implements CardRepository {

  private final CardJpaRepository jpaRepository;
  private final CardMapper mapper;

  public JpaCardRepository(CardJpaRepository jpaRepository, CardMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Card save(Card card) {
    CardEntity entity = mapper.toEntity(card);
    CardEntity savedEntity = jpaRepository.save(entity);
    return mapper.toDomain(savedEntity);
  }

  @Override
  public Optional<Card> findById(Long id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public Optional<Card> findByQrCode(String qrCode) {
    return jpaRepository.findByQrCode(qrCode).map(mapper::toDomain);
  }

  @Override
  public Optional<Card> findByNfcCode(String nfcCode) {
    return jpaRepository.findByNfcCode(nfcCode).map(mapper::toDomain);
  }

  @Override
  public List<Card> findAll() {
    return jpaRepository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
  }

  @Override
  public List<Card> findAllActive() {
    return jpaRepository.findByActiveTrue().stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<Card> findByElement(CardElement element) {
    return jpaRepository.findByElement(mapper.toElementEntity(element)).stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<Card> findByRarity(CardRarity rarity) {
    return jpaRepository.findByRarity(mapper.toRarityEntity(rarity)).stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<Card> findByElementAndRarity(CardElement element, CardRarity rarity) {
    return jpaRepository
        .findByElementAndRarity(mapper.toElementEntity(element), mapper.toRarityEntity(rarity))
        .stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public long countActive() {
    return jpaRepository.countByActiveTrue();
  }

  @Override
  public long countByElement(CardElement element) {
    return jpaRepository.countByElement(mapper.toElementEntity(element));
  }

  @Override
  public long countByRarity(CardRarity rarity) {
    return jpaRepository.countByRarity(mapper.toRarityEntity(rarity));
  }

  @Override
  public List<Card> findByNameContainingIgnoreCase(String name) {
    return jpaRepository.findByNameContainingIgnoreCase(name).stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public boolean existsByQrCode(String qrCode) {
    return jpaRepository.existsByQrCode(qrCode);
  }

  @Override
  public boolean existsByNfcCode(String nfcCode) {
    return jpaRepository.existsByNfcCode(nfcCode);
  }

  @Override
  public void deleteById(Long id) {
    // Soft delete - mark as inactive
    jpaRepository
        .findById(id)
        .ifPresent(
            entity -> {
              entity.setActive(false);
              jpaRepository.save(entity);
            });
  }

  @Override
  public Card update(Card card) {
    return save(card);
  }

  @Override
  public List<Card> findSuitableForLevel(int guardianLevel) {
    // Map guardian level to energy cost (simple mapping for now)
    int maxCost = Math.min(guardianLevel / 2 + 1, 10);
    return jpaRepository.findSuitableForLevel(maxCost).stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<Card> findPremiumCards() {
    return jpaRepository.findPremiumCards().stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<Card> findByPowerRange(int minPower, int maxPower) {
    return jpaRepository.findByPowerRange(minPower, maxPower).stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }
}

package com.guardianes.cards.infrastructure.persistence.repository;

import com.guardianes.cards.domain.model.*;
import com.guardianes.cards.domain.repository.CardCollectionRepository;
import com.guardianes.cards.infrastructure.persistence.entity.*;
import com.guardianes.cards.infrastructure.persistence.mapper.CardCollectionMapper;
import com.guardianes.cards.infrastructure.persistence.mapper.CardMapper;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class JpaCardCollectionRepository implements CardCollectionRepository {

  private final CardCollectionJpaRepository collectionJpaRepository;
  private final CollectedCardJpaRepository collectedCardJpaRepository;
  private final CardJpaRepository cardJpaRepository;
  private final CardCollectionMapper collectionMapper;
  private final CardMapper cardMapper;

  public JpaCardCollectionRepository(
      CardCollectionJpaRepository collectionJpaRepository,
      CollectedCardJpaRepository collectedCardJpaRepository,
      CardJpaRepository cardJpaRepository,
      CardCollectionMapper collectionMapper,
      CardMapper cardMapper) {
    this.collectionJpaRepository = collectionJpaRepository;
    this.collectedCardJpaRepository = collectedCardJpaRepository;
    this.cardJpaRepository = cardJpaRepository;
    this.collectionMapper = collectionMapper;
    this.cardMapper = cardMapper;
  }

  @Override
  public CardCollection save(CardCollection collection) {
    CardCollectionEntity entity = collectionMapper.toEntity(collection);
    CardCollectionEntity savedEntity = collectionJpaRepository.save(entity);

    // Save individual collected cards
    for (CollectedCard collectedCard : collection.getAllCards()) {
      CollectedCardEntity collectedCardEntity =
          collectionMapper.toEntity(collectedCard, savedEntity);
      collectedCardJpaRepository.save(collectedCardEntity);
    }

    return collectionMapper.toDomain(
        collectionJpaRepository.findById(savedEntity.getId()).orElse(null));
  }

  @Override
  public Optional<CardCollection> findByGuardianId(Long guardianId) {
    return collectionJpaRepository.findByGuardianId(guardianId).map(collectionMapper::toDomain);
  }

  @Override
  public CardCollection findOrCreateByGuardianId(Long guardianId) {
    Optional<CardCollectionEntity> existingCollection =
        collectionJpaRepository.findByGuardianId(guardianId);

    if (existingCollection.isPresent()) {
      return collectionMapper.toDomain(existingCollection.get());
    }

    // Create new collection
    CardCollectionEntity newCollection = new CardCollectionEntity(guardianId);
    CardCollectionEntity savedCollection = collectionJpaRepository.save(newCollection);
    return collectionMapper.toDomain(savedCollection);
  }

  @Override
  public CardCollection addCardToCollection(Long guardianId, Long cardId, int count) {
    CardCollectionEntity collection =
        collectionJpaRepository
            .findByGuardianId(guardianId)
            .orElseGet(() -> collectionJpaRepository.save(new CardCollectionEntity(guardianId)));

    Optional<CollectedCardEntity> existingCollectedCard =
        collectedCardJpaRepository.findByCollectionIdAndCardId(collection.getId(), cardId);

    if (existingCollectedCard.isPresent()) {
      // Update existing
      CollectedCardEntity collectedCard = existingCollectedCard.get();
      collectedCard.setCount(collectedCard.getCount() + count);
      collectedCard.setLastCollectedAt(java.time.LocalDateTime.now());
      collectedCardJpaRepository.save(collectedCard);
    } else {
      // Create new
      CardEntity cardEntity =
          cardJpaRepository
              .findById(cardId)
              .orElseThrow(() -> new IllegalArgumentException("Card not found: " + cardId));

      CollectedCardEntity newCollectedCard =
          new CollectedCardEntity(collection, cardEntity, count, java.time.LocalDateTime.now());
      collectedCardJpaRepository.save(newCollectedCard);
    }

    return collectionMapper.toDomain(
        collectionJpaRepository.findById(collection.getId()).orElse(null));
  }

  @Override
  public boolean guardianOwnsCard(Long guardianId, Long cardId) {
    List<CollectedCardEntity> cards = collectedCardJpaRepository.findByGuardianId(guardianId);
    return cards.stream().anyMatch(cc -> cc.getCard().getId().equals(cardId));
  }

  @Override
  public int getCardCount(Long guardianId, Long cardId) {
    List<CollectedCardEntity> cards = collectedCardJpaRepository.findByGuardianId(guardianId);
    return cards.stream()
        .filter(cc -> cc.getCard().getId().equals(cardId))
        .mapToInt(CollectedCardEntity::getCount)
        .sum();
  }

  @Override
  public List<CollectedCard> getGuardianCards(Long guardianId) {
    return collectedCardJpaRepository.findByGuardianId(guardianId).stream()
        .map(collectionMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<CollectedCard> getGuardianCardsByElement(Long guardianId, CardElement element) {
    return collectedCardJpaRepository
        .findByGuardianIdAndCardElement(guardianId, cardMapper.toElementEntity(element))
        .stream()
        .map(collectionMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<CollectedCard> getGuardianCardsByRarity(Long guardianId, CardRarity rarity) {
    return collectedCardJpaRepository
        .findByGuardianIdAndCardRarity(guardianId, cardMapper.toRarityEntity(rarity))
        .stream()
        .map(collectionMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public int getUniqueCardCount(Long guardianId) {
    return collectedCardJpaRepository.countByGuardianId(guardianId);
  }

  @Override
  public int getTotalCardCount(Long guardianId) {
    return collectedCardJpaRepository.sumCountByGuardianId(guardianId);
  }

  @Override
  public double getCompletionPercentage(Long guardianId, int totalAvailableCards) {
    if (totalAvailableCards <= 0) return 0.0;
    int uniqueCards = getUniqueCardCount(guardianId);
    return (double) uniqueCards / totalAvailableCards * 100.0;
  }

  @Override
  public Map<CardRarity, Integer> getCardCountsByRarity(Long guardianId) {
    List<CollectedCardEntity> cards = collectedCardJpaRepository.findByGuardianId(guardianId);
    Map<CardRarity, Integer> counts = new EnumMap<>(CardRarity.class);

    // Initialize all rarities with 0
    for (CardRarity rarity : CardRarity.values()) {
      counts.put(rarity, 0);
    }

    // Count cards by rarity
    for (CollectedCardEntity card : cards) {
      CardRarity rarity = cardMapper.toRarityDomain(card.getCard().getRarity());
      counts.put(rarity, counts.get(rarity) + card.getCount());
    }

    return counts;
  }

  @Override
  public Map<CardElement, Integer> getCardCountsByElement(Long guardianId) {
    List<CollectedCardEntity> cards = collectedCardJpaRepository.findByGuardianId(guardianId);
    Map<CardElement, Integer> counts = new EnumMap<>(CardElement.class);

    // Initialize all elements with 0
    for (CardElement element : CardElement.values()) {
      counts.put(element, 0);
    }

    // Count cards by element
    for (CollectedCardEntity card : cards) {
      CardElement element = cardMapper.toElementDomain(card.getCard().getElement());
      counts.put(element, counts.get(element) + card.getCount());
    }

    return counts;
  }

  @Override
  public List<CollectedCard> getRecentlyCollected(Long guardianId, int limit) {
    return collectedCardJpaRepository
        .findByGuardianIdOrderByLastCollectedAtDesc(guardianId)
        .stream()
        .limit(limit)
        .map(collectionMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public int getTotalTradeValue(Long guardianId) {
    return collectedCardJpaRepository.findByGuardianId(guardianId).stream()
        .mapToInt(
            cc ->
                cardMapper.toRarityDomain(cc.getCard().getRarity()).getTradeValue() * cc.getCount())
        .sum();
  }

  @Override
  public boolean hasElementalBalance(Long guardianId) {
    List<CollectedCardEntity> cards = collectedCardJpaRepository.findByGuardianId(guardianId);
    Set<CardElementEntity> elements =
        cards.stream().map(cc -> cc.getCard().getElement()).collect(Collectors.toSet());

    return elements.size() == CardElementEntity.values().length;
  }

  @Override
  public Optional<CollectedCard> getRarestCard(Long guardianId) {
    return collectedCardJpaRepository.findByGuardianId(guardianId).stream()
        .max(Comparator.comparing(cc -> cc.getCard().getRarity().ordinal()))
        .map(collectionMapper::toDomain);
  }

  @Override
  public CardCollection removeCardsFromCollection(Long guardianId, Long cardId, int count) {
    Optional<CardCollectionEntity> collection =
        collectionJpaRepository.findByGuardianId(guardianId);

    if (collection.isEmpty()) {
      throw new IllegalArgumentException("Collection not found for guardian: " + guardianId);
    }

    Optional<CollectedCardEntity> collectedCard =
        collectedCardJpaRepository.findByCollectionIdAndCardId(collection.get().getId(), cardId);

    if (collectedCard.isEmpty()) {
      throw new IllegalArgumentException("Card not found in collection: " + cardId);
    }

    CollectedCardEntity cardEntity = collectedCard.get();
    int currentCount = cardEntity.getCount();

    if (count >= currentCount) {
      // Remove entirely
      collectedCardJpaRepository.delete(cardEntity);
    } else {
      // Reduce count
      cardEntity.setCount(currentCount - count);
      collectedCardJpaRepository.save(cardEntity);
    }

    return collectionMapper.toDomain(collection.get());
  }

  @Override
  public CardCollection update(CardCollection collection) {
    return save(collection);
  }

  @Override
  public void deleteByGuardianId(Long guardianId) {
    collectionJpaRepository
        .findByGuardianId(guardianId)
        .ifPresent(
            collection -> {
              collectedCardJpaRepository.deleteByCollectionId(collection.getId());
              collectionJpaRepository.delete(collection);
            });
  }

  @Override
  public List<CardCollection> findAll() {
    return collectionJpaRepository.findAll().stream()
        .map(collectionMapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<Long> findGuardiansOwningCard(Long cardId) {
    return collectedCardJpaRepository.findGuardianIdsByCardId(cardId);
  }

  @Override
  public Map<Long, Integer> getMostPopularCards(int limit) {
    // This would require a complex query - simplified version
    List<CollectedCardEntity> allCards = collectedCardJpaRepository.findAll();
    return allCards.stream()
        .collect(Collectors.groupingBy(cc -> cc.getCard().getId(), Collectors.summingInt(cc -> 1)))
        .entrySet()
        .stream()
        .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
        .limit(limit)
        .collect(
            Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
  }

  @Override
  public Map<Long, Integer> getCollectionSizeStatistics() {
    List<CardCollectionEntity> allCollections = collectionJpaRepository.findAll();
    return allCollections.stream()
        .collect(
            Collectors.toMap(
                CardCollectionEntity::getGuardianId,
                collection -> collection.getCollectedCards().size()));
  }
}

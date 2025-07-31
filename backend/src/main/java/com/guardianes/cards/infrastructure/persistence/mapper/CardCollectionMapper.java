package com.guardianes.cards.infrastructure.persistence.mapper;

import com.guardianes.cards.domain.model.Card;
import com.guardianes.cards.domain.model.CardCollection;
import com.guardianes.cards.domain.model.CollectedCard;
import com.guardianes.cards.infrastructure.persistence.entity.CardCollectionEntity;
import com.guardianes.cards.infrastructure.persistence.entity.CollectedCardEntity;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CardCollectionMapper {

  private final CardMapper cardMapper;

  public CardCollectionMapper(CardMapper cardMapper) {
    this.cardMapper = cardMapper;
  }

  public CardCollection toDomain(CardCollectionEntity entity) {
    if (entity == null) {
      return null;
    }

    Map<Long, CollectedCard> cards = new HashMap<>();

    for (CollectedCardEntity collectedCardEntity : entity.getCollectedCards()) {
      Card card = cardMapper.toDomain(collectedCardEntity.getCard());
      CollectedCard collectedCard =
          new CollectedCard(
              card,
              collectedCardEntity.getCount(),
              collectedCardEntity.getFirstCollectedAt(),
              collectedCardEntity.getLastCollectedAt());

      cards.put(card.getId(), collectedCard);
    }

    return new CardCollection(entity.getGuardianId(), cards, entity.getCreatedAt());
  }

  public CardCollectionEntity toEntity(CardCollection domain) {
    if (domain == null) {
      return null;
    }

    CardCollectionEntity entity = new CardCollectionEntity();
    entity.setGuardianId(domain.getGuardianId());
    entity.setCreatedAt(domain.getCreatedAt());

    return entity;
  }

  public CollectedCard toDomain(CollectedCardEntity entity) {
    if (entity == null) {
      return null;
    }

    Card card = cardMapper.toDomain(entity.getCard());
    return new CollectedCard(
        card, entity.getCount(), entity.getFirstCollectedAt(), entity.getLastCollectedAt());
  }

  public CollectedCardEntity toEntity(CollectedCard domain, CardCollectionEntity collection) {
    if (domain == null) {
      return null;
    }

    CollectedCardEntity entity = new CollectedCardEntity();
    entity.setCollection(collection);
    entity.setCard(cardMapper.toEntity(domain.getCard()));
    entity.setCount(domain.getCount());
    entity.setFirstCollectedAt(domain.getFirstCollectedAt());
    entity.setLastCollectedAt(domain.getLastCollectedAt());

    return entity;
  }
}

package com.guardianes.cards.infrastructure.persistence.mapper;

import com.guardianes.cards.domain.model.Card;
import com.guardianes.cards.domain.model.CardElement;
import com.guardianes.cards.domain.model.CardRarity;
import com.guardianes.cards.infrastructure.persistence.entity.CardElementEntity;
import com.guardianes.cards.infrastructure.persistence.entity.CardEntity;
import com.guardianes.cards.infrastructure.persistence.entity.CardRarityEntity;
import org.springframework.stereotype.Component;

@Component
public class CardMapper {

  public Card toDomain(CardEntity entity) {
    if (entity == null) {
      return null;
    }

    return new Card(
        entity.getId(),
        entity.getName(),
        entity.getDescription(),
        mapElementToDomain(entity.getElement()),
        mapRarityToDomain(entity.getRarity()),
        entity.getAttackPower(),
        entity.getDefensePower(),
        entity.getEnergyCost(),
        entity.getImageUrl(),
        entity.getQrCode(),
        entity.getNfcCode(),
        entity.getCreatedAt(),
        entity.getActive());
  }

  public CardEntity toEntity(Card domain) {
    if (domain == null) {
      return null;
    }

    CardEntity entity = new CardEntity();
    entity.setId(domain.getId());
    entity.setName(domain.getName());
    entity.setDescription(domain.getDescription());
    entity.setElement(mapElementToEntity(domain.getElement()));
    entity.setRarity(mapRarityToEntity(domain.getRarity()));
    entity.setAttackPower(domain.getAttackPower());
    entity.setDefensePower(domain.getDefensePower());
    entity.setEnergyCost(domain.getEnergyCost());
    entity.setImageUrl(domain.getImageUrl());
    entity.setQrCode(domain.getQrCode());
    entity.setNfcCode(domain.getNfcCode());
    entity.setCreatedAt(domain.getCreatedAt());
    entity.setActive(domain.isActive());

    return entity;
  }

  private CardElement mapElementToDomain(CardElementEntity entityElement) {
    if (entityElement == null) {
      return null;
    }

    return switch (entityElement) {
      case FIRE -> CardElement.FIRE;
      case EARTH -> CardElement.EARTH;
      case WATER -> CardElement.WATER;
      case AIR -> CardElement.AIR;
    };
  }

  private CardElementEntity mapElementToEntity(CardElement domainElement) {
    if (domainElement == null) {
      return null;
    }

    return switch (domainElement) {
      case FIRE -> CardElementEntity.FIRE;
      case EARTH -> CardElementEntity.EARTH;
      case WATER -> CardElementEntity.WATER;
      case AIR -> CardElementEntity.AIR;
    };
  }

  private CardRarity mapRarityToDomain(CardRarityEntity entityRarity) {
    if (entityRarity == null) {
      return null;
    }

    return switch (entityRarity) {
      case COMMON -> CardRarity.COMMON;
      case UNCOMMON -> CardRarity.UNCOMMON;
      case RARE -> CardRarity.RARE;
      case EPIC -> CardRarity.EPIC;
      case LEGENDARY -> CardRarity.LEGENDARY;
    };
  }

  private CardRarityEntity mapRarityToEntity(CardRarity domainRarity) {
    if (domainRarity == null) {
      return null;
    }

    return switch (domainRarity) {
      case COMMON -> CardRarityEntity.COMMON;
      case UNCOMMON -> CardRarityEntity.UNCOMMON;
      case RARE -> CardRarityEntity.RARE;
      case EPIC -> CardRarityEntity.EPIC;
      case LEGENDARY -> CardRarityEntity.LEGENDARY;
    };
  }

  // Public mapping methods for repository use
  public CardElementEntity toElementEntity(CardElement element) {
    return mapElementToEntity(element);
  }

  public CardRarityEntity toRarityEntity(CardRarity rarity) {
    return mapRarityToEntity(rarity);
  }

  public CardElement toElementDomain(CardElementEntity entityElement) {
    return mapElementToDomain(entityElement);
  }

  public CardRarity toRarityDomain(CardRarityEntity entityRarity) {
    return mapRarityToDomain(entityRarity);
  }
}

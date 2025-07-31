package com.guardianes.cards.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "collected_cards")
public class CollectedCardEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "collection_id", nullable = false)
  private CardCollectionEntity collection;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "card_id", nullable = false)
  private CardEntity card;

  @Column(nullable = false)
  private Integer count = 1;

  @Column(nullable = false)
  private LocalDateTime firstCollectedAt;

  @Column(nullable = false)
  private LocalDateTime lastCollectedAt;

  public CollectedCardEntity() {
    LocalDateTime now = LocalDateTime.now();
    this.firstCollectedAt = now;
    this.lastCollectedAt = now;
  }

  public CollectedCardEntity(
      CardCollectionEntity collection,
      CardEntity card,
      Integer count,
      LocalDateTime collectionTime) {
    this.collection = collection;
    this.card = card;
    this.count = count;
    this.firstCollectedAt = collectionTime;
    this.lastCollectedAt = collectionTime;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public CardCollectionEntity getCollection() {
    return collection;
  }

  public void setCollection(CardCollectionEntity collection) {
    this.collection = collection;
  }

  public CardEntity getCard() {
    return card;
  }

  public void setCard(CardEntity card) {
    this.card = card;
  }

  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public LocalDateTime getFirstCollectedAt() {
    return firstCollectedAt;
  }

  public void setFirstCollectedAt(LocalDateTime firstCollectedAt) {
    this.firstCollectedAt = firstCollectedAt;
  }

  public LocalDateTime getLastCollectedAt() {
    return lastCollectedAt;
  }

  public void setLastCollectedAt(LocalDateTime lastCollectedAt) {
    this.lastCollectedAt = lastCollectedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CollectedCardEntity that = (CollectedCardEntity) o;
    return Objects.equals(id, that.id)
        && Objects.equals(collection.getId(), that.collection.getId())
        && Objects.equals(card.getId(), that.card.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, collection.getId(), card.getId());
  }

  @Override
  public String toString() {
    return "CollectedCardEntity{"
        + "id="
        + id
        + ", cardId="
        + (card != null ? card.getId() : null)
        + ", count="
        + count
        + ", firstCollectedAt="
        + firstCollectedAt
        + ", lastCollectedAt="
        + lastCollectedAt
        + '}';
  }
}

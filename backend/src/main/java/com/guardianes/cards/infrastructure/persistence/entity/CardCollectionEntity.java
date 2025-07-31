package com.guardianes.cards.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "card_collections")
public class CardCollectionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private Long guardianId;

  @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<CollectedCardEntity> collectedCards = new ArrayList<>();

  @Column(nullable = false)
  private LocalDateTime createdAt;

  public CardCollectionEntity() {
    this.createdAt = LocalDateTime.now();
  }

  public CardCollectionEntity(Long guardianId) {
    this();
    this.guardianId = guardianId;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getGuardianId() {
    return guardianId;
  }

  public void setGuardianId(Long guardianId) {
    this.guardianId = guardianId;
  }

  public List<CollectedCardEntity> getCollectedCards() {
    return collectedCards;
  }

  public void setCollectedCards(List<CollectedCardEntity> collectedCards) {
    this.collectedCards = collectedCards;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CardCollectionEntity that = (CardCollectionEntity) o;
    return Objects.equals(id, that.id) && Objects.equals(guardianId, that.guardianId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, guardianId);
  }

  @Override
  public String toString() {
    return "CardCollectionEntity{"
        + "id="
        + id
        + ", guardianId="
        + guardianId
        + ", cardCount="
        + collectedCards.size()
        + ", createdAt="
        + createdAt
        + '}';
  }
}

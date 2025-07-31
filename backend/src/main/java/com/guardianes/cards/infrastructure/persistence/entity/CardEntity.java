package com.guardianes.cards.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "cards")
public class CardEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 50)
  private String name;

  @Column(nullable = false, length = 500)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private CardElementEntity element;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private CardRarityEntity rarity;

  @Column(nullable = false)
  private Integer attackPower;

  @Column(nullable = false)
  private Integer defensePower;

  @Column(nullable = false)
  private Integer energyCost;

  @Column(length = 500)
  private String imageUrl;

  @Column(unique = true, nullable = false, length = 16)
  private String qrCode;

  @Column(unique = true, length = 24)
  private String nfcCode;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private Boolean active = true;

  public CardEntity() {
    this.createdAt = LocalDateTime.now();
  }

  public CardEntity(
      String name,
      String description,
      CardElementEntity element,
      CardRarityEntity rarity,
      Integer attackPower,
      Integer defensePower,
      Integer energyCost,
      String imageUrl,
      String qrCode,
      String nfcCode) {
    this();
    this.name = name;
    this.description = description;
    this.element = element;
    this.rarity = rarity;
    this.attackPower = attackPower;
    this.defensePower = defensePower;
    this.energyCost = energyCost;
    this.imageUrl = imageUrl;
    this.qrCode = qrCode;
    this.nfcCode = nfcCode;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public CardElementEntity getElement() {
    return element;
  }

  public void setElement(CardElementEntity element) {
    this.element = element;
  }

  public CardRarityEntity getRarity() {
    return rarity;
  }

  public void setRarity(CardRarityEntity rarity) {
    this.rarity = rarity;
  }

  public Integer getAttackPower() {
    return attackPower;
  }

  public void setAttackPower(Integer attackPower) {
    this.attackPower = attackPower;
  }

  public Integer getDefensePower() {
    return defensePower;
  }

  public void setDefensePower(Integer defensePower) {
    this.defensePower = defensePower;
  }

  public Integer getEnergyCost() {
    return energyCost;
  }

  public void setEnergyCost(Integer energyCost) {
    this.energyCost = energyCost;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public String getQrCode() {
    return qrCode;
  }

  public void setQrCode(String qrCode) {
    this.qrCode = qrCode;
  }

  public String getNfcCode() {
    return nfcCode;
  }

  public void setNfcCode(String nfcCode) {
    this.nfcCode = nfcCode;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CardEntity that = (CardEntity) o;
    return Objects.equals(id, that.id) && Objects.equals(qrCode, that.qrCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, qrCode);
  }

  @Override
  public String toString() {
    return "CardEntity{"
        + "id="
        + id
        + ", name='"
        + name
        + '\''
        + ", element="
        + element
        + ", rarity="
        + rarity
        + ", attackPower="
        + attackPower
        + ", defensePower="
        + defensePower
        + ", energyCost="
        + energyCost
        + ", qrCode='"
        + qrCode
        + '\''
        + ", active="
        + active
        + '}';
  }
}

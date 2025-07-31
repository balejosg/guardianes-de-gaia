package com.guardianes.cards.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a collectible card in the Guardianes de Gaia game. Cards can be collected by scanning
 * QR codes or NFC tags.
 */
public class Card {
  private final Long id;
  private final String name;
  private final String description;
  private final CardElement element;
  private final CardRarity rarity;
  private final Integer attackPower;
  private final Integer defensePower;
  private final Integer energyCost;
  private final String imageUrl;
  private final String qrCode;
  private final String nfcCode;
  private final LocalDateTime createdAt;
  private final boolean isActive;

  public Card(
      Long id,
      String name,
      String description,
      CardElement element,
      CardRarity rarity,
      Integer attackPower,
      Integer defensePower,
      Integer energyCost,
      String imageUrl,
      String qrCode,
      String nfcCode,
      LocalDateTime createdAt,
      boolean isActive) {
    this.id = id;
    this.name = validateName(name);
    this.description = validateDescription(description);
    this.element = Objects.requireNonNull(element, "Element cannot be null");
    this.rarity = Objects.requireNonNull(rarity, "Rarity cannot be null");
    this.attackPower = validateAttackPower(attackPower);
    this.defensePower = validateDefensePower(defensePower);
    this.energyCost = validateEnergyCost(energyCost);
    this.imageUrl = imageUrl;
    this.qrCode = validateQrCode(qrCode);
    this.nfcCode = nfcCode; // NFC code is optional
    this.createdAt = Objects.requireNonNull(createdAt, "Created at cannot be null");
    this.isActive = isActive;
  }

  public static Card create(
      String name,
      String description,
      CardElement element,
      CardRarity rarity,
      Integer attackPower,
      Integer defensePower,
      Integer energyCost,
      String imageUrl,
      String qrCode,
      String nfcCode) {
    return new Card(
        null,
        name,
        description,
        element,
        rarity,
        attackPower,
        defensePower,
        energyCost,
        imageUrl,
        qrCode,
        nfcCode,
        LocalDateTime.now(),
        true);
  }

  // Validation methods
  private String validateName(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Card name cannot be null or empty");
    }
    if (name.length() > 50) {
      throw new IllegalArgumentException("Card name cannot exceed 50 characters");
    }
    return name.trim();
  }

  private String validateDescription(String description) {
    if (description == null || description.trim().isEmpty()) {
      throw new IllegalArgumentException("Card description cannot be null or empty");
    }
    if (description.length() > 500) {
      throw new IllegalArgumentException("Card description cannot exceed 500 characters");
    }
    return description.trim();
  }

  private Integer validateAttackPower(Integer attackPower) {
    if (attackPower == null || attackPower < 0 || attackPower > 999) {
      throw new IllegalArgumentException("Attack power must be between 0 and 999");
    }
    return attackPower;
  }

  private Integer validateDefensePower(Integer defensePower) {
    if (defensePower == null || defensePower < 0 || defensePower > 999) {
      throw new IllegalArgumentException("Defense power must be between 0 and 999");
    }
    return defensePower;
  }

  private Integer validateEnergyCost(Integer energyCost) {
    if (energyCost == null || energyCost < 0 || energyCost > 10) {
      throw new IllegalArgumentException("Energy cost must be between 0 and 10");
    }
    return energyCost;
  }

  private String validateQrCode(String qrCode) {
    if (qrCode == null || qrCode.trim().isEmpty()) {
      throw new IllegalArgumentException("QR code cannot be null or empty");
    }
    if (!qrCode.matches("^[A-Z0-9]{16}$")) {
      throw new IllegalArgumentException("QR code must be 16 alphanumeric characters");
    }
    return qrCode;
  }

  // Business logic methods
  public boolean canBePlayedWith(int availableEnergy) {
    return availableEnergy >= this.energyCost;
  }

  public boolean isElementallyAdvantageousAgainst(Card opponent) {
    return this.element.hasAdvantageOver(opponent.element);
  }

  public int calculateDamageAgainst(Card opponent) {
    int baseDamage = this.attackPower - opponent.defensePower;
    baseDamage = Math.max(baseDamage, 1); // Minimum 1 damage

    if (this.isElementallyAdvantageousAgainst(opponent)) {
      baseDamage = (int) (baseDamage * 1.5); // 50% bonus damage
    }

    return baseDamage;
  }

  public int getTotalPower() {
    return this.attackPower + this.defensePower;
  }

  public boolean isPremium() {
    return this.nfcCode != null && !this.nfcCode.trim().isEmpty();
  }

  // Getters
  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public CardElement getElement() {
    return element;
  }

  public CardRarity getRarity() {
    return rarity;
  }

  public Integer getAttackPower() {
    return attackPower;
  }

  public Integer getDefensePower() {
    return defensePower;
  }

  public Integer getEnergyCost() {
    return energyCost;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public String getQrCode() {
    return qrCode;
  }

  public String getNfcCode() {
    return nfcCode;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public boolean isActive() {
    return isActive;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Card card = (Card) o;
    return Objects.equals(id, card.id) && Objects.equals(qrCode, card.qrCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, qrCode);
  }

  @Override
  public String toString() {
    return "Card{"
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
        + ", isPremium="
        + isPremium()
        + '}';
  }
}

package com.guardianes.battle.domain.service;

import com.guardianes.battle.domain.model.Battle;
import com.guardianes.battle.domain.model.BattleMove;
import com.guardianes.battle.domain.model.BattleResult;
import com.guardianes.battle.domain.model.BattleType;
import com.guardianes.cards.domain.model.Card;
import com.guardianes.guardian.domain.model.Guardian;
import com.guardianes.walking.domain.EnergyCalculationService;
import java.util.List;
import java.util.Objects;

/**
 * Core battle engine that handles battle mechanics, move resolution, and result calculation. This
 * service encapsulates all the business rules for how battles work in Guardianes de Gaia.
 */
public class BattleEngine {
  private final EnergyCalculationService energyService;

  public BattleEngine(EnergyCalculationService energyService) {
    this.energyService = Objects.requireNonNull(energyService, "Energy service cannot be null");
  }

  /**
   * Executes a battle move and updates the battle state with the results.
   *
   * @param battle The current battle state
   * @param move The move to execute
   * @param card The card being played
   * @param challenger The Guardian making the move
   * @param target The target Guardian (can be null for non-targeted moves)
   * @return Updated battle with the move executed
   */
  public Battle executeMove(
      Battle battle, BattleMove move, Card card, Guardian challenger, Guardian target) {
    Objects.requireNonNull(battle, "Battle cannot be null");
    Objects.requireNonNull(move, "Battle move cannot be null");
    Objects.requireNonNull(card, "Card cannot be null");
    Objects.requireNonNull(challenger, "Challenger cannot be null");

    if (!battle.isActive()) {
      throw new IllegalStateException("Cannot execute moves on inactive battle");
    }

    if (!battle.isParticipant(challenger.getId())) {
      throw new IllegalArgumentException("Guardian is not a participant in this battle");
    }

    int availableEnergy = energyService.getCurrentEnergyBalance(challenger.getId());
    if (!card.canBePlayedWith(availableEnergy)) {
      throw new InsufficientEnergyException(
          "Guardian does not have enough energy to play this card");
    }

    // Validate move targets
    validateMoveTarget(battle, move, target);

    // Create detailed move description with effects
    String moveDescription = buildMoveDescription(card, move, challenger, target);

    // Calculate damage and healing based on card and move type
    BattleMoveEffects effects = calculateMoveEffects(card, move, challenger, target);

    // Create the executed move with calculated effects
    BattleMove executedMove =
        new BattleMove(
            null,
            battle.getId(),
            challenger.getId(),
            card.getId(),
            target != null ? target.getId() : null,
            move.getMoveType(),
            card.getEnergyCost(),
            effects.damage(),
            effects.healing(),
            moveDescription,
            move.getExecutedAt());

    // Add the move to the battle
    Battle updatedBattle = battle.addMove(executedMove);

    // Check if battle should end after this move
    return checkBattleCompletion(updatedBattle, challenger, target);
  }

  /**
   * Resolves a complete battle and determines the final result.
   *
   * @param battle The battle to resolve
   * @param participants List of all battle participants
   * @return The battle result with winner, XP rewards, and statistics
   */
  public BattleResult resolveBattle(Battle battle, List<Guardian> participants) {
    Objects.requireNonNull(battle, "Battle cannot be null");
    Objects.requireNonNull(participants, "Participants cannot be null");

    if (!battle.isCompleted()) {
      throw new IllegalStateException("Cannot resolve incomplete battle");
    }

    // Calculate battle statistics
    BattleStatistics stats = calculateBattleStatistics(battle, participants);

    // Determine winner based on battle type and statistics
    Guardian winner = determineWinner(battle, participants, stats);

    // Calculate XP rewards for all participants
    List<XpReward> xpRewards = calculateXpRewards(battle, participants, winner, stats);

    return new BattleResult(
        battle.getId(),
        winner != null ? winner.getId() : null,
        stats.totalMoves(),
        stats.totalEnergySpent(),
        battle.getStartedAt(),
        battle.getCompletedAt(),
        xpRewards);
  }

  /**
   * Checks if a Guardian can start a battle with their current energy level.
   *
   * @param guardian The Guardian attempting to start a battle
   * @param battleType The type of battle
   * @return true if the Guardian has sufficient energy
   */
  public boolean canStartBattle(Guardian guardian, BattleType battleType) {
    Objects.requireNonNull(guardian, "Guardian cannot be null");
    Objects.requireNonNull(battleType, "Battle type cannot be null");

    int availableEnergy = energyService.getCurrentEnergyBalance(guardian.getId());
    return availableEnergy >= battleType.getMinimumEnergyCost();
  }

  /**
   * Calculates the maximum number of moves a Guardian can make with their current energy.
   *
   * @param guardian The Guardian
   * @param availableCards The cards available to play
   * @return Maximum possible moves
   */
  public int calculateMaxPossibleMoves(Guardian guardian, List<Card> availableCards) {
    Objects.requireNonNull(guardian, "Guardian cannot be null");
    Objects.requireNonNull(availableCards, "Available cards cannot be null");

    int availableEnergy = energyService.getCurrentEnergyBalance(guardian.getId());
    int moves = 0;

    // Find the card with lowest energy cost that can be played
    Card cheapestCard =
        availableCards.stream()
            .filter(card -> card.getEnergyCost() <= availableEnergy)
            .min((c1, c2) -> Integer.compare(c1.getEnergyCost(), c2.getEnergyCost()))
            .orElse(null);

    if (cheapestCard != null) {
      moves = availableEnergy / cheapestCard.getEnergyCost();
    }

    return moves;
  }

  // Private helper methods

  private void validateMoveTarget(Battle battle, BattleMove move, Guardian target) {
    if (move.getMoveType().canTarget() && move.getTargetGuardianId() != null) {
      if (target == null) {
        throw new IllegalArgumentException("Target Guardian is required for this move type");
      }
      if (battle.isSoloBattle()) {
        return; // Solo battles can target AI opponents
      }
      if (!battle.isParticipant(target.getId())) {
        throw new IllegalArgumentException("Target must be a battle participant");
      }
    }
  }

  private String buildMoveDescription(
      Card card, BattleMove move, Guardian challenger, Guardian target) {
    StringBuilder description = new StringBuilder();
    description.append(challenger.getName()).append(" plays ").append(card.getName());

    if (target != null && !target.equals(challenger)) {
      description.append(" targeting ").append(target.getName());
    }

    return description.toString();
  }

  private BattleMoveEffects calculateMoveEffects(
      Card card, BattleMove move, Guardian challenger, Guardian target) {
    int damage = 0;
    int healing = 0;

    switch (move.getMoveType()) {
      case ATTACK:
        if (target != null) {
          damage = card.calculateDamageAgainst(convertGuardianToCard(target));
        } else {
          damage = card.getAttackPower(); // Base damage for solo battles
        }
        break;

      case DEFENSE:
        // Defense moves don't directly cause damage or healing
        // Their effects are applied when resolving opponent attacks
        break;

      case SUPPORT:
        healing = calculateHealingAmount(card, challenger);
        break;

      case SPECIAL:
        // Special moves can have both damage and healing effects
        if (target != null) {
          damage = card.calculateDamageAgainst(convertGuardianToCard(target));
        }
        healing = calculateHealingAmount(card, challenger);
        break;
    }

    return new BattleMoveEffects(damage, healing);
  }

  private int calculateHealingAmount(Card card, Guardian guardian) {
    // Healing based on card's defense power and Guardian's level
    int baseHealing = card.getDefensePower();
    int levelOrdinal = guardian.getLevel().ordinal(); // Get numeric level (0-based)
    double levelMultiplier = 1.0 + (levelOrdinal * 0.1); // 10% per level
    return (int) (baseHealing * levelMultiplier);
  }

  private Card convertGuardianToCard(Guardian guardian) {
    // For battle calculations, we create a temporary card representation of the Guardian
    // This is used when Guardians battle without specific cards
    int levelOrdinal = guardian.getLevel().ordinal(); // Get numeric level (0-based)
    return Card.create(
        "Guardian " + guardian.getName(),
        "Guardian representation for battle calculations",
        com.guardianes.cards.domain.model.CardElement.EARTH, // Default element
        com.guardianes.cards.domain.model.CardRarity.COMMON,
        (levelOrdinal + 1) * 10, // Attack based on level (1-based)
        (levelOrdinal + 1) * 8, // Defense based on level (1-based)
        5, // Standard energy cost
        null,
        "GUARDIAN_" + guardian.getId(),
        null);
  }

  private Battle checkBattleCompletion(Battle battle, Guardian challenger, Guardian target) {
    // For now, battles complete after a fixed number of moves or energy depletion
    // This can be enhanced with more sophisticated completion logic

    if (battle.getTotalEnergySpent() >= 100) { // Maximum energy per battle
      int xpReward = battle.getType().getBaseXpReward();
      return battle.complete(challenger.getId(), xpReward);
    }

    return battle;
  }

  private BattleStatistics calculateBattleStatistics(Battle battle, List<Guardian> participants) {
    int totalMoves = battle.getTotalMoves();
    int totalEnergySpent = battle.getTotalEnergySpent();

    return new BattleStatistics(totalMoves, totalEnergySpent);
  }

  private Guardian determineWinner(
      Battle battle, List<Guardian> participants, BattleStatistics stats) {
    if (battle.isSoloBattle()) {
      // Solo battles: player always wins if battle completes successfully
      return participants.get(0);
    }

    // For PvP battles, determine winner based on damage dealt or other metrics
    Guardian challenger =
        participants.stream()
            .filter(g -> g.getId().equals(battle.getChallengerId()))
            .findFirst()
            .orElse(null);

    return challenger; // Simplified winner determination
  }

  private List<XpReward> calculateXpRewards(
      Battle battle, List<Guardian> participants, Guardian winner, BattleStatistics stats) {
    return participants.stream()
        .map(
            guardian -> {
              boolean isWinner = winner != null && winner.getId().equals(guardian.getId());
              int xpAmount =
                  battle
                      .getType()
                      .calculateXpReward(
                          stats.totalEnergySpent() / participants.size(), isWinner, 1.0);
              return new XpReward(guardian.getId(), xpAmount);
            })
        .toList();
  }

  // Helper records for internal calculations
  private record BattleMoveEffects(int damage, int healing) {}

  private record BattleStatistics(int totalMoves, int totalEnergySpent) {}

  public record XpReward(Long guardianId, int amount) {}

  /** Exception thrown when a Guardian doesn't have enough energy for a battle action. */
  public static class InsufficientEnergyException extends RuntimeException {
    public InsufficientEnergyException(String message) {
      super(message);
    }
  }
}

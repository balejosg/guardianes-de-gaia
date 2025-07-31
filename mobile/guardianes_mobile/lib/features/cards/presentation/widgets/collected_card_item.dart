import 'package:flutter/material.dart';
import '../../domain/entities/collected_card.dart';
import '../../domain/entities/card.dart' as game_card;

class CollectedCardItem extends StatelessWidget {
  final CollectedCard collectedCard;
  final VoidCallback onTap;

  const CollectedCardItem({
    super.key,
    required this.collectedCard,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final card = collectedCard.card;
    
    return Card(
      elevation: 2,
      clipBehavior: Clip.antiAlias,
      child: InkWell(
        onTap: onTap,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Card Header with Element
            Container(
              height: 40,
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  colors: _getElementGradient(card.element),
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
              ),
              child: Row(
                children: [
                  const SizedBox(width: 8),
                  Text(
                    card.element.emoji,
                    style: const TextStyle(fontSize: 16),
                  ),
                  const SizedBox(width: 4),
                  Expanded(
                    child: Text(
                      card.element.displayName,
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 12,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ),
                  if (collectedCard.count > 1)
                    Container(
                      margin: const EdgeInsets.only(right: 8),
                      padding: const EdgeInsets.symmetric(
                        horizontal: 6,
                        vertical: 2,
                      ),
                      decoration: BoxDecoration(
                        color: Colors.white.withValues(alpha: 0.9),
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: Text(
                        'x${collectedCard.count}',
                        style: const TextStyle(
                          fontSize: 10,
                          fontWeight: FontWeight.bold,
                          color: Colors.black87,
                        ),
                      ),
                    ),
                ],
              ),
            ),
            
            // Card Content
            Expanded(
              child: Padding(
                padding: const EdgeInsets.all(8),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // Card Name
                    Text(
                      card.name,
                      style: const TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.bold,
                      ),
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                    ),
                    const SizedBox(height: 4),
                    
                    // Rarity Badge
                    Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 6,
                        vertical: 2,
                      ),
                      decoration: BoxDecoration(
                        color: _getRarityColor(card.rarity),
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Text(
                            card.rarity.colorEmoji,
                            style: const TextStyle(fontSize: 8),
                          ),
                          const SizedBox(width: 2),
                          Text(
                            card.rarity.displayName,
                            style: const TextStyle(
                              color: Colors.white,
                              fontSize: 8,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ],
                      ),
                    ),
                    
                    const Spacer(),
                    
                    // Stats Row
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        _buildStatChip('⚔️', card.attackPower.toString(), Colors.red),
                        _buildStatChip('🛡️', card.defensePower.toString(), Colors.blue),
                        _buildStatChip('⚡', card.energyCost.toString(), Colors.orange),
                      ],
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildStatChip(String emoji, String value, Color color) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 2),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(4),
        border: Border.all(
          color: color.withValues(alpha: 0.3),
          width: 0.5,
        ),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(
            emoji,
            style: const TextStyle(fontSize: 8),
          ),
          const SizedBox(width: 2),
          Text(
            value,
            style: TextStyle(
              fontSize: 8,
              fontWeight: FontWeight.bold,
              color: color,
            ),
          ),
        ],
      ),
    );
  }

  List<Color> _getElementGradient(game_card.CardElement element) {
    switch (element) {
      case game_card.CardElement.earth:
        return [Colors.brown, Colors.green.shade700];
      case game_card.CardElement.water:
        return [Colors.blue, Colors.cyan];
      case game_card.CardElement.fire:
        return [Colors.red, Colors.orange];
      case game_card.CardElement.air:
        return [Colors.lightBlue, Colors.grey.shade300];
    }
  }

  Color _getRarityColor(game_card.CardRarity rarity) {
    switch (rarity) {
      case game_card.CardRarity.common:
        return Colors.grey[500]!;
      case game_card.CardRarity.uncommon:
        return Colors.green[600]!;
      case game_card.CardRarity.rare:
        return Colors.blue[600]!;
      case game_card.CardRarity.epic:
        return Colors.purple[600]!;
      case game_card.CardRarity.legendary:
        return Colors.orange[600]!;
    }
  }
}
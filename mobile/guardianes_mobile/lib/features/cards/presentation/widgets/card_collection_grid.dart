import 'package:flutter/material.dart';
import '../../domain/entities/card_collection.dart';
import '../../domain/entities/collected_card.dart';
import 'collected_card_item.dart';

class CardCollectionGrid extends StatelessWidget {
  final CardCollection collection;
  final Function(CollectedCard) onCardTap;

  const CardCollectionGrid({
    super.key,
    required this.collection,
    required this.onCardTap,
  });

  @override
  Widget build(BuildContext context) {
    if (collection.cards.isEmpty) {
      return _buildEmptyState(context);
    }

    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 2,
        childAspectRatio: 0.75,
        crossAxisSpacing: 12,
        mainAxisSpacing: 12,
      ),
      itemCount: collection.cards.length,
      itemBuilder: (context, index) {
        final collectedCard = collection.cards[index];
        return CollectedCardItem(
          collectedCard: collectedCard,
          onTap: () => onCardTap(collectedCard),
        );
      },
    );
  }

  Widget _buildEmptyState(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(32),
      decoration: BoxDecoration(
        color: Colors.grey[100],
        borderRadius: BorderRadius.circular(16),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(
            Icons.collections_bookmark_outlined,
            size: 64,
            color: Colors.grey[400],
          ),
          const SizedBox(height: 16),
          Text(
            'No tienes cartas aún',
            style: Theme.of(context).textTheme.headlineSmall?.copyWith(
              color: Colors.grey[600],
              fontWeight: FontWeight.w500,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'Escanea códigos QR para comenzar tu colección',
            textAlign: TextAlign.center,
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
              color: Colors.grey[500],
            ),
          ),
          const SizedBox(height: 16),
          ElevatedButton.icon(
            onPressed: () {
              Navigator.of(context).pop();
            },
            icon: const Icon(Icons.qr_code_scanner),
            label: const Text('Escanear Carta'),
          ),
        ],
      ),
    );
  }
}
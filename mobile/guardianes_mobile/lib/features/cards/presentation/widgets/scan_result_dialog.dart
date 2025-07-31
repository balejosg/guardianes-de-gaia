import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../../core/utils/injection.dart';
import '../../domain/entities/card_scan_result.dart';
import '../../domain/entities/card.dart';
import '../bloc/card_bloc.dart';
import '../pages/card_collection_page.dart';

class ScanResultDialog extends StatelessWidget {
  final CardScanResult result;
  final VoidCallback onContinue;
  final VoidCallback onClose;
  final int guardianId;

  const ScanResultDialog({
    super.key,
    required this.result,
    required this.onContinue,
    required this.onClose,
    required this.guardianId,
  });

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(16),
      ),
      title: Row(
        children: [
          Icon(
            result.success ? Icons.check_circle : Icons.error,
            color: result.success ? Colors.green : Colors.red,
            size: 28,
          ),
          const SizedBox(width: 8),
          Expanded(
            child: Text(
              result.success ? '¡Éxito!' : 'Error',
              style: TextStyle(
                color: result.success ? Colors.green : Colors.red,
                fontWeight: FontWeight.bold,
              ),
            ),
          ),
        ],
      ),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            result.message,
            style: const TextStyle(fontSize: 16),
          ),
          if (result.success && result.card != null) ...[
            const SizedBox(height: 16),
            _buildCardInfo(result),
          ],
        ],
      ),
      actions: [
        if (result.success) ...[
          TextButton(
            onPressed: onContinue,
            child: const Text('Escanear otra'),
          ),
          ElevatedButton(
            onPressed: () {
              Navigator.of(context).pop(); // Close dialog first
              Navigator.of(context).push(
                MaterialPageRoute(
                  builder: (context) => BlocProvider<CardBloc>(
                    create: (context) => getIt<CardBloc>(),
                    child: CardCollectionPage(guardianId: guardianId),
                  ),
                ),
              );
            },
            child: const Text('Ver colección'),
          ),
        ] else ...[
          TextButton(
            onPressed: onContinue,
            child: const Text('Intentar de nuevo'),
          ),
          TextButton(
            onPressed: onClose,
            child: const Text('Cerrar'),
          ),
        ],
      ],
    );
  }

  Widget _buildCardInfo(CardScanResult result) {
    final card = result.card!;
    
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.grey[100],
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: Colors.grey[300]!),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Text(
                card.element.emoji,
                style: const TextStyle(fontSize: 24),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      card.name,
                      style: const TextStyle(
                        fontWeight: FontWeight.bold,
                        fontSize: 16,
                      ),
                    ),
                    Text(
                      card.element.displayName,
                      style: TextStyle(
                        color: Colors.grey[600],
                        fontSize: 12,
                      ),
                    ),
                  ],
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 8,
                  vertical: 4,
                ),
                decoration: BoxDecoration(
                  color: _getRarityColor(card.rarity),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(
                      card.rarity.colorEmoji,
                      style: const TextStyle(fontSize: 12),
                    ),
                    const SizedBox(width: 4),
                    Text(
                      card.rarity.displayName,
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 10,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Row(
            children: [
              _buildStatBadge(
                '⚔️ ${card.attackPower}',
                Colors.red[100]!,
              ),
              const SizedBox(width: 8),
              _buildStatBadge(
                '🛡️ ${card.defensePower}',
                Colors.blue[100]!,
              ),
              const SizedBox(width: 8),
              _buildStatBadge(
                '⚡ ${card.energyCost}',
                Colors.yellow[100]!,
              ),
            ],
          ),
          if (result.count != null && result.count! > 1) ...[
            const SizedBox(height: 8),
            Container(
              padding: const EdgeInsets.symmetric(
                horizontal: 8,
                vertical: 4,
              ),
              decoration: BoxDecoration(
                color: Colors.orange[100],
                borderRadius: BorderRadius.circular(8),
              ),
              child: Text(
                result.isNew 
                    ? 'Primera vez coleccionada'
                    : 'Tienes ${result.count} copias',
                style: TextStyle(
                  color: Colors.orange[800],
                  fontSize: 12,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildStatBadge(String text, Color backgroundColor) {
    return Container(
      padding: const EdgeInsets.symmetric(
        horizontal: 6,
        vertical: 2,
      ),
      decoration: BoxDecoration(
        color: backgroundColor,
        borderRadius: BorderRadius.circular(4),
      ),
      child: Text(
        text,
        style: const TextStyle(
          fontSize: 10,
          fontWeight: FontWeight.w500,
        ),
      ),
    );
  }

  Color _getRarityColor(CardRarity rarity) {
    switch (rarity) {
      case CardRarity.common:
        return Colors.grey[400]!;
      case CardRarity.uncommon:
        return Colors.green[400]!;
      case CardRarity.rare:
        return Colors.blue[400]!;
      case CardRarity.epic:
        return Colors.purple[400]!;
      case CardRarity.legendary:
        return Colors.orange[400]!;
    }
  }
}
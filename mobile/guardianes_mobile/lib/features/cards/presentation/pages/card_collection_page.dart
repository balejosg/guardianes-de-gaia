import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../bloc/card_bloc.dart';
import '../bloc/card_event.dart';
import '../bloc/card_state.dart';
import '../widgets/card_collection_grid.dart';
import '../widgets/collection_stats_card.dart';

class CardCollectionPage extends StatefulWidget {
  final int guardianId;

  const CardCollectionPage({
    super.key,
    required this.guardianId,
  });

  @override
  State<CardCollectionPage> createState() => _CardCollectionPageState();
}

class _CardCollectionPageState extends State<CardCollectionPage> {
  @override
  void initState() {
    super.initState();
    context
        .read<CardBloc>()
        .add(LoadCardCollectionEvent(guardianId: widget.guardianId));
    context
        .read<CardBloc>()
        .add(LoadCollectionStatisticsEvent(guardianId: widget.guardianId));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Mi Colección'),
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        actions: [
          IconButton(
            onPressed: () {
              // TODO: Add search functionality
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('Búsqueda próximamente')),
              );
            },
            icon: const Icon(Icons.search),
          ),
        ],
      ),
      body: BlocBuilder<CardBloc, CardState>(
        builder: (context, state) {
          if (state is CardLoading) {
            return const Center(child: CircularProgressIndicator());
          }

          if (state is CardError) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Icon(
                    Icons.error_outline,
                    size: 64,
                    color: Colors.red,
                  ),
                  const SizedBox(height: 16),
                  Text(
                    'Error al cargar la colección',
                    style: Theme.of(context).textTheme.headlineSmall,
                  ),
                  const SizedBox(height: 8),
                  Text(
                    state.error,
                    textAlign: TextAlign.center,
                    style: Theme.of(context).textTheme.bodyMedium,
                  ),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: () {
                      context.read<CardBloc>().add(
                            LoadCardCollectionEvent(
                                guardianId: widget.guardianId),
                          );
                      context.read<CardBloc>().add(
                            LoadCollectionStatisticsEvent(
                                guardianId: widget.guardianId),
                          );
                    },
                    child: const Text('Intentar de nuevo'),
                  ),
                ],
              ),
            );
          }

          if (state is CollectionLoaded) {
            return RefreshIndicator(
              onRefresh: () async {
                context.read<CardBloc>().add(
                      LoadCardCollectionEvent(guardianId: widget.guardianId),
                    );
                context.read<CardBloc>().add(
                      LoadCollectionStatisticsEvent(
                          guardianId: widget.guardianId),
                    );
              },
              child: SingleChildScrollView(
                physics: const AlwaysScrollableScrollPhysics(),
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // Statistics Card
                    BlocBuilder<CardBloc, CardState>(
                      buildWhen: (previous, current) =>
                          current is CollectionStatisticsLoaded,
                      builder: (context, statsState) {
                        if (statsState is CollectionStatisticsLoaded) {
                          return CollectionStatsCard(
                            statistics: statsState.statistics,
                          );
                        }
                        return const SizedBox.shrink();
                      },
                    ),
                    const SizedBox(height: 24),

                    // Collection Header
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          'Cartas Coleccionadas',
                          style: Theme.of(context)
                              .textTheme
                              .headlineSmall
                              ?.copyWith(
                                fontWeight: FontWeight.bold,
                              ),
                        ),
                        Text(
                          '${state.collection.cards.length} cartas',
                          style:
                              Theme.of(context).textTheme.bodyMedium?.copyWith(
                                    color: Colors.grey[600],
                                  ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),

                    // Cards Grid
                    CardCollectionGrid(
                      collection: state.collection,
                      onCardTap: (card) {
                        // TODO: Show card details
                        ScaffoldMessenger.of(context).showSnackBar(
                          SnackBar(
                            content: Text(
                                'Detalles de ${card.card.name} próximamente'),
                          ),
                        );
                      },
                    ),
                  ],
                ),
              ),
            );
          }

          return const Center(
            child: Text('Carga la colección para empezar'),
          );
        },
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          Navigator.of(context).pop();
        },
        child: const Icon(Icons.qr_code_scanner),
        tooltip: 'Escanear nueva carta',
      ),
    );
  }
}

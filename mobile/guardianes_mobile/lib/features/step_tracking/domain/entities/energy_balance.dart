import 'package:equatable/equatable.dart';

class EnergyBalance extends Equatable {
  final int currentBalance;
  final int totalEarned;
  final int totalSpent;
  final DateTime lastUpdated;

  const EnergyBalance({
    required this.currentBalance,
    required this.totalEarned,
    required this.totalSpent,
    required this.lastUpdated,
  });

  @override
  List<Object?> get props => [
        currentBalance,
        totalEarned,
        totalSpent,
        lastUpdated,
      ];
}
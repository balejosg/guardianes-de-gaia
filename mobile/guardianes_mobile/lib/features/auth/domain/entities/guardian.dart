import 'package:equatable/equatable.dart';

class Guardian extends Equatable {
  final int id;
  final String username;
  final String email;
  final String name;
  final DateTime birthDate;
  final int age;
  final String level;
  final int experiencePoints;
  final int experienceToNextLevel;
  final int totalSteps;
  final int totalEnergyGenerated;
  final DateTime createdAt;
  final DateTime lastActiveAt;
  final bool isChild;

  const Guardian({
    required this.id,
    required this.username,
    required this.email,
    required this.name,
    required this.birthDate,
    required this.age,
    required this.level,
    required this.experiencePoints,
    required this.experienceToNextLevel,
    required this.totalSteps,
    required this.totalEnergyGenerated,
    required this.createdAt,
    required this.lastActiveAt,
    required this.isChild,
  });

  @override
  List<Object?> get props => [
        id,
        username,
        email,
        name,
        birthDate,
        age,
        level,
        experiencePoints,
        experienceToNextLevel,
        totalSteps,
        totalEnergyGenerated,
        createdAt,
        lastActiveAt,
        isChild,
      ];
}

// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'guardian_model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

GuardianModel _$GuardianModelFromJson(Map<String, dynamic> json) =>
    GuardianModel(
      id: (json['id'] as num).toInt(),
      username: json['username'] as String,
      email: json['email'] as String,
      name: json['name'] as String,
      birthDate: DateTime.parse(json['birthDate'] as String),
      age: (json['age'] as num).toInt(),
      level: json['level'] as String,
      experiencePoints: (json['experiencePoints'] as num).toInt(),
      experienceToNextLevel: (json['experienceToNextLevel'] as num).toInt(),
      totalSteps: (json['totalSteps'] as num).toInt(),
      totalEnergyGenerated: (json['totalEnergyGenerated'] as num).toInt(),
      createdAt: DateTime.parse(json['createdAt'] as String),
      lastActiveAt: DateTime.parse(json['lastActiveAt'] as String),
      isChild: json['isChild'] as bool,
    );

Map<String, dynamic> _$GuardianModelToJson(GuardianModel instance) =>
    <String, dynamic>{
      'id': instance.id,
      'username': instance.username,
      'email': instance.email,
      'name': instance.name,
      'birthDate': instance.birthDate.toIso8601String(),
      'age': instance.age,
      'level': instance.level,
      'experiencePoints': instance.experiencePoints,
      'experienceToNextLevel': instance.experienceToNextLevel,
      'totalSteps': instance.totalSteps,
      'totalEnergyGenerated': instance.totalEnergyGenerated,
      'createdAt': instance.createdAt.toIso8601String(),
      'lastActiveAt': instance.lastActiveAt.toIso8601String(),
      'isChild': instance.isChild,
    };

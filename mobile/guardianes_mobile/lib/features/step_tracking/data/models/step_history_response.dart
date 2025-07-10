import 'package:json_annotation/json_annotation.dart';

part 'step_history_response.g.dart';

@JsonSerializable()
class StepHistoryEntry {
  final DateTime date;
  final int stepCount;
  final int energyEarned;

  const StepHistoryEntry({
    required this.date,
    required this.stepCount,
    required this.energyEarned,
  });

  factory StepHistoryEntry.fromJson(Map<String, dynamic> json) =>
      _$StepHistoryEntryFromJson(json);

  factory StepHistoryEntry.fromApiResponse(Map<String, dynamic> json) {
    // Handle the backend API response format
    final date = json['date'] is String 
        ? DateTime.parse(json['date']) 
        : DateTime.now();
    
    final stepCount = json['totalSteps'] is Map 
        ? (json['totalSteps']['value'] ?? 0) as int
        : (json['totalStepsValue'] ?? 0) as int;
    
    // Calculate energy from steps (1 energy per 10 steps)
    final energyEarned = stepCount ~/ 10;
    
    return StepHistoryEntry(
      date: date,
      stepCount: stepCount,
      energyEarned: energyEarned,
    );
  }

  Map<String, dynamic> toJson() => _$StepHistoryEntryToJson(this);
}

@JsonSerializable()
class StepHistoryResponse {
  @JsonKey(name: 'dailySteps', fromJson: _parseHistoryEntries)
  final List<StepHistoryEntry> history;
  @JsonKey(includeFromJson: false, includeToJson: false)
  final int totalSteps;
  @JsonKey(includeFromJson: false, includeToJson: false)
  final int totalEnergy;

  const StepHistoryResponse({
    required this.history,
    this.totalSteps = 0,
    this.totalEnergy = 0,
  });

  static List<StepHistoryEntry> _parseHistoryEntries(dynamic value) {
    if (value == null) return [];
    if (value is List) {
      return value.map((item) => StepHistoryEntry.fromApiResponse(item)).toList();
    }
    return [];
  }

  factory StepHistoryResponse.fromJson(Map<String, dynamic> json) =>
      _$StepHistoryResponseFromJson(json);

  Map<String, dynamic> toJson() => _$StepHistoryResponseToJson(this);
}
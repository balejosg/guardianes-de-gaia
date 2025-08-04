import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:guardianes_mobile/features/walking/data/models/step_submission_request_dto.dart';
import 'package:guardianes_mobile/features/walking/data/models/step_submission_response_dto.dart';
import 'package:guardianes_mobile/features/walking/data/models/current_step_count_response_dto.dart';
import 'package:guardianes_mobile/features/walking/data/models/step_history_response_dto.dart';

abstract class StepRemoteDataSource {
  /// Submit steps for a guardian
  Future<StepSubmissionResponseDto> submitSteps(
    int guardianId,
    StepSubmissionRequestDto request,
  );

  /// Get current daily step count for a guardian
  Future<CurrentStepCountResponseDto> getCurrentStepCount(int guardianId);

  /// Get step history for a guardian within a date range
  Future<StepHistoryResponseDto> getStepHistory(
    int guardianId,
    String fromDate,
    String toDate,
  );
}

class StepRemoteDataSourceImpl implements StepRemoteDataSource {
  final http.Client client;
  static const String baseUrl = 'http://dev-guardianes.duckdns.org:8080';

  StepRemoteDataSourceImpl(this.client);

  @override
  Future<StepSubmissionResponseDto> submitSteps(
    int guardianId,
    StepSubmissionRequestDto request,
  ) async {
    final url = Uri.parse('$baseUrl/api/v1/guardians/$guardianId/steps');
    final response = await client.post(
      url,
      headers: {
        'Content-Type': 'application/json',
      },
      body: json.encode(request.toJson()),
    );

    if (response.statusCode == 201) {
      return StepSubmissionResponseDto.fromJson(json.decode(response.body));
    } else {
      throw Exception('Failed to submit steps: ${response.statusCode}');
    }
  }

  @override
  Future<CurrentStepCountResponseDto> getCurrentStepCount(
      int guardianId) async {
    final url =
        Uri.parse('$baseUrl/api/v1/guardians/$guardianId/steps/current');
    final response = await client.get(
      url,
      headers: {
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      return CurrentStepCountResponseDto.fromJson(json.decode(response.body));
    } else {
      throw Exception(
          'Failed to get current step count: ${response.statusCode}');
    }
  }

  @override
  Future<StepHistoryResponseDto> getStepHistory(
    int guardianId,
    String fromDate,
    String toDate,
  ) async {
    final url = Uri.parse(
        '$baseUrl/api/v1/guardians/$guardianId/steps/history?from=$fromDate&to=$toDate');
    final response = await client.get(
      url,
      headers: {
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      return StepHistoryResponseDto.fromJson(json.decode(response.body));
    } else {
      throw Exception('Failed to get step history: ${response.statusCode}');
    }
  }
}

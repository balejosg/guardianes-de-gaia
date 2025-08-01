import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/guardian_model.dart';

abstract class GuardianProfileRemoteDataSource {
  Future<GuardianModel> getGuardianProfile(int guardianId);
  Future<GuardianModel> updateGuardianProfile(
      int guardianId, Map<String, dynamic> updates);
}

class GuardianProfileRemoteDataSourceImpl
    implements GuardianProfileRemoteDataSource {
  final http.Client client;
  final String baseUrl;

  GuardianProfileRemoteDataSourceImpl({
    required this.client,
    this.baseUrl = 'https://my-guardianes.duckdns.org',
  });

  @override
  Future<GuardianModel> getGuardianProfile(int guardianId) async {
    final response = await client.get(
      Uri.parse('$baseUrl/api/v1/guardians/$guardianId'),
      headers: {'Content-Type': 'application/json'},
    );

    if (response.statusCode == 200) {
      return GuardianModel.fromJson(json.decode(response.body));
    } else if (response.statusCode == 404) {
      throw Exception('Guardian not found');
    } else {
      throw Exception('Failed to get guardian profile: ${response.statusCode}');
    }
  }

  @override
  Future<GuardianModel> updateGuardianProfile(
      int guardianId, Map<String, dynamic> updates) async {
    final response = await client.put(
      Uri.parse('$baseUrl/api/v1/guardians/$guardianId'),
      headers: {'Content-Type': 'application/json'},
      body: json.encode(updates),
    );

    if (response.statusCode == 200) {
      return GuardianModel.fromJson(json.decode(response.body));
    } else if (response.statusCode == 404) {
      throw Exception('Guardian not found');
    } else {
      throw Exception(
          'Failed to update guardian profile: ${response.statusCode}');
    }
  }
}

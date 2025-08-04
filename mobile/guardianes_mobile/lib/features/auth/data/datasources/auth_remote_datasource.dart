import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:guardianes_mobile/features/auth/data/models/auth_response_model.dart';

abstract class AuthRemoteDataSource {
  Future<AuthResponseModel> register({
    required String username,
    required String email,
    required String password,
    required String name,
    required DateTime birthDate,
  });

  Future<AuthResponseModel> login({
    required String usernameOrEmail,
    required String password,
  });
}

class AuthRemoteDataSourceImpl implements AuthRemoteDataSource {
  final http.Client client;
  final String baseUrl;

  AuthRemoteDataSourceImpl({
    required this.client,
    this.baseUrl = 'http://dev-guardianes.duckdns.org:8080/api',
  });

  @override
  Future<AuthResponseModel> register({
    required String username,
    required String email,
    required String password,
    required String name,
    required DateTime birthDate,
  }) async {
    final response = await client.post(
      Uri.parse('$baseUrl/auth/register'),
      headers: {
        'Content-Type': 'application/json',
      },
      body: jsonEncode({
        'username': username,
        'email': email,
        'password': password,
        'name': name,
        'birthDate': birthDate.toIso8601String().split('T')[0],
      }),
    );

    if (response.statusCode == 201) {
      return AuthResponseModel.fromJson(jsonDecode(response.body));
    } else {
      final error = jsonDecode(response.body);
      throw Exception(error['message'] ?? 'Registration failed');
    }
  }

  @override
  Future<AuthResponseModel> login({
    required String usernameOrEmail,
    required String password,
  }) async {
    final response = await client.post(
      Uri.parse('$baseUrl/auth/login'),
      headers: {
        'Content-Type': 'application/json',
      },
      body: jsonEncode({
        'usernameOrEmail': usernameOrEmail,
        'password': password,
      }),
    );

    if (response.statusCode == 200) {
      return AuthResponseModel.fromJson(jsonDecode(response.body));
    } else {
      final error = jsonDecode(response.body);
      throw Exception(error['message'] ?? 'Login failed');
    }
  }
}

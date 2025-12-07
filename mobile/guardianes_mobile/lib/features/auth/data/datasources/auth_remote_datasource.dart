import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'package:http/http.dart' as http;
import 'package:guardianes_mobile/features/auth/data/models/auth_response_model.dart';

/// Custom exception for authentication errors with user-friendly messages
class AuthException implements Exception {
  final String message;
  final int? statusCode;
  final String? technicalDetails;

  AuthException(this.message, {this.statusCode, this.technicalDetails});

  @override
  String toString() => message;
}

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
  final Duration timeout;

  AuthRemoteDataSourceImpl({
    required this.client,
    this.baseUrl = 'http://dev-guardianes.duckdns.org/api',
    this.timeout = const Duration(seconds: 30),
  });

  @override
  Future<AuthResponseModel> register({
    required String username,
    required String email,
    required String password,
    required String name,
    required DateTime birthDate,
  }) async {
    try {
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
      ).timeout(timeout);

      if (response.statusCode == 201) {
        try {
          return AuthResponseModel.fromJson(jsonDecode(response.body));
        } on FormatException catch (e) {
          throw AuthException(
            'Error al procesar la respuesta del servidor',
            statusCode: response.statusCode,
            technicalDetails: 'JSON parse error: ${e.message}',
          );
        }
      } else {
        throw _handleErrorResponse(response, 'registro');
      }
    } on TimeoutException {
      throw AuthException(
        'El servidor tardó demasiado en responder. Por favor, intenta de nuevo.',
        technicalDetails: 'Request timeout after ${timeout.inSeconds}s',
      );
    } on SocketException catch (e) {
      throw AuthException(
        'No se pudo conectar al servidor. Verifica tu conexión a internet.',
        technicalDetails: 'Socket error: ${e.message}',
      );
    } on AuthException {
      rethrow;
    } catch (e) {
      throw AuthException(
        'Error inesperado durante el registro. Por favor, intenta de nuevo.',
        technicalDetails: 'Unexpected error: $e',
      );
    }
  }

  @override
  Future<AuthResponseModel> login({
    required String usernameOrEmail,
    required String password,
  }) async {
    try {
      final response = await client.post(
        Uri.parse('$baseUrl/auth/login'),
        headers: {
          'Content-Type': 'application/json',
        },
        body: jsonEncode({
          'usernameOrEmail': usernameOrEmail,
          'password': password,
        }),
      ).timeout(timeout);

      if (response.statusCode == 200) {
        try {
          return AuthResponseModel.fromJson(jsonDecode(response.body));
        } on FormatException catch (e) {
          throw AuthException(
            'Error al procesar la respuesta del servidor',
            statusCode: response.statusCode,
            technicalDetails: 'JSON parse error: ${e.message}',
          );
        }
      } else {
        throw _handleErrorResponse(response, 'inicio de sesión');
      }
    } on TimeoutException {
      throw AuthException(
        'El servidor tardó demasiado en responder. Por favor, intenta de nuevo.',
        technicalDetails: 'Request timeout after ${timeout.inSeconds}s',
      );
    } on SocketException catch (e) {
      throw AuthException(
        'No se pudo conectar al servidor. Verifica tu conexión a internet.',
        technicalDetails: 'Socket error: ${e.message}',
      );
    } on AuthException {
      rethrow;
    } catch (e) {
      throw AuthException(
        'Error inesperado durante el inicio de sesión. Por favor, intenta de nuevo.',
        technicalDetails: 'Unexpected error: $e',
      );
    }
  }

  /// Handles HTTP error responses and returns user-friendly error messages
  AuthException _handleErrorResponse(http.Response response, String operation) {
    // Try to parse JSON error response
    try {
      final error = jsonDecode(response.body);
      final message = error['message'] ?? error['error'] ?? 'Error desconocido';
      return AuthException(
        message,
        statusCode: response.statusCode,
        technicalDetails: 'Server returned: ${response.body}',
      );
    } on FormatException {
      // Response is not valid JSON (e.g., HTML error page from nginx)
      return _getErrorMessageForStatusCode(response.statusCode, operation);
    }
  }

  /// Returns user-friendly error messages based on HTTP status codes
  AuthException _getErrorMessageForStatusCode(int statusCode, String operation) {
    switch (statusCode) {
      case 400:
        return AuthException(
          'Los datos ingresados no son válidos. Por favor, revisa la información.',
          statusCode: statusCode,
        );
      case 401:
        return AuthException(
          'Credenciales incorrectas. Por favor, verifica tu usuario y contraseña.',
          statusCode: statusCode,
        );
      case 403:
        return AuthException(
          'No tienes permiso para realizar esta acción.',
          statusCode: statusCode,
        );
      case 404:
        return AuthException(
          'El servicio no está disponible. Por favor, intenta más tarde.',
          statusCode: statusCode,
        );
      case 409:
        return AuthException(
          'El nombre de usuario o email ya está registrado.',
          statusCode: statusCode,
        );
      case 422:
        return AuthException(
          'Los datos ingresados no cumplen con los requisitos.',
          statusCode: statusCode,
        );
      case 429:
        return AuthException(
          'Demasiados intentos. Por favor, espera un momento antes de intentar de nuevo.',
          statusCode: statusCode,
        );
      case 500:
      case 502:
      case 503:
      case 504:
        return AuthException(
          'El servidor tiene problemas. Por favor, intenta más tarde.',
          statusCode: statusCode,
          technicalDetails: 'Server error: HTTP $statusCode',
        );
      default:
        return AuthException(
          'Error durante el $operation (código: $statusCode)',
          statusCode: statusCode,
        );
    }
  }
}

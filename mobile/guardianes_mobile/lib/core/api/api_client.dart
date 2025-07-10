import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../constants/api_constants.dart';

class ApiClient {
  static final ApiClient _instance = ApiClient._internal();
  factory ApiClient() => _instance;
  ApiClient._internal();

  late Dio _dio;
  final FlutterSecureStorage _storage = const FlutterSecureStorage();

  void initialize() {
    _dio = Dio(BaseOptions(
      baseUrl: ApiConstants.baseUrl,
      connectTimeout: const Duration(seconds: 30),
      receiveTimeout: const Duration(seconds: 30),
      headers: {
        'Content-Type': ApiConstants.contentTypeJson,
        'Accept': ApiConstants.contentTypeJson,
      },
    ));

    _dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) async {
          // Add auth token to requests
          final token = await _storage.read(key: 'auth_token');
          if (token != null) {
            options.headers[ApiConstants.authorizationHeader] = 
                '${ApiConstants.bearerPrefix}$token';
          }
          handler.next(options);
        },
        onError: (error, handler) async {
          // Handle token refresh on 401
          if (error.response?.statusCode == 401) {
            try {
              await _refreshToken();
              // Retry the original request
              final response = await _dio.fetch(error.requestOptions);
              handler.resolve(response);
            } catch (e) {
              // If refresh fails, clear tokens and redirect to login
              await _clearTokens();
              handler.next(error);
            }
          } else {
            handler.next(error);
          }
        },
      ),
    );
  }

  Future<void> _refreshToken() async {
    final refreshToken = await _storage.read(key: 'refresh_token');
    if (refreshToken == null) throw Exception('No refresh token available');

    final response = await _dio.post(
      ApiConstants.validatePath,
      data: {'refreshToken': refreshToken},
    );

    final newToken = response.data['token'];
    final newRefreshToken = response.data['refreshToken'];
    
    await _storage.write(key: 'auth_token', value: newToken);
    await _storage.write(key: 'refresh_token', value: newRefreshToken);
  }

  Future<void> _clearTokens() async {
    await _storage.delete(key: 'auth_token');
    await _storage.delete(key: 'refresh_token');
  }

  Future<Response> get(String path, {Map<String, dynamic>? queryParameters}) {
    return _dio.get(path, queryParameters: queryParameters);
  }

  Future<Response> post(String path, {dynamic data}) {
    return _dio.post(path, data: data);
  }

  Future<Response> put(String path, {dynamic data}) {
    return _dio.put(path, data: data);
  }

  Future<Response> delete(String path) {
    return _dio.delete(path);
  }

  Future<void> setAuthToken(String token, String refreshToken) async {
    await _storage.write(key: 'auth_token', value: token);
    await _storage.write(key: 'refresh_token', value: refreshToken);
  }

  Future<String?> getAuthToken() async {
    return await _storage.read(key: 'auth_token');
  }

  Future<bool> isAuthenticated() async {
    final token = await getAuthToken();
    return token != null;
  }

  Future<void> clearAuth() async {
    await _clearTokens();
  }
}
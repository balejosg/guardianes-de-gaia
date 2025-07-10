import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../api/api_client.dart';
import '../constants/api_constants.dart';

class AuthService {
  static final AuthService _instance = AuthService._internal();
  factory AuthService() => _instance;
  AuthService._internal();

  final FlutterSecureStorage _storage = const FlutterSecureStorage();
  final ApiClient _apiClient = ApiClient();

  Future<bool> login(String username, String password) async {
    try {
      final response = await _apiClient.post(
        ApiConstants.loginPath,
        data: {
          'username': username,
          'password': password,
        },
      );

      if (response.statusCode == 200) {
        final token = response.data['token'];
        final refreshToken = response.data['refreshToken'] ?? token; // fallback if no refresh token
        
        // Extract guardian ID from username
        final username = response.data['username'];
        String guardianId;
        if (username.startsWith('guardian')) {
          guardianId = username.replaceAll('guardian', '');
        } else if (username == 'testuser') {
          guardianId = '999'; // Use a test ID for testuser
        } else if (username == 'admin') {
          guardianId = '1'; // Admin acts as guardian 1
        } else {
          guardianId = '1'; // Default fallback
        }
        
        await _apiClient.setAuthToken(token, refreshToken);
        await _storage.write(key: 'guardian_id', value: guardianId);
        
        return true;
      }
      return false;
    } catch (e) {
      return false;
    }
  }

  Future<void> logout() async {
    await _apiClient.clearAuth();
    await _storage.delete(key: 'guardian_id');
  }

  Future<bool> isAuthenticated() async {
    return await _apiClient.isAuthenticated();
  }

  Future<String?> getCurrentGuardianId() async {
    return await _storage.read(key: 'guardian_id');
  }

  Future<void> initialize() async {
    _apiClient.initialize();
  }
}
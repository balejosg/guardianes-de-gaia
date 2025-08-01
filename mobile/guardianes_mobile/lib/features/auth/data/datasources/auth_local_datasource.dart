import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:guardianes_mobile/features/auth/data/models/guardian_model.dart';

abstract class AuthLocalDataSource {
  Future<void> saveToken(String token);
  Future<String?> getToken();
  Future<void> removeToken();
  Future<void> saveGuardian(GuardianModel guardian);
  Future<GuardianModel?> getGuardian();
  Future<void> removeGuardian();
}

class AuthLocalDataSourceImpl implements AuthLocalDataSource {
  final SharedPreferences sharedPreferences;

  AuthLocalDataSourceImpl({required this.sharedPreferences});

  static const String tokenKey = 'auth_token';
  static const String guardianKey = 'guardian_data';

  @override
  Future<void> saveToken(String token) async {
    await sharedPreferences.setString(tokenKey, token);
  }

  @override
  Future<String?> getToken() async {
    return sharedPreferences.getString(tokenKey);
  }

  @override
  Future<void> removeToken() async {
    await sharedPreferences.remove(tokenKey);
  }

  @override
  Future<void> saveGuardian(GuardianModel guardian) async {
    final guardianJson = jsonEncode(guardian.toJson());
    await sharedPreferences.setString(guardianKey, guardianJson);
  }

  @override
  Future<GuardianModel?> getGuardian() async {
    final guardianJson = sharedPreferences.getString(guardianKey);
    if (guardianJson != null) {
      final guardianMap = jsonDecode(guardianJson) as Map<String, dynamic>;
      return GuardianModel.fromJson(guardianMap);
    }
    return null;
  }

  @override
  Future<void> removeGuardian() async {
    await sharedPreferences.remove(guardianKey);
  }
}

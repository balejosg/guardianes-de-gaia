class ApiConstants {
  static const String baseUrl = 'http://localhost:8080';
  static const String apiVersion = '/api/v1';
  
  // Authentication endpoints
  static const String authPath = '$apiVersion/auth';
  static const String loginPath = '$authPath/login';
  static const String validatePath = '$authPath/validate';
  
  // Guardian-centric endpoints (require guardianId)
  static const String guardiansPath = '$apiVersion/guardians';
  
  // Step tracking endpoints
  static String getStepsPath(String guardianId) => '$guardiansPath/$guardianId/steps';
  static String getCurrentStepsPath(String guardianId) => '$guardiansPath/$guardianId/steps/current';
  static String getStepHistoryPath(String guardianId) => '$guardiansPath/$guardianId/steps/history';
  
  // Energy management endpoints  
  static String getEnergyBalancePath(String guardianId) => '$guardiansPath/$guardianId/energy/balance';
  static String getEnergySpendingPath(String guardianId) => '$guardiansPath/$guardianId/energy/spend';
  
  // Health endpoints
  static const String healthPath = '$apiVersion/health';
  static const String gameSystemsHealthPath = '$healthPath/game-systems';
  
  // Headers
  static const String contentTypeJson = 'application/json';
  static const String authorizationHeader = 'Authorization';
  static const String bearerPrefix = 'Bearer ';
}